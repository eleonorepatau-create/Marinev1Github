package fr.marine.routine.core

import kotlinx.serialization.Serializable

@Serializable
data class HelpRequest(
    val activityId: String,
    val atMs: Long,
    val acknowledged: Boolean = false,
)

/**
 * Déroulement d'une routine pour une date. Les activités sont copiées au
 * démarrage : une modification faite par le parent pendant la routine
 * s'appliquera à la prochaine occurrence, sans perturber celle en cours.
 */
@Serializable
data class RoutineRun(
    val routineId: String,
    val routineName: String,
    val date: String,
    val activities: List<RoutineActivity>,
    val currentIndex: Int = 0,
    val currentStartedAtMs: Long,
    val pausedAtMs: Long? = null,
    val pausedTotalMs: Long = 0,
    /** Uniquement les activités confirmées par « J'ai terminé ». */
    val completedAtMs: Map<String, Long> = emptyMap(),
    val helpRequests: List<HelpRequest> = emptyList(),
    val remindedActivityIds: Set<String> = emptySet(),
    val delaySignaledActivityIds: Set<String> = emptySet(),
    /**
     * Avance cumulée depuis le début de la routine (temps prévu − temps réel,
     * pauses exclues). Positive = Marine a de l'avance. Sert uniquement à
     * féliciter : un retard n'est jamais annoncé à Marine.
     */
    val advanceMs: Long = 0,
) {
    val isFinished: Boolean get() = currentIndex >= activities.size
    val current: RoutineActivity? get() = activities.getOrNull(currentIndex)
    val isPaused: Boolean get() = pausedAtMs != null

    fun isCompleted(activityId: String): Boolean = completedAtMs.containsKey(activityId)

    fun elapsedMs(nowMs: Long): Long {
        val pausedNow = pausedAtMs?.let { nowMs - it } ?: 0
        return (nowMs - currentStartedAtMs - pausedTotalMs - pausedNow).coerceAtLeast(0)
    }

    val pendingHelp: List<HelpRequest> get() = helpRequests.filter { !it.acknowledged }
}

/** Retour donné à Marine après « J'ai terminé » : toujours positif. */
enum class ConfirmFeedback {
    /** Plus rapide que prévu, et de l'avance sur l'ensemble de la routine. */
    FASTER_AND_AHEAD,

    /** Plus rapide que prévu pour cette activité. */
    FASTER,

    /** Simple félicitation. */
    WELL_DONE,
}

/** Événement à traiter par l'interface (voix, alerte locale). */
sealed class RunEvent {
    data class Reminder(val activity: RoutineActivity) : RunEvent()
    data class SignificantDelay(val activity: RoutineActivity) : RunEvent()
}

/** Transitions de la routine. Toutes les fonctions sont pures. */
object RoutineRunner {

    fun start(routine: Routine, activities: List<RoutineActivity>, date: String, nowMs: Long): RoutineRun =
        RoutineRun(
            routineId = routine.id,
            routineName = routine.name,
            date = date,
            activities = activities,
            currentStartedAtMs = nowMs,
        )

    /** Confirmation explicite « J'ai terminé » : seule façon de terminer une activité. */
    fun confirmCurrent(run: RoutineRun, nowMs: Long): RoutineRun {
        val current = run.current ?: return run
        val gained = current.durationMinutes * Timing.MINUTE_MS - run.elapsedMs(nowMs)
        return run.copy(
            advanceMs = run.advanceMs + gained,
            completedAtMs = run.completedAtMs + (current.id to nowMs),
            currentIndex = run.currentIndex + 1,
            currentStartedAtMs = nowMs,
            pausedAtMs = null,
            pausedTotalMs = 0,
        )
    }

    /**
     * Félicitation à donner pour la confirmation de [before] à [nowMs].
     * Une avance n'est annoncée qu'à partir d'une minute, pour rester vraie.
     */
    fun feedback(before: RoutineRun, nowMs: Long): ConfirmFeedback {
        val current = before.current ?: return ConfirmFeedback.WELL_DONE
        val faster = before.elapsedMs(nowMs) < current.durationMinutes * Timing.MINUTE_MS
        val after = confirmCurrent(before, nowMs)
        return when {
            faster && after.advanceMs >= Timing.MINUTE_MS -> ConfirmFeedback.FASTER_AND_AHEAD
            faster -> ConfirmFeedback.FASTER
            else -> ConfirmFeedback.WELL_DONE
        }
    }

    /** Demande d'aide : enregistrée, mais ne valide jamais l'activité. */
    fun requestHelp(run: RoutineRun, nowMs: Long): RoutineRun {
        val current = run.current ?: return run
        return run.copy(helpRequests = run.helpRequests + HelpRequest(current.id, nowMs))
    }

    fun acknowledgeHelp(run: RoutineRun): RoutineRun =
        run.copy(helpRequests = run.helpRequests.map { it.copy(acknowledged = true) })

    fun pause(run: RoutineRun, nowMs: Long): RoutineRun =
        if (run.isPaused || run.isFinished) run else run.copy(pausedAtMs = nowMs)

    fun resume(run: RoutineRun, nowMs: Long): RoutineRun {
        val pausedAt = run.pausedAtMs ?: return run
        return run.copy(pausedAtMs = null, pausedTotalMs = run.pausedTotalMs + (nowMs - pausedAt).coerceAtLeast(0))
    }

    fun timing(run: RoutineRun, nowMs: Long, significantDelayMinutes: Int): ActivityTiming? {
        val current = run.current ?: return null
        return Timing.timing(current, run.elapsedMs(nowMs), significantDelayMinutes)
    }

    /**
     * Événements devenus dus à [nowMs] (rappel, retard important), chacun une
     * seule fois par activité. Retourne la routine mise à jour et les événements.
     * Aucun événement n'est émis pendant une pause.
     */
    fun dueEvents(run: RoutineRun, nowMs: Long, significantDelayMinutes: Int): Pair<RoutineRun, List<RunEvent>> {
        val current = run.current ?: return run to emptyList()
        if (run.isPaused) return run to emptyList()
        val elapsed = run.elapsedMs(nowMs)
        var updated = run
        val events = mutableListOf<RunEvent>()

        val reminderAt = Timing.reminderAtMs(current)
        if (reminderAt != null && elapsed >= reminderAt && current.id !in run.remindedActivityIds) {
            updated = updated.copy(remindedActivityIds = updated.remindedActivityIds + current.id)
            // Pas de rappel « il reste du temps » si le temps est déjà largement dépassé
            // (ex. application rouverte tard) : il serait faux.
            if (elapsed < current.durationMinutes * Timing.MINUTE_MS) {
                events += RunEvent.Reminder(current)
            }
        }

        val phase = Timing.timing(current, elapsed, significantDelayMinutes).phase
        if (phase == TimePhase.SIGNIFICANT_DELAY && current.id !in run.delaySignaledActivityIds) {
            updated = updated.copy(delaySignaledActivityIds = updated.delaySignaledActivityIds + current.id)
            events += RunEvent.SignificantDelay(current)
        }
        return updated to events
    }
}
