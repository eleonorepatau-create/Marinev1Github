package fr.marine.routine.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoutineRunTest {
    private val min = Timing.MINUTE_MS
    private val routine = Defaults.settings().routines.first { it.id == "matin" }
    private fun newRun() = RoutineRunner.start(routine, routine.activities, "2026-10-19", 0)

    @Test
    fun `rappel par defaut deux minutes avant la fin`() {
        val a = RoutineActivity("a", "A", "A", "⭐", durationMinutes = 10)
        assertEquals(8 * min, Timing.reminderAtMs(a))
    }

    @Test
    fun `rappel reglable a une minute`() {
        val a = RoutineActivity("a", "A", "A", "⭐", durationMinutes = 10, reminderMinutesBefore = 1)
        assertEquals(9 * min, Timing.reminderAtMs(a))
    }

    @Test
    fun `duree inferieure ou egale au delai - rappel a mi-duree`() {
        val a = RoutineActivity("a", "A", "A", "⭐", durationMinutes = 2, reminderMinutesBefore = 2)
        assertEquals(1 * min, Timing.reminderAtMs(a))
        val b = RoutineActivity("b", "B", "B", "⭐", durationMinutes = 1, reminderMinutesBefore = 5)
        assertEquals(30_000L, Timing.reminderAtMs(b))
    }

    @Test
    fun `zero minute - aucun rappel`() {
        assertNull(Timing.reminderAtMs(RoutineActivity("a", "A", "A", "⭐", 10, reminderMinutesBefore = 0)))
    }

    @Test
    fun `la barre represente le temps ecoule`() {
        val run = newRun() // lever : 10 min
        assertEquals(0f, RoutineRunner.timing(run, 0, 5)!!.progress)
        assertEquals(0.5f, RoutineRunner.timing(run, 5 * min, 5)!!.progress)
        assertEquals(1f, RoutineRunner.timing(run, 30 * min, 5)!!.progress)
    }

    @Test
    fun `temps ecoule - l activite reste affichee et n est pas validee`() {
        val run = newRun()
        val t = RoutineRunner.timing(run, 11 * min, 5)!!
        assertEquals(TimePhase.TIME_UP, t.phase)
        assertEquals(0, run.currentIndex)
        assertFalse(run.isCompleted("lever"))
        assertEquals(TimePhase.SIGNIFICANT_DELAY, RoutineRunner.timing(run, 15 * min, 5)!!.phase)
    }

    @Test
    fun `confirmation - activite terminee et etape suivante`() {
        val run = RoutineRunner.confirmCurrent(newRun(), 3 * min)
        assertTrue(run.isCompleted("lever"))
        assertEquals("habillage", run.current?.id)
        assertEquals(0L, run.elapsedMs(3 * min))
    }

    @Test
    fun `la demande d aide ne valide pas la tache`() {
        val run = RoutineRunner.requestHelp(newRun(), 2 * min)
        assertEquals(0, run.currentIndex)
        assertFalse(run.isCompleted("lever"))
        assertEquals(1, run.pendingHelp.size)
        assertEquals("lever", run.pendingHelp.single().activityId)
        assertTrue(RoutineRunner.acknowledgeHelp(run).pendingHelp.isEmpty())
    }

    @Test
    fun `routine terminee apres toutes les confirmations`() {
        val done = routine.activities.fold(newRun()) { r, _ -> RoutineRunner.confirmCurrent(r, 1) }
        assertTrue(done.isFinished)
        assertEquals(routine.activities.size, done.completedAtMs.size)
        assertEquals(done, RoutineRunner.confirmCurrent(done, 2))
    }

    @Test
    fun `la pause arrete le temps`() {
        var run = RoutineRunner.pause(newRun(), 2 * min)
        assertEquals(2 * min, run.elapsedMs(10 * min))
        run = RoutineRunner.resume(run, 10 * min)
        assertEquals(3 * min, run.elapsedMs(11 * min))
        assertTrue(RoutineRunner.dueEvents(RoutineRunner.pause(run, 11 * min), 60 * min, 5).second.isEmpty())
    }

    @Test
    fun `rappel et retard important emis une seule fois`() {
        var run = newRun()
        var (r, events) = RoutineRunner.dueEvents(run, 7 * min, 5)
        assertTrue(events.isEmpty())
        run = r
        RoutineRunner.dueEvents(run, 8 * min, 5).let { (r2, e2) -> run = r2; events = e2 }
        assertEquals(listOf<RunEvent>(RunEvent.Reminder(routine.activities[0])), events)
        RoutineRunner.dueEvents(run, 9 * min, 5).let { (r2, e2) -> run = r2; events = e2 }
        assertTrue(events.isEmpty())
        RoutineRunner.dueEvents(run, 15 * min, 5).let { (r2, e2) -> run = r2; events = e2 }
        assertEquals(listOf<RunEvent>(RunEvent.SignificantDelay(routine.activities[0])), events)
        assertTrue(RoutineRunner.dueEvents(run, 20 * min, 5).second.isEmpty())
    }

    @Test
    fun `pas de rappel tardif si le temps est deja depasse`() {
        val (run, events) = RoutineRunner.dueEvents(newRun(), 11 * min, 5)
        assertTrue(events.isEmpty())
        assertTrue("lever" in run.remindedActivityIds)
    }

    @Test
    fun `json aller-retour`() {
        val settings = Defaults.settings().copy(
            taxi = TaxiSchedule(mapOf(1 to 495), mapOf("2026-10-20" to null)),
            parentPin = ParentPin.create("1234"),
        )
        assertEquals(settings, MarineJson.decodeSettings(MarineJson.encodeSettings(settings)))
        val state = DayState().activate(RoutineRunner.requestHelp(newRun(), 5))
        assertEquals(state, MarineJson.decodeDayState(MarineJson.encodeDayState(state)))
    }

    @Test
    fun `code parent`() {
        val pin = ParentPin.create("2468")
        assertTrue(ParentPin.matches(pin, "2468"))
        assertFalse(ParentPin.matches(pin, "1357"))
        assertFalse(ParentPin.isValidFormat("12a4"))
        assertTrue(ParentPin.isValidFormat("1234"))
    }
}

class AdvanceTest {
    private val min = Timing.MINUTE_MS
    private val routine = Defaults.settings().routines.first { it.id == "matin" } // 10, 10, 15…
    private fun newRun() = RoutineRunner.start(routine, routine.activities, "2026-10-19", 0)

    @Test
    fun `plus rapide que prevu - felicitation et avance`() {
        val run = newRun()
        assertEquals(ConfirmFeedback.FASTER_AND_AHEAD, RoutineRunner.feedback(run, 7 * min))
        assertEquals(3 * min, RoutineRunner.confirmCurrent(run, 7 * min).advanceMs)
    }

    @Test
    fun `a peine plus rapide - felicitation sans annoncer d avance`() {
        assertEquals(ConfirmFeedback.FASTER, RoutineRunner.feedback(newRun(), 9 * min + 30_000))
    }

    @Test
    fun `apres le temps prevu - simple bravo, jamais de retard annonce`() {
        assertEquals(ConfirmFeedback.WELL_DONE, RoutineRunner.feedback(newRun(), 12 * min))
    }

    @Test
    fun `l avance est cumulee sur la routine`() {
        // Lever en 13 min (3 min de plus), habillage en 8 min (2 min de moins) : pas d'avance globale.
        val afterWake = RoutineRunner.confirmCurrent(newRun(), 13 * min)
        assertEquals(ConfirmFeedback.FASTER, RoutineRunner.feedback(afterWake, 21 * min))
        // Habillage en 5 min : 5 − 3 = 2 min d'avance.
        assertEquals(ConfirmFeedback.FASTER_AND_AHEAD, RoutineRunner.feedback(afterWake, 18 * min))
    }

    @Test
    fun `la pause ne compte pas dans le temps`() {
        val paused = RoutineRunner.resume(RoutineRunner.pause(newRun(), 2 * min), 12 * min)
        assertEquals(ConfirmFeedback.FASTER_AND_AHEAD, RoutineRunner.feedback(paused, 15 * min))
    }

    @Test
    fun `clepsydre - temps restant`() {
        val t = RoutineRunner.timing(newRun(), 5 * min, 5)!!
        assertEquals(0.5f, t.remaining)
        assertEquals(0f, RoutineRunner.timing(newRun(), 20 * min, 5)!!.remaining)
    }
}
