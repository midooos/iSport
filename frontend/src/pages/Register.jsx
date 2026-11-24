import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Input from "../components/Input.jsx";
import Button from "../components/Button.jsx";
import { register } from "../services/api.js";
import { validateRegister } from "../utils/validation.js";

const EMPTY = { login: "", email: "", password: "", confirmPassword: "" };

export default function Register() {
  const navigate = useNavigate();
  const [values, setValues] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setValues((current) => ({ ...current, [name]: value }));
    // l'erreur d'un champ disparaît dès qu'on le modifie
    setErrors((current) => ({ ...current, [name]: undefined }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setServerError("");

    const found = validateRegister(values);
    setErrors(found);
    if (Object.keys(found).length > 0) return;

    setLoading(true);
    try {
      await register({
        login: values.login.trim(),
        email: values.email.trim(),
        password: values.password,
        confirmPassword: values.confirmPassword,
      });
      navigate("/login", { state: { message: "Compte créé, tu peux te connecter." } });
    } catch (error) {
      setServerError(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <header className="page__header">
        <h1>Inscription</h1>
        <p>Crée ton compte pour rejoindre des événements.</p>
      </header>

      <form className="form" onSubmit={handleSubmit} noValidate>
        {serverError && (
          <p className="field__error" role="alert">
            {serverError}
          </p>
        )}

        <Input label="Nom d'utilisateur" id="login" name="login" value={values.login} onChange={handleChange} error={errors.login} autoComplete="username" />
        <Input label="Email" id="email" name="email" type="email" value={values.email} onChange={handleChange} error={errors.email} autoComplete="email" />
        <Input label="Mot de passe" id="password" name="password" type="password" value={values.password} onChange={handleChange} error={errors.password} hint="8 caractères minimum." autoComplete="new-password" />
        <Input label="Confirmer le mot de passe" id="confirmPassword" name="confirmPassword" type="password" value={values.confirmPassword} onChange={handleChange} error={errors.confirmPassword} autoComplete="new-password" />

        <Button type="submit" disabled={loading}>
          {loading ? "Création du compte…" : "S'inscrire"}
        </Button>
      </form>

      <p className="auth-switch">
        Tu as déjà un compte ? <Link to="/login">Se connecter</Link>
      </p>
    </>
  );
}
