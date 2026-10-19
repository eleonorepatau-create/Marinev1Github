package fr.marine.routine.core

/** Phase de l'activité en cours, du point de vue du temps (jamais une validation). */
enum class TimePhase {
    /** Temps prévu non écoulé. */
    IN_PROGRESS,

    /** Temps prévu écoulé : l'activité reste affichée, elle n'est pas validée. */
    TIME_UP,

    /** Dépassement au-delà du seuil de « retard important » réglé par le parent. */
    SIGNIFICANT_DELAY,
}

data class ActivityTiming(
    val elapsedMs: Long,
    val durationMs: Long,
    /** 0.0 → 1.0, plafonné à 1.0 une fois le temps écoulé. */
    val progress: Float,
    val phase: TimePhase,
)

object Timing {

    /**
     * Moment du rappel vocal, en millisecondes après le début de l'activité,
     * ou null s'il n'y a pas de rappel.
     *
     * Règle :
     * - par défaut le rappel a lieu [RoutineActivity.reminderMinutesBefore] minutes avant la fin ;
     * - 0 minute = pas de rappel ;
     * - si la durée est inférieure ou égale au délai choisi (ex. activité de 2 min avec
     *   rappel à 2 min), le rappel est donné à mi-durée, pour qu'il ne tombe jamais
     *   au moment même où la consigne de début est lue.
     */
    fun reminderAtMs(activity: RoutineActivity): Long? {
        val before = activity.reminderMinutesBefore
        if (before <= 0 || activity.durationMinutes <= 0) return null
        val durationMs = activity.durationMinutes * MINUTE_MS
        return if (before < activity.durationMinutes) {
            durationMs - before * MINUTE_MS
        } else {
            durationMs / 2
        }
    }

    fun timing(activity: RoutineActivity, elapsedMs: Long, significantDelayMinutes: Int): ActivityTiming {
        val durationMs = activity.durationMinutes.coerceAtLeast(1) * MINUTE_MS
        val elapsed = elapsedMs.coerceAtLeast(0)
        val phase = when {
            elapsed < durationMs -> TimePhase.IN_PROGRESS
            elapsed < durationMs + significantDelayMinutes.coerceAtLeast(0) * MINUTE_MS -> TimePhase.TIME_UP
            else -> TimePhase.SIGNIFICANT_DELAY
        }
        val progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
        return ActivityTiming(elapsed, durationMs, progress, phase)
    }

    const val MINUTE_MS = 60_000L
}
