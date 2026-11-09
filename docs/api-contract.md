# Contrat d'API (brouillon)

Ce document sert à ce que le front et le back avancent en parallèle. Pour l'instant il liste les routes qui viennent des stories du sprint 1. Il reste à compléter ensemble : les données envoyées et reçues pour chaque route, et les codes d'erreur.

## Règles générales

- Toutes les routes commencent par `/api`.
- Les données sont échangées en JSON.
- Les routes protégées demandent l'en-tête `Authorization: Bearer <token>` (JWT obtenu à la connexion).

## Routes

| Méthode | Route | Accès | Story |
|---|---|---|---|
| GET | `/api/health` | public | vérification que le back tourne |
| POST | `/api/auth/register` | public | US-01 et US-02 (inscription et validation) |
| POST | `/api/auth/login` | public | US-03 (renvoie le JWT) |
| GET | `/api/users/me` | connecté | US-18 (nom et email du profil) |
| POST | `/api/events` | connecté | US-05 (créer un événement) |
| GET | `/api/events` | connecté | US-06 (événements à venir, triés par date) |
| GET | `/api/events?sport=...` | connecté | US-15 (recherche par sport) |
| POST | `/api/events/{id}/join` | connecté | US-08 et US-10 (rejoindre, refus si complet) |
| POST | `/api/events/{id}/leave` | connecté | US-09 (quitter un événement) |

La déconnexion (US-04) se fait côté front : on supprime le token et on redirige vers la page de connexion.

## À décider

- Format des erreurs renvoyées par le back (par exemple un message et un code).
- Durée de vie du token JWT.
