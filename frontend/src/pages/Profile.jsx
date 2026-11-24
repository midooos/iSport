import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Card from "../components/Card.jsx";
import { useAuth } from "../context/useAuth.js";
import { getMe } from "../services/api.js";

export default function Profile() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  // On affiche tout de suite les infos gardées à la connexion, puis on vérifie auprès du back.
  // Si le token n'est plus valable (401), on déconnecte l'utilisateur.
  useEffect(() => {
    getMe().catch((error) => {
      if (error.status === 401) {
        logout();
        navigate("/login");
      }
    });
  }, [logout, navigate]);

  return (
    <>
      <header className="page__header">
        <h1>Mon profil</h1>
        <p>Les informations de ton compte.</p>
      </header>

      <Card as="section" className="profile">
        <div className="profile__heading">
          <span className="profile__avatar" aria-hidden="true">
            {user.login.trim().charAt(0).toUpperCase()}
          </span>
          <div>
            <p className="profile__eyebrow">Espace joueur</p>
            <h2>{user.login}</h2>
            <p className="profile__subtitle">Votre compte iSport</p>
          </div>
        </div>

        <dl className="profile__details">
          <div className="profile__detail">
            <dt>Nom d'utilisateur</dt>
            <dd>{user.login}</dd>
          </div>
          <div className="profile__detail">
            <dt>Adresse e-mail</dt>
            <dd>{user.email}</dd>
          </div>
        </dl>
      </Card>
    </>
  );
}
