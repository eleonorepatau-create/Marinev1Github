package fr.marine.routine.core

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RewardTest {
    private val monday = LocalDate.of(2026, 10, 19)
    private val routine = Defaults.settings().routines.first { it.id == "matin" }

    private fun runOn(date: LocalDate, confirmed: Int): RoutineRun =
        (1..confirmed).fold(RoutineRunner.start(routine, routine.activities, date.toString(), 0)) { r, _ ->
            RoutineRunner.confirmCurrent(r, 1)
        }

    @Test
    fun `une etoile par activite confirmee, sur la semaine`() {
        val state = DayState()
            .withRun(runOn(monday, 6))
            .withRun(runOn(monday.plusDays(2), 3))
            .withRun(runOn(monday.minusDays(1), 6)) // semaine précédente
        assertEquals(6, Rewards.starsOn(state, monday))
        assertEquals(0, Rewards.starsOn(state, monday.plusDays(1)))
        assertEquals(9, Rewards.weekStars(state, monday.plusDays(6)))
    }

    @Test
    fun `une activite confirmee apres le temps prevu compte aussi`() {
        val late = RoutineRunner.confirmCurrent(RoutineRunner.start(routine, routine.activities, monday.toString(), 0), 60 * Timing.MINUTE_MS)
        assertEquals(1, Rewards.starsOn(DayState().withRun(late), monday))
    }

    @Test
    fun `la demande d aide ne retire pas d etoile`() {
        val run = RoutineRunner.requestHelp(runOn(monday, 2), 5)
        assertEquals(2, Rewards.starsOn(DayState().withRun(run), monday))
    }

    @Test
    fun `sans objectif la recompense est toujours acquise`() {
        assertTrue(Rewards.isEarned(RewardSettings(), 0))
        assertFalse(Rewards.isEarned(RewardSettings(starsGoal = 20), 19))
        assertTrue(Rewards.isEarned(RewardSettings(starsGoal = 20), 20))
    }

    @Test
    fun `jour de recompense et recompense donnee`() {
        val saturday = monday.plusDays(5)
        assertTrue(Rewards.isRewardDay(RewardSettings(), saturday))
        assertFalse(Rewards.isRewardDay(RewardSettings(enabled = false), saturday))
        val state = DayState().withRewardGiven(saturday, true)
        assertTrue(Rewards.isGiven(state, monday))
        assertFalse(Rewards.isGiven(state, monday.plusWeeks(1)))
    }

    @Test
    fun `le nettoyage garde toute la semaine en cours`() {
        val sunday = monday.plusDays(6)
        val state = DayState().withRun(runOn(monday, 6)).prune(sunday, keepDays = 1)
        assertEquals(6, Rewards.weekStars(state, sunday))
    }
}
