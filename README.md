# Marine – Ma routine (Android, jalon 1)

Application Android de routine accessible pour Marine, avec un espace Parent.
Ce dépôt contient le **premier jalon** : une démonstration locale, sur un seul
téléphone, de la routine paramétrable.

## Ce que fait ce jalon

| Fonction | État |
|---|---|
| Routine du matin : réveil 10 min, habillage 10, petit déjeuner 15, dents/visage/crème/cheveux 10, sac/chaussures/manteau 10, « J'attends le taxi » 5 | ✅ paramétrable |
| Lever calculé automatiquement : **heure du taxi − 60 min**, chaque jour | ✅ (testé) |
| Taxi modifié pour un jour seulement, sans toucher aux autres jours | ✅ (testé) |
| Douche au retour de l'IME, cheveux mercredi après-midi et dimanche matin, activités ponctuelles | ✅ démarrage manuel |
| Une activité à la fois, pictogramme, texte court | ✅ |
| Consigne vocale lue automatiquement au début de chaque activité | ✅ application ouverte |
| Réglette du temps restant, comme une clepsydre (sans heure à lire) | ✅ |
| En avance : « Bravo, tu as été rapide ! Tu as un peu d'avance. » | ✅ (testé) |
| Rappel vocal 2 min avant la fin (réglable par activité, 0 = aucun) | ✅ application ouverte |
| « J'ai terminé » obligatoire ; l'activité passe au vert (Marine et Parent) | ✅ (testé) |
| Temps écoulé : activité toujours affichée, jamais validée seule | ✅ (testé) |
| « J'ai besoin d'aide » distinct, ne valide pas l'activité | ✅ (testé) |
| Seuil de « retard important » réglable | ✅ affiché sur ce téléphone |
| Pause / reprise par le parent | ✅ |
| Étoiles de la semaine (une par activité confirmée, jamais retirées) et récompense de fin de semaine choisie par le parent | ✅ (testé) |
| Ton encourageant, sans rouge ni alarme, sans validation automatique | ✅ |
| Reprise après fermeture ou redémarrage de l'application | ✅ stockage local |
| Code parent (4 à 8 chiffres, stocké sous forme d'empreinte) | ✅ facultatif |
| Couleurs : violet pour Marine, vert pour Parent | ✅ |
| **Lever automatique application fermée / téléphone verrouillé** | ❌ jalon suivant |
| **Alerte sur le téléphone du parent, synchronisation entre deux téléphones** | ❌ non commencé |

Détails : [docs/LIMITES_V1.md](docs/LIMITES_V1.md).

## Installer la démonstration

Aucun code à manipuler :

1. Sur GitHub, ouvrir l'onglet **Actions** du dépôt, puis la dernière exécution
   réussie (coche verte) du flux « Android ».
2. En bas de la page, télécharger **marine-routine-debug-apk** (fichier zip
   contenant l'APK).
3. Copier l'APK sur le téléphone et l'ouvrir. Android demande d'autoriser
   l'installation d'applications de cette source.

Premier réglage : **Parent › Taxi** pour indiquer l'heure du taxi de chaque jour.
Tant qu'aucune heure n'est saisie, la routine du matin ne démarre pas seule
(elle peut être lancée à la main).

## Organisation du code

```
core/   Logique métier en Kotlin pur, testée sans téléphone
        Model.kt       activités, routines, taxi, réglages
        Planning.kt    jours, heure de début, lever = taxi − 60 min
        Timing.kt      barre temporelle, rappel, retard important
        RoutineRun.kt  déroulement : confirmation, aide, pause, événements
        DayState.kt    reprise, démarrage automatique
        Storage.kt     JSON local, code parent
app/    Application Android (Kotlin, Jetpack Compose)
        data/LocalStore.kt    fichiers privés, écriture atomique
        voice/Speaker.kt      synthèse vocale Android en français
        ui/                   écrans Marine et Parent
docs/   Décisions, limites, recette, documents d'origine
```

Architecture détaillée : [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
Décisions et questions ouvertes : [docs/DECISIONS.md](docs/DECISIONS.md).
Recette : [docs/RECETTE.md](docs/RECETTE.md).

## Pour les développeurs

```
./gradlew :core:test           # logique métier (aucun SDK Android nécessaire)
./gradlew :app:assembleDebug   # APK (SDK Android nécessaire : ANDROID_HOME ou local.properties)
```
