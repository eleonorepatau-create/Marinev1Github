package fr.marine.routine.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Jours ISO : 1 = lundi … 7 = dimanche. */
typealias IsoDay = Int

val ALL_DAYS: Set<IsoDay> = (1..7).toSet()
val SCHOOL_DAYS: Set<IsoDay> = (1..5).toSet()

/** Heure de la journée exprimée en minutes depuis minuit (0..1439). */
typealias MinuteOfDay = Int

const val DEFAULT_REMINDER_MINUTES = 2
const val DEFAULT_WAKE_UP_BEFORE_TAXI_MINUTES = 60
const val DEFAULT_SIGNIFICANT_DELAY_MINUTES = 5

@Serializable
data class RoutineActivity(
    val id: String,
    val name: String,
    /** Consigne lue à voix haute au début de l'activité. */
    val instruction: String,
    /** Pictogramme provisoire (emoji) ; illustrations définitives reportées. */
    val pictogram: String,
    val durationMinutes: Int,
    /** Rappel vocal « X minutes avant la fin » ; 0 = aucun rappel. */
    val reminderMinutesBefore: Int = DEFAULT_REMINDER_MINUTES,
    /** Jours où l'activité a lieu ; null = tous les jours de la routine. */
    val days: Set<IsoDay>? = null,
    /** Texte du grand bouton de confirmation ; null = « J'ai terminé ». */
    val confirmLabel: String? = null,
)

/** Ce qui déclenche le début d'une routine. */
@Serializable
sealed class Anchor {
    /** Début = heure du taxi du jour − [minutesBefore] (règle confirmée : lever = taxi − 60 min). */
    @Serializable
    @SerialName("taxi")
    data class BeforeTaxi(val minutesBefore: Int = DEFAULT_WAKE_UP_BEFORE_TAXI_MINUTES) : Anchor()

    @Serializable
    @SerialName("fixed")
    data class FixedTime(val minuteOfDay: MinuteOfDay) : Anchor()

    /** Démarrée à la main (heure variable, ex. retour de l'IME). */
    @Serializable
    @SerialName("manual")
    data object Manual : Anchor()
}

@Serializable
data class Routine(
    val id: String,
    val name: String,
    val anchor: Anchor,
    val days: Set<IsoDay>,
    /** Routine ponctuelle : n'a lieu qu'à cette date (AAAA-MM-JJ), [days] est alors ignoré. */
    val oneOffDate: String? = null,
    val activities: List<RoutineActivity>,
    val enabled: Boolean = true,
)

/**
 * Horaires du taxi. Le planning hebdomadaire sert par défaut ; une exception
 * datée ne modifie que ce jour-là (valeur null = pas de taxi ce jour-là).
 */
@Serializable
data class TaxiSchedule(
    val weekly: Map<IsoDay, MinuteOfDay> = emptyMap(),
    val overrides: Map<String, MinuteOfDay?> = emptyMap(),
)

@Serializable
data class Settings(
    val schemaVersion: Int = 1,
    val routines: List<Routine>,
    val taxi: TaxiSchedule = TaxiSchedule(),
    /** Minutes après la fin du temps prévu avant de signaler un « retard important ». */
    val significantDelayMinutes: Int = DEFAULT_SIGNIFICANT_DELAY_MINUTES,
    val speechRate: Float = 0.9f,
    /** Code parent (empreinte salée) ; null = pas de code. */
    val parentPin: PinHash? = null,
    val reward: RewardSettings = RewardSettings(),
)

@Serializable
data class PinHash(val salt: String, val hash: String)
