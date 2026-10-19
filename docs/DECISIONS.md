# Décisions et questions ouvertes

## Décisions appliquées dans le jalon 1

| # | Décision | Origine | Où |
|---|---|---|---|
| D1 | Application Android native Kotlin / Jetpack Compose. Le débat web/Android n'est pas rouvert. | Brief | `app/` |
| D2 | **Lever = heure du taxi − 60 min**, recalculé chaque jour. Il ne dépend jamais de la somme des durées. | Règle confirmée | `Planning.startTime` |
| D3 | Changer le taxi d'un jour crée une exception datée : les autres jours ne changent pas. « Pas de taxi ce jour » est possible. Les exceptions passées sont effacées. | Règle confirmée | `Planning.withTaxiOverride` |
| D4 | Aucune heure de taxi n'est présumée : sans heure saisie, la routine du matin ne démarre pas toute seule. | Pas d'heure fixe présumée | `Defaults` |
| D5 | Matin (durées fournies) : réveil 10 min, habillage 10, petit déjeuner 15, dents + visage + crème + coiffure 10, sac + chaussures + manteau 10 = 55 min. Une étape « J'attends le taxi » de 5 min complète l'heure jusqu'au taxi (à confirmer). Douche au retour de l'IME 15 min, lavage des cheveux 10 min. | Durées fournies | `Defaults` |
| D6 | Douche au retour de l'IME, cheveux mercredi et dimanche : **démarrage manuel** (heure de retour variable, non fournie). Le parent peut passer à une heure fixe. | Précisions de routine | `Defaults` |
| D7 | Rappel vocal par défaut 2 min avant la fin, réglable par activité de 0 (aucun) à 60 min. | Brief | `Timing.reminderAtMs` |
| D8 | **Durée ≤ délai de rappel** (ex. activité de 2 min, rappel à 2 min) : le rappel est donné **à mi-durée**, pour ne pas tomber en même temps que la consigne de début. | Brief : « définir et documenter » | `Timing.reminderAtMs` |
| D9 | Si l'application est rouverte après la fin du temps prévu, le rappel « il te reste… » n'est pas lu (il serait faux). | Cohérence | `RoutineRunner.dueEvents` |
| D10 | L'activité suivante commence au moment de la confirmation de la précédente (une activité à la fois), pas à une heure fixe. | Brief | `RoutineRunner.confirmCurrent` |
| D11 | Temps écoulé : barre pleine, message « Prends ton temps », aucune couleur rouge, aucun son d'alarme, aucune validation automatique. | Accessibilité, non anxiogène | `MarineScreen` |
| D12 | Retard important : par défaut 5 min après le temps prévu, réglable de 1 à 60 min. Signalé une seule fois par activité. | Brief (valeur provisoire) | `Settings.significantDelayMinutes` |
| D13 | Une modification de routine pendant son déroulement s'applique au prochain démarrage (copie des activités au démarrage). | Robustesse | `RoutineRun.activities` |
| D14 | Une routine programmée ne démarre seule que dans les 3 h suivant son heure, et une seule fois par jour. | Robustesse | `AutoStart` |
| D15 | Données uniquement sur le téléphone, sans sauvegarde en ligne ni transfert automatique ; aucune permission réseau. Déroulements conservés 7 jours. | Minimisation | Manifest, `DayState.prune` |
| D16 | Code parent facultatif, empreinte SHA-256 salée (10 000 itérations), jamais stocké en clair. | Protection des comptes | `ParentPin` |
| D17 | Pictogrammes provisoires sous forme d'emoji ; illustrations et avatar définitifs reportés. | Brief | `RoutineActivity.pictogram` |
| D18 | **Accompagnement, pas évaluation** : une étoile par activité confirmée « J'ai terminé », même après le temps prévu ; aucune étoile retirée ; un jour sans étoile reste neutre (ni croix ni rouge) ; la demande d'aide ne fait rien perdre. | Parent : « pas stressant » | `Rewards`, `WeekStars` |
| D19 | **Récompense de fin de semaine** choisie par le parent (texte + pictogramme), donnée le samedi par défaut (jour réglable). Par défaut **sans objectif** : toujours donnée. Un objectif d'étoiles est possible en option. Le parent marque « Récompense donnée ». | Parent | `RewardSettings` |
| D20 | Rappel vocal reformulé pour encourager : « Tu avances bien. Encore 2 minutes pour… ». Chaque confirmation : « Bravo ! Une étoile de plus. » | Parent : « pas stressant » | `MainViewModel` |

## Questions ouvertes (réponses attendues)

| Question | Bloque |
|---|---|
| Modèle et version Android du téléphone de Marine (téléphone absent pour l'instant) | Réglages batterie, lever application fermée, tests physiques |
| Garder ou non l'étape finale « J'attends le taxi » (5 min de marge) | Réglage (supprimable dans l'application) |
| Heure habituelle de retour de l'IME | Passage éventuel de la douche en heure fixe |
| Seuil de retard important et répétition des rappels en cas de dépassement | Valeur par défaut (5 min, sans répétition) |
| Comportement vocal en mode silencieux / Ne pas déranger | Jalon notifications |
| Mode d'association sécurisé des deux téléphones et hébergement | Synchronisation, alertes parent |
| Voix (vitesse, type) | Réglage (vitesse déjà réglable) |
| Quelle récompense, quel jour (samedi par défaut), avec ou sans objectif d'étoiles | Réglage dans Parent › Réglages |
