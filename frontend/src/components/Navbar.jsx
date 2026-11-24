
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth.js";
import Button from "./Button.jsx";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <header className="navbar">
      <div className="navbar__inner">

        <NavLink to="/" className="navbar__brand">
          <img className="navbar__brand-icon" src="/favicon.svg" alt="" />
          <span>iSport</span>
        </NavLink>

        <nav className="navbar__links">
          <NavLink to="/" end>
            Événements
          </NavLink>

          <NavLink to="/events/new">
            Créer un événement
          </NavLink>
        </nav>

        <div className="navbar__auth">
          {user ? (
            <>
              <NavLink to="/profile">
                {user.login}
              </NavLink>

              <Button
                variant="secondary"
                onClick={handleLogout}
              >
                Se déconnecter
              </Button>
            </>
          ) : (
            <>
              <NavLink to="/login">
                Connexion
              </NavLink>

              <NavLink to="/register" className="navbar__cta">
                Inscription
              </NavLink>
            </>
          )}
        </div>

      </div>
    </header>
  );
}
