package fr.marine.routine.core

import java.time.LocalDate
import java.time.LocalTime

/** Calculs de planning : quelles routines ont lieu un jour donné, et à quelle heure. */
object Planning {

    /** Heure du taxi pour [date] : l'exception datée prime sur le planning hebdomadaire. */
    fun taxiTime(schedule: TaxiSchedule, date: LocalDate): MinuteOfDay? {
        val key = date.toString()
        return if (schedule.overrides.containsKey(key)) {
            schedule.overrides[key]
        } else {
            schedule.weekly[date.dayOfWeek.value]
        }
    }

    /**
     * Fixe l'heure du taxi pour une seule date. Les autres jours ne changent pas.
     * [minuteOfDay] null = pas de taxi ce jour-là.
     */
    fun withTaxiOverride(schedule: TaxiSchedule, date: LocalDate, minuteOfDay: MinuteOfDay?): TaxiSchedule =
        schedule.copy(overrides = schedule.overrides + (date.toString() to minuteOfDay))

    /** Revient au planning hebdomadaire pour [date]. */
    fun withoutTaxiOverride(schedule: TaxiSchedule, date: LocalDate): TaxiSchedule =
        schedule.copy(overrides = schedule.overrides - date.toString())

    /** Supprime les exceptions passées (avant [today]) pour ne pas accumuler de données. */
    fun pruneOverrides(schedule: TaxiSchedule, today: LocalDate): TaxiSchedule =
        schedule.copy(overrides = schedule.overrides.filterKeys { LocalDate.parse(it) >= today })

    fun appliesOn(routine: Routine, date: LocalDate): Boolean {
        if (!routine.enabled) return false
        val oneOff = routine.oneOffDate
        return if (oneOff != null) {
            oneOff == date.toString()
        } else {
            date.dayOfWeek.value in routine.days
        }
    }

    /** Activités de la routine prévues ce jour-là, dans l'ordre défini par le parent. */
    fun activitiesOn(routine: Routine, date: LocalDate): List<RoutineActivity> =
        routine.activities.filter { a -> a.days == null || date.dayOfWeek.value in a.days }

    /**
     * Heure de début de la routine pour [date], ou null si elle est manuelle,
     * n'a pas lieu ce jour-là, ou si aucun taxi n'est prévu (routine liée au taxi).
     * Le lever est calculé uniquement à partir du taxi, jamais à partir des durées.
     */
    fun startTime(routine: Routine, settings: Settings, date: LocalDate): MinuteOfDay? {
        if (!appliesOn(routine, date)) return null
        return when (val anchor = routine.anchor) {
            is Anchor.BeforeTaxi -> {
                val taxi = taxiTime(settings.taxi, date) ?: return null
                (taxi - anchor.minutesBefore).takeIf { it >= 0 }
            }
            is Anchor.FixedTime -> anchor.minuteOfDay
            Anchor.Manual -> null
        }
    }

    fun format(minuteOfDay: MinuteOfDay): String =
        "%d h %02d".format(minuteOfDay / 60, minuteOfDay % 60)

    fun toLocalTime(minuteOfDay: MinuteOfDay): LocalTime = LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)

    fun clampMinute(value: Int): MinuteOfDay = ((value % 1440) + 1440) % 1440
}
