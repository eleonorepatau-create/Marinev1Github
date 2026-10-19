# Outil Marine V1 — organisation des cinq agents

Les cinq agents sont chargés de développer l’outil, sans GitHub, de la coordination aux tests. Ils doivent poser les questions utiles au lieu de supposer les besoins. Le débat web/Android n’est pas rouvert.

## Agents effectivement créés et missions

| Agent | Mission | Livrable organisé |
|---|---|---|
| Chef de projet et coordination | Périmètre, jalons, dépendances, décisions et synthèse | Plan de coordination |
| Ergonomie et accessibilité | Parcours, libellés et critères d'accessibilité | Parcours et critères de recette |
| Développement Android | Moteur, stockage, reprise et interfaces | Architecture, contrats et futures preuves de compilation |
| Notifications vocales et synchronisation | Voix, rappels, permissions et transmission | Contrats, états et limites vérifiables |
| Tests et qualité | Audit des profils et future recette | Résultats de configuration et matrice de tests |

Les cinq agents de cette session ont été créés réellement et observés en exécution simultanée. Ils ont chacun remis leur organisation de mission ; leurs cinq missions sont maintenant terminées. Ils ne constituent pas cinq services permanents en arrière-plan.

## Configuration préparée

Le dossier comporte les instructions communes AGENTS.md, cinq profils de projet .codex/agents, une configuration autorisant cinq sous-agents concurrents, cinq fiches missions et cinq livrables. Les six TOML et la cohérence des rôles ont été contrôlés.

Chaque fichier a un propriétaire ; les analyses indépendantes peuvent se faire en parallèle. Coordination consolide les quatre spécialistes ; le pilote gère les lancements et l'intégration. Les modifications partagées sont séquentielles. Aucun sixième agent et aucun GitHub.

## Exigences conservées

Marine violet, Parent vert ; consignes vocales automatiques ; barre temporelle systématique ; rappel paramétrable, deux minutes avant la fin par défaut ; activités vertes uniquement après confirmation explicite « J'ai terminé » ; aide distincte, sans validation automatique. Le hors ligne et la récupération des réglages Parent depuis un autre appareil restent dans le besoin.

## Limite à connaître

Les profils sont préparés au format Codex, mais leur chargement dans un client Codex distinct n'a pas été exécuté : le programme Codex n'est pas disponible ici. Je ne peux donc pas affirmer que le projet apparaît dans ton interface Codex personnelle. Télécharger le dossier ne crée pas automatiquement ce projet ni ses agents dans un autre client.

Les cinq agents réellement créés sont ceux de cette session. Aucun test de l'application, APK ou synchronisation n'est validé par cette organisation.

Source officielle consultée pour les profils de projet et l'exécution parallèle : [Documentation OpenAI — Subagents](https://learn.chatgpt.com/docs/agent-configuration/subagents).

## Mission de réalisation et questions

Les instructions ont été corrigées : les agents ne doivent pas s’arrêter à la documentation. Coordination pilote le plan et regroupe les questions ; Accessibilité définit et révise les parcours ; Android réalise le moteur et les écrans ; Voix/synchronisation réalise ses modules ; Qualité contrôle et fait corriger. Ils avancent sur les travaux indépendants et attendent les réponses uniquement pour les choix qui en dépendent.

Deux informations d’usage sont attendues : modèle/version Android du téléphone de Marine et routine réelle (heure de début, activités, durées). Aucun code à manipuler pour l’utilisateur. Les blocages de compilation et de synchronisation ne sont pas déclarés résolus.

## Précisions de routine fournies

Les horaires varient quotidiennement en fonction du taxi. Ordre du matin : lever, habillage, petit déjeuner, brossage des dents, chaussures, départ avec le taxi. Ajouter les activités ponctuelles de journée, la douche au retour de l’IME, le lavage des cheveux le mercredi après-midi et le dimanche matin. Les horaires et durées restent modifiables ; aucune heure fixe ni durée réelle n’est présumée. Le téléphone étant absent, son modèle sera demandé plus tard. Les cinq rôles reçoivent ces informations pour leur travail.

## Règle de lever confirmée

Le lever est fixé exactement une heure avant le passage du taxi : lever = taxi − 60 minutes. Les horaires de taxi varient selon le jour ; leur modification doit recalculer l’occurrence du matin sans modifier silencieusement les autres jours. Exemple seulement : taxi à 8 h 15, lever à 7 h 15. Les durées des étapes intermédiaires restent réglables et ne sont pas encore fournies. Le lever n’est pas calculé à partir de la somme des durées d’exemple.
