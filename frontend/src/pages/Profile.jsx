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
        <dl>
          <dt>Login</dt>
          <dd>{user.login}</dd>
          <dt>Email</dt>
          <dd>{user.email}</dd>
        </dl>
      </Card>
    </>
  );
}
