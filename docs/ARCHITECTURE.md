# Architecture

## Découpage

```
┌──────────────────────────── app (Android) ────────────────────────────┐
│ ui/        MarineScreen · ParentScreen · App (navigation) · Theme    │
│ ui/        MainViewModel : horloge 1 s, actions, voix, sauvegarde     │
│ voice/     Speaker : TextToSpeech, français, vitesse réglable         │
│ data/      LocalStore : settings.json, day_state.json (AtomicFile)    │
└───────────────────────────────┬───────────────────────────────────────┘
                                │ dépend de
┌──────────────────────────── core (Kotlin pur) ────────────────────────┐
│ Model       Routine, RoutineActivity, Anchor, TaxiSchedule, Settings  │
│ Planning    jours, exceptions de taxi, heure de début                 │
│ Timing      progression, phases, moment du rappel                     │
│ RoutineRun  déroulement et transitions pures                          │
│ DayState    déroulements enregistrés, démarrage automatique           │
│ Storage     JSON, code parent                                         │
└───────────────────────────────────────────────────────────────────────┘
```

La logique qui décide **quand** et **si** une activité est terminée est
entièrement dans `core`, sans dépendance Android : elle est testée par
`./gradlew :core:test` (26 tests).

## Séparation demandée par le brief

| Responsabilité | Jalon 1 | Jalon suivant |
|---|---|---|
| Interface | `app/ui` | Illustrations, TalkBack vérifié |
| Planification | `core/Planning`, `core/DayState` (application ouverte) | `AlarmManager` + récepteur de démarrage |
| Synthèse vocale | `app/voice/Speaker` | Service de premier plan pour la voix écran verrouillé |
| Stockage | `app/data/LocalStore` + `core/Storage` | Migration de schéma (`schemaVersion`) |
| Synchronisation | — | Module `sync` : service distant sécurisé, file d'attente hors ligne |
| Notifications | — | Module `notify` : canal « aide », canal « retard », permission `POST_NOTIFICATIONS` |

## Ancrage des routines

- `Anchor.BeforeTaxi(60)` : début = taxi du jour − 60 min (routine du matin).
- `Anchor.FixedTime(minute)` : heure fixe choisie par le parent.
- `Anchor.Manual` : démarrée par un bouton (retour de l'IME, cheveux, ponctuel).

## Choix techniques

- Kotlin 2.1, Jetpack Compose (Material 3), minSdk 26 (Android 8), targetSdk 35.
- Pas de base de données : deux petits fichiers JSON suffisent à ce stade.
- Aucune bibliothèque réseau, aucun service tiers, aucune permission.
