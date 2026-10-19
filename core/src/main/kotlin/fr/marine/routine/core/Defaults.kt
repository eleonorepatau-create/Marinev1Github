package fr.marine.routine.core

/**
 * Routine d'exemple construite à partir des informations fournies :
 * ordre du matin, douche au retour de l'IME, lavage des cheveux le mercredi
 * après-midi et le dimanche matin.
 *
 * Les DURÉES sont des valeurs d'exemple à régler par le parent : aucune durée
 * réelle n'a été fournie. Aucune heure de taxi n'est présumée : tant que le
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
            RoutineActivity("lever", "Je me lève", "Bonjour Marine. C'est le moment de te lever.", "🛏️", 10),
            RoutineActivity("habillage", "Je m'habille", "Habille-toi.", "👕", 15),
            RoutineActivity("petit-dejeuner", "Je prends mon petit déjeuner", "C'est l'heure du petit déjeuner.", "🥣", 20),
            RoutineActivity("dents", "Je me brosse les dents", "Brosse-toi les dents.", "🪥", 5),
            RoutineActivity("chaussures", "Je mets mes chaussures", "Mets tes chaussures.", "👟", 5),
            RoutineActivity("taxi", "Je pars avec le taxi", "Le taxi va arriver. Prépare-toi à partir.", "🚕", 5),
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
            RoutineActivity("cheveux-mer", "Je me lave les cheveux", "C'est le moment de te laver les cheveux.", "🧴", 15),
        ),
    )

    private fun sundayHair() = Routine(
        id = "cheveux-dimanche",
        name = "Dimanche matin",
        anchor = Anchor.Manual,
        days = setOf(7),
        activities = listOf(
            RoutineActivity("cheveux-dim", "Je me lave les cheveux", "C'est le moment de te laver les cheveux.", "🧴", 15),
        ),
    )
}
