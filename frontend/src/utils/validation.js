// Validation côté client. Les règles sont les mêmes que celles du back (contrat d'API),
// mais le back reste la référence : il revérifie tout.

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function validateRegister({ login, email, password, confirmPassword }) {
  const errors = {}

  if (!login.trim()) {
    errors.login = 'Le login est obligatoire'
  } else if (login.trim().length > 50) {
    errors.login = 'Le login ne doit pas dépasser 50 caractères'
  }

  if (!email.trim()) {
    errors.email = "L'email est obligatoire"
  } else if (!EMAIL_REGEX.test(email.trim())) {
    errors.email = "L'email n'est pas valide"
  }

  if (!password) {
    errors.password = 'Le mot de passe est obligatoire'
  } else if (password.length < 8) {
    errors.password = 'Le mot de passe doit contenir au moins 8 caractères'
  }

  if (!confirmPassword) {
    errors.confirmPassword = 'La confirmation du mot de passe est obligatoire'
  } else if (confirmPassword !== password) {
    errors.confirmPassword = 'La confirmation ne correspond pas au mot de passe'
  }

  return errors
}

export function validateLogin({ email, password }) {
  const errors = {}
  if (!email.trim()) {
    errors.email = "L'email est obligatoire"
  }
  if (!password) {
    errors.password = 'Le mot de passe est obligatoire'
  }
  return errors
}
