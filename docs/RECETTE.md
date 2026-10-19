# Recette du jalon 1

Légende : ✅ vérifié · ⏳ à faire sur téléphone · — hors jalon.

## Tests automatiques (`./gradlew :core:test`)

| Critère du brief | Test | État |
|---|---|---|
| Lever = taxi − 60 min | `lever = taxi moins 60 minutes` | ✅ |
| Le lever ne dépend pas des durées | `le lever ne depend pas des durees des activites` | ✅ |
| Changer le taxi d'un jour ne modifie pas les autres | `une exception de taxi ne modifie que sa date` | ✅ |
| Le rappel se produit au moment configuré (2 min par défaut, 1 min, aucun) | `rappel par defaut…`, `rappel reglable…`, `zero minute…` | ✅ |
| Durée inférieure au délai de rappel | `duree inferieure ou egale au delai - rappel a mi-duree` | ✅ |
| La barre représente le temps écoulé | `la barre represente le temps ecoule` | ✅ |
| Temps écoulé : pas de validation automatique | `temps ecoule - l activite reste affichee…` | ✅ |
| La confirmation passe l'activité au vert et permet l'étape suivante | `confirmation - activite terminee et etape suivante` | ✅ |
| La demande d'aide ne valide pas la tâche | `la demande d aide ne valide pas la tache` | ✅ |
| Pause | `la pause arrete le temps` | ✅ |
| Rappel / retard émis une seule fois | `rappel et retard important emis une seule fois` | ✅ |
| Reprise : enregistrement fidèle | `json aller-retour` | ✅ |

## Essais sur téléphone (à réaliser)

| # | Essai | Attendu | État |
|---|---|---|---|
| T1 | Saisir un taxi dans 61 min, laisser l'écran Marine ouvert | Au bout d'1 min : lever affiché et consigne lue sans toucher l'écran | ⏳ |
| T2 | Activité de 3 min, rappel 2 min | Rappel vocal après 1 min | ⏳ |
| T3 | Laisser passer le temps | Barre pleine, « Prends ton temps », activité non validée | ⏳ |
| T4 | « J'ai terminé » | Carte verte « Terminé », puis étape suivante lue ; vert dans Parent › Suivi | ⏳ |
| T5 | « J'ai besoin d'aide » | Message à Marine ; demande visible dans Parent › Suivi ; activité non validée | ⏳ |
| T6 | Fermer de force l'application en cours de routine puis la rouvrir | Reprise sur la même activité | ⏳ |
| T7 | Taxi modifié pour demain seulement | Lever de demain recalculé, autres jours inchangés | ⏳ |
| T8 | TalkBack activé | Boutons et barre annoncés clairement | ⏳ |
| T9 | Police système au maximum | Écran Marine lisible, défilement possible, rien de coupé | ⏳ |
| T10 | Mode silencieux / Ne pas déranger | Noter le comportement de la voix | ⏳ |
| T11 | Absence de voix française | Message explicatif dans Parent › Réglages | ⏳ |
| T12 | Code parent | Espace Parent inaccessible sans le code | ⏳ |
| T13 | Essai avec Marine | Ajustements selon son ressenti, sans supposer de besoins non exprimés | ⏳ |
| — | Notification parentale entre deux téléphones | Validée seulement après essai réel entre deux appareils | — |
