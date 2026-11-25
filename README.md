# iSport

Application web pour organiser et rejoindre des événements sportifs : on crée un événement (sport, date, lieu, nombre de places), les autres joueurs le trouvent et s'y inscrivent.

## Technologies

- Back-end : Spring Boot (Java 17), Spring Security, JPA
- Base de données : PostgreSQL
- Front-end : React (Vite)
- Intégration continue : GitHub Actions

## Structure du dépôt

```
backend/    API Spring Boot
frontend/   application React
docs/       documentation (contrat d'API)
.github/    CI et modèle de pull request
```

## Lancer le projet en local (Windows / PowerShell)

Prérequis : JDK 17, Node.js et Docker Desktop démarré. Le Maven Wrapper télécharge Maven automatiquement.

1. À la racine du dépôt, démarrer PostgreSQL :

   ```powershell
   docker compose up -d
   ```

   PostgreSQL est accessible sur `localhost:5433` (le port hôte `5432` est réservé par un PostgreSQL local).

2. Dans un terminal, démarrer le backend :

   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

   L'API répond sur `http://localhost:8080`; vérifier avec `http://localhost:8080/api/health`.
   Pour les tests : `.\mvnw.cmd test`.

3. Dans un autre terminal, démarrer le frontend :

   ```powershell
   cd frontend
   npm ci
   npm run dev
   ```

   Ouvrir l'adresse affichée par Vite, généralement `http://localhost:5173`.

### Profil de démonstration

Pour lancer le backend avec les données de démonstration, utiliser à la place de la commande de démarrage ci-dessus :

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Le profil `dev` utilise une base H2 en mémoire (et non PostgreSQL), recréée au démarrage. Compte de démonstration : `demo@isport.com` / `motdepasse1`.

## Équipe

- Rayen Bejaoui
- Mohamed Dridi
- Yassine Kchok
- Elhoussine Ouadhour
- Tarek Aziz Ben Amara
