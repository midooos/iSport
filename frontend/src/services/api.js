
const TOKEN_KEY = "isport_token";

// Vérifier si le backend répond
export async function getHealth() {
  const response = await fetch("/api/health");

  if (!response.ok) {
    throw new Error("Le back ne répond pas");
  }

  return response.json();
}

// Récupérer le token
export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

// Enregistrer le token
export function setToken(token) {
  try {
    localStorage.setItem(TOKEN_KEY, token);
  } catch {
    // Le stockage peut être indisponible
  }
}

// Supprimer le token
export function clearToken() {
  try {
    localStorage.removeItem(TOKEN_KEY);
  } catch {
    // Le stockage peut être indisponible
  }
}

// Connexion
export async function login({ email, password }) {
  const response = await fetch("/api/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email, password }),
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || "Erreur lors de la connexion");
  }

  return data;
}

// Inscription
export async function register({ login, email, password }) {
  const response = await fetch("/api/auth/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ login, email, password }),
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || "Erreur lors de l'inscription");
  }

  return data;
}

// Récupérer le profil de l'utilisateur connecté
export async function getMe() {
  const response = await fetch("/api/users/me", {
    method: "GET",
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  });

  const data = await response.json();

  if (!response.ok) {
    const error = new Error(
      data.message || "Impossible de récupérer le profil"
    );
    error.status = response.status;
    throw error;
  }

  return data;
}
