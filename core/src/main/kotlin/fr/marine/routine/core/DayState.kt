package fr.marine.routine.core

import kotlinx.serialization.Serializable
import java.time.LocalDate

/** Déroulements enregistrés sur l'appareil (pour la reprise après redémarrage). */
@Serializable
data class DayState(
    val runs: List<RoutineRun> = emptyList(),
    /** Routine actuellement affichée chez Marine (routineId), pour la date [activeDate]. */
    val activeRoutineId: String? = null,
    val activeDate: String? = null,
    /** Semaines (lundi, AAAA-MM-JJ) dont la récompense a été donnée. */
    val rewardsGivenWeeks: Set<String> = emptySet(),
) {
    fun runFor(routineId: String, date: String): RoutineRun? =
        runs.firstOrNull { it.routineId == routineId && it.date == date }

    val activeRun: RoutineRun?
        get() {
            val id = activeRoutineId ?: return null
            val date = activeDate ?: return null
            return runFor(id, date)
        }

    fun withRun(run: RoutineRun): DayState =
        copy(runs = runs.filterNot { it.routineId == run.routineId && it.date == run.date } + run)

    fun activate(run: RoutineRun): DayState =
        withRun(run).copy(activeRoutineId = run.routineId, activeDate = run.date)

    /**
     * Ne garde que les déroulements récents (minimisation des données),
     * et toujours ceux de la semaine en cours (étoiles de la récompense).
     */
    fun prune(today: LocalDate, keepDays: Long = 7): DayState {
        val limit = minOf(today.minusDays(keepDays), Rewards.weekStart(today))
        val kept = runs.filter { LocalDate.parse(it.date) >= limit }
        val weeks = rewardsGivenWeeks.filter { LocalDate.parse(it) >= Rewards.weekStart(today).minusWeeks(4) }.toSet()
        val activeStillThere = kept.any { it.routineId == activeRoutineId && it.date == activeDate }
        return if (activeStillThere) {
            copy(runs = kept, rewardsGivenWeeks = weeks)
        } else {
            DayState(runs = kept, rewardsGivenWeeks = weeks)
        }
    }

    fun withRewardGiven(date: LocalDate, given: Boolean): DayState {
        val week = Rewards.weekStart(date).toString()
        return copy(rewardsGivenWeeks = if (given) rewardsGivenWeeks + week else rewardsGivenWeeks - week)
    }
}

object AutoStart {
    /** Une routine programmée ne démarre automatiquement que dans cette fenêtre après son heure. */
    const val WINDOW_MINUTES = 180

    /**
     * Routine programmée à démarrer maintenant, ou null.
     * Ne démarre rien si une routine est déjà en cours (non terminée) chez Marine,
     * ni une routine déjà démarrée ce jour-là. Une routine d'un jour précédent
     * restée inachevée ne bloque pas celle du jour.
     */
    fun routineToStart(settings: Settings, state: DayState, date: LocalDate, nowMinute: MinuteOfDay): Routine? {
        val active = state.activeRun
        if (active != null && active.date == date.toString() && !active.isFinished) return null
        return settings.routines
            .mapNotNull { r -> Planning.startTime(r, settings, date)?.let { r to it } }
            .filter { (r, start) ->
                nowMinute >= start &&
                    nowMinute < start + WINDOW_MINUTES &&
                    state.runFor(r.id, date.toString()) == null &&
                    Planning.activitiesOn(r, date).isNotEmpty()
            }
            .minByOrNull { (_, start) -> start }
            ?.first
    }
}
