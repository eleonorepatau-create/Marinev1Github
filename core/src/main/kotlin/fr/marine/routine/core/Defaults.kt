package fr.marine.routine.core

/**
 * Routine d'exemple construite à partir des informations fournies :
 * ordre du matin, douche au retour de l'IME, lavage des cheveux le mercredi
 * après-midi et le dimanche matin.
 *
 * Durées du matin fournies par le parent (réveil 10, habillage 10, petit
 * déjeuner 15, toilette 10, sac/chaussures/manteau 10) ; l'attente du taxi
 * (5 min) complète l'heure entre le lever et le taxi. Douche 15 min,
 * lavage des cheveux 10 min. Aucune heure de taxi n'est présumée : tant que le
 * parent ne l'a pas saisie, la routine du matin ne démarre pas toute seule.
 */
object Defaults {

    fun settings(): Settings = Settings(
        routines = listOf(morning(), afterIme(), wednesdayHair(), sundayHair()),
        taxi = TaxiSchedule(),
    )

    private fun morning() = Routine(
        id = "matin",
        name = "Routine du matin",
        anchor = Anchor.BeforeTaxi(DEFAULT_WAKE_UP_BEFORE_TAXI_MINUTES),
        days = SCHOOL_DAYS,
        activities = listOf(
            RoutineActivity("lever", "Je me réveille", "Bonjour Marine. C'est le moment de te réveiller.", "🛏️", 10),
            RoutineActivity("habillage", "Je m'habille", "Habille-toi.", "👕", 10),
            RoutineActivity("petit-dejeuner", "Je prends mon petit déjeuner", "C'est l'heure du petit déjeuner.", "🥣", 15),
            RoutineActivity(
                "toilette", "Dents, visage, crème, cheveux",
                "Brosse-toi les dents, lave-toi le visage, mets ta crème et coiffe-toi.", "🪥", 10,
            ),
            RoutineActivity(
                "depart", "Sac, chaussures, manteau",
                "Prends ton sac, mets tes chaussures et ton manteau.", "🎒", 10,
            ),
            RoutineActivity(
                "taxi", "Le taxi arrive bientôt", "Tout est prêt. Le taxi va arriver.", "🚕", 5,
                confirmLabel = "J'attends le taxi",
            ),
        ),
    )

    private fun afterIme() = Routine(
        id = "retour-ime",
        name = "Retour de l'IME",
        anchor = Anchor.Manual,
        days = SCHOOL_DAYS,
        activities = listOf(
            RoutineActivity("douche", "Je prends ma douche", "C'est le moment de prendre ta douche.", "🚿", 15),
        ),
    )

    private fun wednesdayHair() = Routine(
        id = "cheveux-mercredi",
        name = "Mercredi après-midi",
        anchor = Anchor.Manual,
        days = setOf(3),
        activities = listOf(
            RoutineActivity("cheveux-mer", "Je me lave les cheveux", "C'est le moment de te laver les cheveux.", "🧴", 10),
        ),
    )

    private fun sundayHair() = Routine(
        id = "cheveux-dimanche",
        name = "Dimanche matin",
        anchor = Anchor.Manual,
        days = setOf(7),
        activities = listOf(
            RoutineActivity("cheveux-dim", "Je me lave les cheveux", "C'est le moment de te laver les cheveux.", "🧴", 10),
        ),
    )
}
