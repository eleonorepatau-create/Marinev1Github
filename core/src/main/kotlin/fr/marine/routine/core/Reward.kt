package fr.marine.routine.core

import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Récompense de fin de semaine, pensée comme un accompagnement et non une
 * évaluation :
 * - une étoile par activité confirmée « J'ai terminé », même après le temps prévu ;
 * - aucune étoile n'est retirée, aucun jour n'est marqué comme « raté » ;
 * - par défaut, pas d'objectif : la récompense est donnée en fin de semaine.
 *   Le parent peut fixer un nombre d'étoiles s'il le souhaite.
 */
@Serializable
data class RewardSettings(
    val enabled: Boolean = true,
    /** Ce que Marine reçoit, choisi par le parent (ex. « Sortie au parc »). */
    val description: String = "",
    val pictogram: String = "🎁",
    /** Jour où la récompense est donnée (ISO, 6 = samedi par défaut). */
    val day: IsoDay = 6,
    /** null = pas d'objectif, récompense toujours donnée. */
    val starsGoal: Int? = null,
)

object Rewards {

    /** Lundi de la semaine de [date] : identifiant de la semaine. */
    fun weekStart(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun weekDays(date: LocalDate): List<LocalDate> = weekStart(date).let { start -> (0L..6L).map(start::plusDays) }

    /** Étoiles gagnées un jour donné (toutes routines confondues). */
    fun starsOn(state: DayState, date: LocalDate): Int =
        state.runs.filter { it.date == date.toString() }.sumOf { it.completedAtMs.size }

    fun weekStars(state: DayState, date: LocalDate): Int = weekDays(date).sumOf { starsOn(state, it) }

    fun isRewardDay(reward: RewardSettings, date: LocalDate): Boolean =
        reward.enabled && date.dayOfWeek.value == reward.day

    /** La récompense est-elle acquise ? Toujours vrai sans objectif. */
    fun isEarned(reward: RewardSettings, stars: Int): Boolean = reward.starsGoal == null || stars >= reward.starsGoal

    fun isGiven(state: DayState, date: LocalDate): Boolean = weekStart(date).toString() in state.rewardsGivenWeeks
}
