import { useState } from 'react'
import { clearToken, login as apiLogin, setToken } from '../services/api.js'
import { AuthContext } from './AuthContext.js'

const USER_KEY = 'isport_user'

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY))
  } catch {
    return null
  }
}

// Garde l'utilisateur connecté et son token (stockés dans le navigateur)
function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser)

  async function login(email, password) {
    const data = await apiLogin({ email, password })
    setToken(data.token)
    try {
      localStorage.setItem(USER_KEY, JSON.stringify(data.user))
    } catch {
      // stockage indisponible
    }
    setUser(data.user)
  }

  function logout() {
    clearToken()
    try {
      localStorage.removeItem(USER_KEY)
    } catch {
      // rien à faire
    }
    setUser(null)
  }

  return <AuthContext.Provider value={{ user, login, logout }}>{children}</AuthContext.Provider>
}

export default AuthProvider
