# Marine — Brief de développement Android V1

## Objectif
Développer une application Android accessible de routine matinale, avec deux rôles distincts : Marine et Parent. Partir du prototype `index.html` pour comprendre le parcours, sans considérer son HTML comme une architecture de production.

## Périmètre validé
- Une routine du matin, activités configurables par le parent (ordre, jours, heure, durée, texte et consigne orale).
- Sur le téléphone de Marine : une activité principale à la fois, pictogramme/image, lecture vocale automatique au début de l'activité, barre de progression du temps, grand bouton « J'ai terminé » et bouton « J'ai besoin d'aide ».
- Rappel vocal automatique par défaut **2 minutes avant la fin**, réglable par activité à 1, 2 ou autre nombre de minutes. Si la durée est inférieure au délai choisi, définir et documenter le comportement.
- Une activité ne devient « terminée » que par confirmation explicite ; elle passe alors au vert, dans l'interface Marine et dans le suivi parent.
- Si le temps est écoulé, conserver l'activité affichée, sans la valider automatiquement. Prévoir un seuil configurable de « retard important » pour l'alerte parent.
- Parent : gestion des activités et consultation des états ; notification uniquement en cas de demande d'aide ou de retard important.
- Couleurs : violet pour Marine, vert pour Parent. Avatar et illustrations définitifs reportés à plus tard.

## Architecture et exigences
- Proposer une architecture Android native Kotlin/Jetpack Compose ou une alternative argumentée. Séparer interface, planification, synthèse vocale, stockage, synchronisation et notifications.
- La synchronisation entre deux appareils nécessite un service distant sécurisé ; ne pas présenter un prototype local comme synchronisé.
- Notifications Android : demander les permissions pertinentes, expliquer les limites de lecture vocale automatique quand l'application est fermée, en arrière-plan ou lorsque l'appareil est verrouillé ; tester sur appareils physiques.
- Prévoir le mode hors ligne pour la routine locale et la reprise après redémarrage. Éviter de dépendre uniquement d'une page web en arrière-plan.
- Minimiser les données personnelles, protéger les comptes parent/enfant, ne pas envoyer de photos à un service tiers sans accord.
- Accessibilité : grandes zones tactiles, contraste, pas d'obligation de lire l'heure, messages courts, options de répétition de consigne, absence de mécanismes anxiogènes.

## Répartition des rôles
1. Coordination : traçabilité des exigences et jalons.
2. Accessibilité : parcours utilisateur et critères de lisibilité.
3. Développement : implémentation et stockage.
4. Notifications : planification, TTS et synchronisation.
5. Qualité : tests unitaires et sur deux téléphones Android.

## Premier jalon Codex
Créer le squelette Android et une démonstration locale installable : routine paramétrable sur un appareil, TTS au début de chaque activité lorsque l'app est au premier plan, progression temporelle, confirmation et état vert, demande d'aide visible. Documenter clairement les fonctions qui ne marchent pas encore en arrière-plan et la synchronisation à venir. Ne pas prétendre que les alertes parentales sont opérationnelles avant un test réel entre deux téléphones.

## Questions encore ouvertes
- Heure de départ, activités et durées réelles du matin.
- Seuil et répétition des rappels en cas de dépassement.
- Mode d'association sécurisé des deux téléphones.
- Comportement vocal en mode silencieux / ne pas déranger.

## Critères de recette
- L'activité démarre avec une consigne orale sans toucher l'écran (application ouverte).
- Le rappel se produit au moment configuré.
- La barre représente correctement le temps écoulé.
- La confirmation passe l'activité au vert et permet l'étape suivante.
- La demande d'aide ne valide pas la tâche.
- La notification parentale ne sera validée qu'après essai réel entre deux appareils.
