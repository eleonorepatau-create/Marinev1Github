package fr.marine.routine.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.marine.routine.core.AutoStart
import fr.marine.routine.core.DayState
import fr.marine.routine.core.ParentPin
import fr.marine.routine.core.Planning
import fr.marine.routine.core.RoutineActivity
import fr.marine.routine.core.RoutineRun
import fr.marine.routine.core.RoutineRunner
import fr.marine.routine.core.RunEvent
import fr.marine.routine.core.Settings
import fr.marine.routine.core.Timing
import fr.marine.routine.data.LocalStore
import fr.marine.routine.voice.Speaker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

enum class Screen { HOME, PARENT_PIN, MARINE, PARENT }

/** Affichage en vert de l'activité qui vient d'être confirmée. */
data class Celebration(val activity: RoutineActivity, val untilMs: Long)

data class UiState(
    val settings: Settings,
    val day: DayState,
    val nowMs: Long,
    val today: LocalDate,
    val screen: Screen = Screen.HOME,
    val celebration: Celebration? = null,
    val speech: Speaker.Status = Speaker.Status.STARTING,
    val marineMessage: String? = null,
) {
    /** Routine affichée chez Marine aujourd'hui (en cours ou terminée). */
    val activeRun: RoutineRun? get() = day.activeRun?.takeIf { it.date == today.toString() }
}

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val store = LocalStore(app)
    private val speaker = Speaker(app)

    private val _state: MutableStateFlow<UiState>
    val state: StateFlow<UiState>

    init {
        val today = LocalDate.now()
        val settings = store.loadSettings().let { it.copy(taxi = Planning.pruneOverrides(it.taxi, today)) }
        val day = store.loadDayState().prune(today)
        _state = MutableStateFlow(UiState(settings, day, System.currentTimeMillis(), today))
        state = _state.asStateFlow()
        speaker.setRate(settings.speechRate)
        speaker.onStatusChanged = { status -> _state.update { it.copy(speech = status) } }
        viewModelScope.launch {
            while (true) {
                tick()
                delay(1_000)
            }
        }
    }

    private fun tick() {
        val now = System.currentTimeMillis()
        val today = LocalDate.now()
        val time = LocalTime.now()
        var s = _state.value.copy(nowMs = now, today = today)
        if (s.celebration != null && now >= s.celebration!!.untilMs) s = s.copy(celebration = null)
        _state.value = s

        // Démarrage automatique (routine du matin : lever = taxi − 60 min).
        AutoStart.routineToStart(s.settings, s.day, today, time.hour * 60 + time.minute)?.let { routine ->
            startRoutine(routine.id)
            return
        }

        // Rappels et retards de l'activité en cours.
        val run = s.activeRun ?: return
        val (updated, events) = RoutineRunner.dueEvents(run, now, s.settings.significantDelayMinutes)
        if (updated != run) saveDay(s.day.withRun(updated))
        events.forEach { event ->
            when (event) {
                is RunEvent.Reminder -> speaker.speak(reminderText(event.activity))
                // Signalé dans l'écran Parent de cet appareil uniquement (pas encore de transmission).
                is RunEvent.SignificantDelay -> Unit
            }
        }
    }

    private fun reminderText(activity: RoutineActivity): String {
        val reminderAt = Timing.reminderAtMs(activity) ?: 0
        val remainingMin = ((activity.durationMinutes * Timing.MINUTE_MS - reminderAt) / Timing.MINUTE_MS).toInt()
        // Ton d'accompagnement : encourager, sans presser.
        return when {
            remainingMin >= 2 -> "Tu avances bien. Encore $remainingMin minutes pour : ${activity.name}."
            remainingMin == 1 -> "Tu avances bien. Encore une minute pour : ${activity.name}."
            else -> "Tu avances bien. C'est bientôt fini : ${activity.name}."
        }
    }

    // --- Navigation ---------------------------------------------------------------------------

    fun openMarine() = _state.update { it.copy(screen = Screen.MARINE, marineMessage = null) }

    fun requestParent() = _state.update {
        it.copy(screen = if (it.settings.parentPin != null) Screen.PARENT_PIN else Screen.PARENT)
    }

    fun submitPin(pin: String): Boolean {
        val stored = _state.value.settings.parentPin ?: return true.also { requestParent() }
        val ok = ParentPin.matches(stored, pin)
        if (ok) _state.update { it.copy(screen = Screen.PARENT) }
        return ok
    }

    fun goHome() {
        speaker.stop()
        _state.update { it.copy(screen = Screen.HOME) }
    }

    // --- Actions Marine -----------------------------------------------------------------------

    fun startRoutine(routineId: String) {
        val s = _state.value
        val routine = s.settings.routines.firstOrNull { it.id == routineId } ?: return
        val activities = Planning.activitiesOn(routine, s.today)
        if (activities.isEmpty()) return
        val run = RoutineRunner.start(routine, activities, s.today.toString(), System.currentTimeMillis())
        saveDay(s.day.activate(run))
        _state.update { it.copy(marineMessage = null, celebration = null) }
        speaker.speak(activities.first().instruction)
    }

    /** « J'ai terminé » : seule façon de passer une activité au vert. */
    fun confirmCurrent() {
        val s = _state.value
        val run = s.activeRun ?: return
        val done = run.current ?: return
        val now = System.currentTimeMillis()
        val next = RoutineRunner.confirmCurrent(run, now)
        saveDay(s.day.withRun(next))
        _state.update { it.copy(celebration = Celebration(done, now + 2_500), marineMessage = null) }
        val nextActivity = next.current
        speaker.speak(
            if (nextActivity != null) "Bravo ! Une étoile de plus. ${nextActivity.instruction}"
            else "Bravo Marine ! Tu as tout terminé. Une étoile de plus pour ta semaine.",
        )
    }

    /** « J'ai besoin d'aide » : enregistré, ne valide jamais l'activité. */
    fun requestHelp() {
        val s = _state.value
        val run = s.activeRun ?: return
        if (run.isFinished) return
        saveDay(s.day.withRun(RoutineRunner.requestHelp(run, System.currentTimeMillis())))
        val message = "C'est noté. Tu peux aller voir ton parent pour qu'il t'aide."
        _state.update { it.copy(marineMessage = message) }
        speaker.speak(message)
    }

    fun repeatInstruction() {
        _state.value.activeRun?.current?.let { speaker.speak(it.instruction) }
    }

    // --- Actions Parent -----------------------------------------------------------------------

    fun acknowledgeHelp(routineId: String) = updateRun(routineId) { RoutineRunner.acknowledgeHelp(it) }

    fun pause(routineId: String) = updateRun(routineId) { RoutineRunner.pause(it, System.currentTimeMillis()) }

    fun resume(routineId: String) = updateRun(routineId) { RoutineRunner.resume(it, System.currentTimeMillis()) }

    /** Efface le déroulement du jour pour cette routine (elle pourra être relancée). */
    fun resetRun(routineId: String) {
        val s = _state.value
        val date = s.today.toString()
        val day = s.day.copy(runs = s.day.runs.filterNot { it.routineId == routineId && it.date == date })
        saveDay(if (s.day.activeRoutineId == routineId && s.day.activeDate == date) day.copy(activeRoutineId = null, activeDate = null) else day)
        speaker.stop()
    }

    fun updateSettings(transform: (Settings) -> Settings) {
        val updated = transform(_state.value.settings)
        store.saveSettings(updated)
        speaker.setRate(updated.speechRate)
        _state.update { it.copy(settings = updated) }
    }

    fun setRewardGiven(given: Boolean) {
        val s = _state.value
        saveDay(s.day.withRewardGiven(s.today, given))
    }

    fun setParentPin(pin: String?) = updateSettings { it.copy(parentPin = pin?.let(ParentPin::create)) }

    fun previewVoice(text: String) = speaker.speak(text)

    private fun updateRun(routineId: String, transform: (RoutineRun) -> RoutineRun) {
        val s = _state.value
        val run = s.day.runFor(routineId, s.today.toString()) ?: return
        saveDay(s.day.withRun(transform(run)))
    }

    private fun saveDay(day: DayState) {
        store.saveDayState(day)
        _state.update { it.copy(day = day) }
    }

    override fun onCleared() {
        speaker.shutdown()
    }
}
