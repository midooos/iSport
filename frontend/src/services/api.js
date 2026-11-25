
const TOKEN_KEY = "isport_token";

function apiError(message, status) {
  const error = new Error(message);
  error.status = status;
  return error;
}

async function requestApi(url, options) {
  try {
    return await fetch(url, options);
  } catch (error) {
    if (error instanceof TypeError) {
      throw new Error("Impossible de joindre le serveur. Vérifiez que le backend est démarré.");
    }
    throw error;
  }
}

async function readJsonResponse(response, fallbackMessage) {
  const text = await response.text();
  let data = null;

  if (text.trim()) {
    try {
      data = JSON.parse(text);
    } catch (error) {
      if (!(error instanceof SyntaxError)) throw error;
      const message = response.status >= 500
        ? `Le serveur est indisponible (HTTP ${response.status}). Vérifiez que le backend est démarré.`
        : "La réponse du serveur est invalide.";
      throw apiError(message, response.status);
    }
  }

  if (!response.ok) {
    const message = data?.message || (response.status >= 500
      ? `Le serveur est indisponible (HTTP ${response.status}). Vérifiez que le backend est démarré.`
      : fallbackMessage);
    throw apiError(message, response.status);
  }

  if (data === null || typeof data !== "object") {
    throw apiError("La réponse du serveur est vide ou invalide.", response.status);
  }

  return data;
}

// Vérifier si le backend répond
export async function getHealth() {
  const response = await requestApi("/api/health");

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
  const response = await requestApi("/api/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email, password }),
  });

  return readJsonResponse(response, "Erreur lors de la connexion");
}

// Inscription
export async function register({ login, email, password, confirmPassword }) {
  const response = await requestApi("/api/auth/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ login, email, password, confirmPassword }),
  });

  return readJsonResponse(response, "Erreur lors de l'inscription");
}

// Récupérer le profil de l'utilisateur connecté
export async function getMe() {
  const response = await requestApi("/api/users/me", {
    method: "GET",
    headers: {
      Authorization: `Bearer ${getToken()}`,
    },
  });

  return readJsonResponse(response, "Impossible de récupérer le profil");
}
