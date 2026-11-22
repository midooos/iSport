// Toutes les requêtes vers le back passent par ce fichier

const TOKEN_KEY = 'isport_token'

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function setToken(token) {
  try {
    localStorage.setItem(TOKEN_KEY, token)
  } catch {
    // stockage indisponible : l'utilisateur devra se reconnecter
  }
}

export function clearToken() {
  try {
    localStorage.removeItem(TOKEN_KEY)
  } catch {
    // rien à faire
  }
}

// Erreur avec le code HTTP et le message renvoyé par le back ({ "message": "..." })
export class ApiError extends Error {
  constructor(status, message) {
    super(message)
    this.status = status
  }
}

async function request(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  let response
  try {
    response = await fetch(path, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError(0, 'Le serveur ne répond pas, réessayez plus tard')
  }

  let data = null
  try {
    data = await response.json()
  } catch {
    // réponse sans corps JSON
  }

  if (!response.ok) {
    throw new ApiError(response.status, data?.message || 'Une erreur est survenue')
  }
  return data
}

export function getHealth() {
  return request('/api/health')
}

export function register(values) {
  return request('/api/auth/register', { method: 'POST', body: values })
}

export function login(values) {
  return request('/api/auth/login', { method: 'POST', body: values })
}

export function getMe() {
  return request('/api/users/me')
}
