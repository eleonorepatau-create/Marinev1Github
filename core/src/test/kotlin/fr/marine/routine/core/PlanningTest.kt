package fr.marine.routine.core

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlanningTest {
    private val monday = LocalDate.of(2026, 10, 19)
    private val tuesday = monday.plusDays(1)
    private val wednesday = monday.plusDays(2)
    private val sunday = monday.plusDays(6)

    private fun settingsWithTaxi(vararg weekly: Pair<Int, Int>) =
        Defaults.settings().copy(taxi = TaxiSchedule(weekly = weekly.toMap()))

    private val morning get() = Defaults.settings().routines.first { it.id == "matin" }

    @Test
    fun `lever = taxi moins 60 minutes`() {
        val settings = settingsWithTaxi(1 to 8 * 60 + 15)
        assertEquals(7 * 60 + 15, Planning.startTime(morning, settings, monday))
    }

    @Test
    fun `le lever ne depend pas des durees des activites`() {
        val longer = morning.copy(activities = morning.activities.map { it.copy(durationMinutes = 45) })
        val settings = settingsWithTaxi(1 to 8 * 60 + 15).copy(routines = listOf(longer))
        assertEquals(7 * 60 + 15, Planning.startTime(longer, settings, monday))
    }

    @Test
    fun `sans heure de taxi la routine du matin ne demarre pas`() {
        assertNull(Planning.startTime(morning, Defaults.settings(), monday))
    }

    @Test
    fun `une exception de taxi ne modifie que sa date`() {
        val base = TaxiSchedule(weekly = mapOf(1 to 8 * 60 + 15, 2 to 8 * 60 + 15))
        val changed = Planning.withTaxiOverride(base, monday, 9 * 60)
        assertEquals(9 * 60, Planning.taxiTime(changed, monday))
        assertEquals(8 * 60 + 15, Planning.taxiTime(changed, tuesday))
        assertEquals(8 * 60 + 15, Planning.taxiTime(changed, monday.plusWeeks(1)))
        val settings = Defaults.settings().copy(taxi = changed)
        assertEquals(8 * 60, Planning.startTime(morning, settings, monday))
        assertEquals(7 * 60 + 15, Planning.startTime(morning, settings, tuesday))
    }

    @Test
    fun `exception sans taxi supprime le lever automatique ce jour seulement`() {
        val base = TaxiSchedule(weekly = mapOf(1 to 500, 2 to 500))
        val settings = Defaults.settings().copy(taxi = Planning.withTaxiOverride(base, monday, null))
        assertNull(Planning.startTime(morning, settings, monday))
        assertEquals(440, Planning.startTime(morning, settings, tuesday))
        val restored = settings.copy(taxi = Planning.withoutTaxiOverride(settings.taxi, monday))
        assertEquals(440, Planning.startTime(morning, restored, monday))
    }

    @Test
    fun `routines des cheveux le mercredi et le dimanche seulement`() {
        val routines = Defaults.settings().routines
        val wed = routines.first { it.id == "cheveux-mercredi" }
        val sun = routines.first { it.id == "cheveux-dimanche" }
        assertTrue(Planning.appliesOn(wed, wednesday))
        assertTrue(!Planning.appliesOn(wed, monday))
        assertTrue(Planning.appliesOn(sun, sunday))
        assertTrue(!Planning.appliesOn(sun, wednesday))
    }

    @Test
    fun `routine ponctuelle seulement a sa date`() {
        val oneOff = morning.copy(id = "rdv", oneOffDate = tuesday.toString(), days = emptySet(), anchor = Anchor.Manual)
        assertTrue(Planning.appliesOn(oneOff, tuesday))
        assertTrue(!Planning.appliesOn(oneOff, monday))
    }

    @Test
    fun `activite limitee a certains jours`() {
        val r = morning.copy(activities = morning.activities + RoutineActivity("x", "X", "X", "⭐", 5, days = setOf(3)))
        assertEquals(6, Planning.activitiesOn(r, monday).size)
        assertEquals(7, Planning.activitiesOn(r, wednesday).size)
    }

    @Test
    fun `nettoyage des exceptions passees`() {
        val s = TaxiSchedule(overrides = mapOf(monday.toString() to 500, tuesday.toString() to 510))
        assertEquals(setOf(tuesday.toString()), Planning.pruneOverrides(s, tuesday).overrides.keys)
    }

    @Test
    fun `demarrage automatique a l heure du lever uniquement`() {
        val settings = settingsWithTaxi(1 to 8 * 60 + 15)
        val state = DayState()
        assertNull(AutoStart.routineToStart(settings, state, monday, 7 * 60 + 14))
        assertEquals("matin", AutoStart.routineToStart(settings, state, monday, 7 * 60 + 15)?.id)
        // Les routines manuelles ne démarrent jamais seules.
        assertNull(AutoStart.routineToStart(Defaults.settings(), state, monday, 18 * 60))
    }

    @Test
    fun `pas de second demarrage le meme jour`() {
        val settings = settingsWithTaxi(1 to 8 * 60 + 15)
        val run = RoutineRunner.start(morning, morning.activities, monday.toString(), 0)
        val finished = morning.activities.fold(run) { r, _ -> RoutineRunner.confirmCurrent(r, 1) }
        val state = DayState().activate(finished)
        assertNull(AutoStart.routineToStart(settings, state, monday, 7 * 60 + 20))
    }

    @Test
    fun `une routine inachevee la veille ne bloque pas le matin`() {
        val settings = settingsWithTaxi(1 to 8 * 60 + 15)
        val yesterday = RoutineRunner.start(morning, morning.activities, monday.minusDays(1).toString(), 0)
        val state = DayState().activate(yesterday)
        assertEquals("matin", AutoStart.routineToStart(settings, state, monday, 7 * 60 + 15)?.id)
    }
}
