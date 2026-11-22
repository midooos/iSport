// Toutes les requêtes vers le back passent par ce fichier



const TOKEN_KEY = "isport_token";

export async function getHealth() {
  const response = await fetch("/api/health");

  if (!response.ok) {
    throw new Error("Le back ne répond pas");
  }

  return response.json();
}

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}
