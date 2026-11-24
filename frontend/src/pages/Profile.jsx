import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Card from "../components/Card.jsx";
import { useAuth } from "../context/useAuth.js";
import { getMe } from "../services/api.js";

const AVATARS = [
  { id: "football", label: "Football", emoji: "⚽" },
  { id: "basketball", label: "Basketball", emoji: "🏀" },
  { id: "tennis", label: "Tennis", emoji: "🎾" },
  { id: "volleyball", label: "Volleyball", emoji: "🏐" },
  { id: "running", label: "Course", emoji: "🏃" },
  { id: "strength", label: "Musculation", emoji: "🏋️" },
];

function readAvatar(userId) {
  const savedAvatar = localStorage.getItem(`isport:avatar:${userId}`);
  return AVATARS.some((avatar) => avatar.id === savedAvatar)
    ? savedAvatar
    : AVATARS[0].id;
}

export default function Profile() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [selectedAvatar, setSelectedAvatar] = useState(() => readAvatar(user.id));
  const avatar = AVATARS.find((option) => option.id === selectedAvatar) ?? AVATARS[0];

  const handleAvatarChange = (avatarId) => {
    localStorage.setItem(`isport:avatar:${user.id}`, avatarId);
    setSelectedAvatar(avatarId);
  };

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
        <div className="profile__header">
          <span className="profile__avatar" aria-hidden="true">
            {avatar.emoji}
          </span>
          <div>
            <h2 className="profile__title">Mon compte</h2>
            <p className="profile__subtitle">Tes informations personnelles</p>
          </div>
        </div>

        <dl className="profile__details">
          <div className="profile__item">
            <dt>Nom d'utilisateur</dt>
            <dd>{user.login}</dd>
          </div>
          <div className="profile__item">
            <dt>Email</dt>
            <dd>{user.email}</dd>
          </div>
        </dl>

        <div className="profile__avatar-picker">
          <h3>Choisis ton avatar</h3>
          <p>Ton choix est enregistré dans ce navigateur.</p>
          <div className="profile__avatar-options" role="group" aria-label="Choisir un avatar">
            {AVATARS.map((option) => (
              <button
                key={option.id}
                type="button"
                className="profile__avatar-option"
                aria-label={option.label}
                aria-pressed={selectedAvatar === option.id}
                onClick={() => handleAvatarChange(option.id)}
              >
                <span aria-hidden="true">{option.emoji}</span>
              </button>
            ))}
          </div>
        </div>
      </Card>
    </>
  );
}
