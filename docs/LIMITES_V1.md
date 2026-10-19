# Limites du jalon 1 (à lire avant tout essai)

Ce jalon est une **démonstration locale sur un seul téléphone**. Voici ce qui
ne fonctionne pas encore, ou n'a pas encore été vérifié.

## Ne fonctionne pas encore

1. **Application fermée, en arrière-plan ou téléphone verrouillé** : le lever
   automatique et les consignes vocales n'ont lieu que si l'application est
   ouverte sur l'écran Marine (l'écran reste alors allumé). Si l'application
   est rouverte plus tard, la routine du jour démarre à ce moment-là (dans les
   3 h suivant l'heure prévue).
   *Jalon suivant* : alarme Android exacte (`AlarmManager`), notification
   plein écran et service de premier plan, avec demande des permissions
   correspondantes et réglages d'économie de batterie selon le modèle du
   téléphone de Marine.
2. **Alerte sur le téléphone du parent** : les demandes d'aide et les retards
   importants s'affichent uniquement dans l'espace Parent **du même
   téléphone**. Rien n'est envoyé ailleurs. Ils ne doivent pas être considérés
   comme opérationnels avant un essai réel entre deux téléphones.
3. **Synchronisation entre deux téléphones** et récupération des réglages
   Parent depuis un autre appareil : non commencées. Elles nécessitent un
   service distant sécurisé (comptes parent/enfant, association des
   appareils, chiffrement), dont l'hébergement reste à choisir.
4. **Notifications Android** : aucune dans ce jalon, donc aucune permission
   demandée.

## Non vérifié à ce jour

- **Aucun essai sur téléphone physique.** L'APK est compilé par
  l'intégration continue GitHub ; seule la logique métier (`core`) est
  couverte par des tests automatiques.
- Mode silencieux / Ne pas déranger : la voix utilise le volume « média » du
  téléphone ; à vérifier sur le téléphone de Marine.
- Présence d'une voix française : si elle manque, l'espace Parent l'indique
  (Réglages › Voix) et explique comment l'installer.
- Accessibilité avec TalkBack et grandes tailles de police : prévue dans la
  recette, pas encore testée.

## Reprise

L'état de la routine (activité en cours, activités confirmées, demandes
d'aide, pause) est enregistré à chaque action. Après fermeture forcée ou
redémarrage du téléphone, la routine reprend où elle en était à la
réouverture de l'application ; le temps écoulé est recalculé à partir de
l'horloge du téléphone.
