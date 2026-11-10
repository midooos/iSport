# Contrat d'API

Ce document dit au front et au back comment ils se parlent .
## Règles générales

- Toutes les routes commencent par `/api`.
- Les données sont échangées en JSON.
- Les routes protégées demandent l'en-tête `Authorization: Bearer <token>`. Le token (JWT) est donné par la connexion et dure 24 heures.
- Les dates s'écrivent `2026-12-05` et les heures `18:00`.
- En cas d'erreur, le back renvoie un code HTTP et un message lisible :

```json
{ "message": "L'événement est complet" }
```

Codes utilisés : `400` données invalides, `401` pas connecté ou token invalide, `404` introuvable, `409` conflit (déjà inscrit, événement complet, email déjà utilisé).

## Objet événement

C'est ce que renvoient toutes les routes qui parlent d'un événement.

```json
{
  "id": 12,
  "title": "Match de foot du samedi",
  "sport": "football",
  "date": "2026-12-05",
  "time": "18:00",
  "location": "Stade municipal",
  "maxParticipants": 10,
  "participantsCount": 7,
  "status": "OUVERT",
  "organizer": { "id": 3, "login": "mohamed" },
  "joined": false
}
```

`status` vaut `OUVERT` ou `COMPLET`. `joined` dit si l'utilisateur connecté participe déjà à l'événement.

## Routes

### GET /api/health (public)

Sert à vérifier que le back tourne. Réponse `200` : `{ "status": "ok" }`.

### POST /api/auth/register (public) : US-01 et US-02

Corps :

```json
{ "login": "mohamed", "email": "mohamed@mail.com", "password": "motdepasse1", "confirmPassword": "motdepasse1" }
```

Règles de validation : email valide, mot de passe de 8 caractères minimum, confirmation identique au mot de passe.

- `201` : `{ "id": 3, "login": "mohamed", "email": "mohamed@mail.com" }`
- `400` : un champ est invalide (le message dit lequel)
- `409` : email ou login déjà utilisé

### POST /api/auth/login (public) : US-03

Corps : `{ "email": "mohamed@mail.com", "password": "motdepasse1" }`

- `200` : `{ "token": "<jwt>", "user": { "id": 3, "login": "mohamed", "email": "mohamed@mail.com" } }`
- `401` : email ou mot de passe incorrect

### GET /api/users/me (connecté) : US-18

- `200` : `{ "id": 3, "login": "mohamed", "email": "mohamed@mail.com" }`
- `401` : pas connecté

### POST /api/events (connecté) : US-05

Corps :

```json
{ "title": "Match de foot du samedi", "sport": "football", "date": "2026-12-05", "time": "18:00", "location": "Stade municipal", "maxParticipants": 10 }
```

Règles : tous les champs sont obligatoires, la date ne doit pas être passée, `maxParticipants` est au moins 2.

- `201` : l'objet événement
- `400` : un champ est invalide
- `401` : pas connecté

### GET /api/events (connecté) : US-06 et US-15

Renvoie les événements à venir, triés par date (du plus proche au plus lointain). Avec `?sport=football`, ne garde que ceux de ce sport.

- `200` : une liste d'objets événement
- `401` : pas connecté

### POST /api/events/{id}/join (connecté) : US-08 et US-10

Pas de corps. L'inscription et la mise à jour du statut se font en une seule opération, pour qu'il n'y ait jamais plus de participants que la capacité, même si plusieurs personnes s'inscrivent en même temps. Quand la dernière place est prise, le statut passe à `COMPLET`.

- `200` : l'objet événement mis à jour
- `404` : événement introuvable
- `409` : déjà inscrit, ou événement complet

### POST /api/events/{id}/leave (connecté) : US-09

Pas de corps. Si l'événement était complet, il repasse à `OUVERT`.

- `200` : l'objet événement mis à jour
- `404` : événement introuvable
- `409` : l'utilisateur n'était pas inscrit

