import { NavLink } from "react-router-dom";

export default function Navbar() {
  return (
    <header className="navbar">
      <div className="navbar__inner">
        <NavLink to="/" className="navbar__brand" aria-label="iSport, accueil">
          iSport
        </NavLink>

        <nav className="navbar__links" aria-label="Navigation principale">
          <NavLink to="/" end>
            Événements
          </NavLink>
          <NavLink to="/events/new">Créer un événement</NavLink>
        </nav>

        {/* Emplacement réservé : boutons « Se connecter » / « Se déconnecter »
            (US-03 et US-04, côté yessine) */}
        <div className="navbar__auth" />
      </div>
    </header>
  );
}
