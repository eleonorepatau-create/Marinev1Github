# Projet Marine — Cahier des charges V1

## Objectif
Assistant de routine matinale accessible pour Marine, avec interface Android pour Marine et interface Android pour le parent.

## Missions spécialisées
1. Chef de projet : exigences, arbitrages, traçabilité des décisions.
2. Accessibilité : une action à la fois, pictogrammes, synthèse vocale, grands boutons, absence de lecture d'horloge.
3. Développement : interfaces Android, stockage, logique de progression, synchronisation future.
4. Notifications : rappels Android fiables et transmission sécurisée des demandes d'aide.
5. Qualité : tests sur deux appareils, permissions, accessibilité, protection des données.

## Fonctionnalités cibles
- Parent : éditer tâches, ordre, horaires, consignes, pictogrammes, jours et rappels.
- Marine : afficher la tâche courante, écouter la consigne, confirmer, demander de l'aide.
- Aucune tâche marquée terminée sans confirmation.
- Demandes d'aide et retards importants transmis au parent ; pas d'alerte pour chaque confirmation.
- Gestion des retards sans culpabilisation et avec possibilité de pause.
- Sécurisation des comptes et minimisation des données personnelles.

## Prototype livré
`index.html` : deux modes sur un seul navigateur, édition locale, synthèse vocale navigateur, progression et demande d'aide locale. Horaires indicatifs seulement.

## Limites importantes
Pas d'application Android installable, pas de notifications système, pas de synchronisation entre téléphones, pas de transmission d'alerte au parent. Les tâches ne démarrent pas automatiquement à l'heure programmée. Les informations sont conservées dans le stockage local du navigateur et peuvent être effacées.

## Critères d'acceptation de la future application
1. Déclenchement des consignes à l'heure prévue, même application fermée (selon autorisations Android et restrictions d'économie de batterie).
2. Confirmation manuelle obligatoire.
3. Demande d'aide visible rapidement sur le téléphone parental, avec indication des limites réseau.
4. Synchronisation des modifications entre les deux appareils.
5. Fonctionnement dégradé hors ligne, puis synchronisation à la reconnexion.
6. Tests avec Marine et ajustements selon son ressenti, sans supposer de besoins non exprimés.

## Décisions à valider
Heure réelle de départ ; ordre des tâches ; choix des pictogrammes ; fréquence et seuil des rappels ; type de son/voix ; autorisations de notifications ; choix d'hébergement sécurisé.
