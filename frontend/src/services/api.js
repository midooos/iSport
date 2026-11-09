// Toutes les requêtes vers le back passent par ce fichier

export async function getHealth() {
  const response = await fetch('/api/health')
  if (!response.ok) {
    throw new Error('Le back ne répond pas')
  }
  return response.json()
}
