import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import Input from "../components/Input.jsx";
import Button from "../components/Button.jsx";
import { useAuth } from "../context/useAuth.js";
import { validateLogin } from "../utils/validation.js";

export default function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [values, setValues] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState("");
  const [loading, setLoading] = useState(false);
  const message = location.state?.message;

  const handleChange = (event) => {
    const { name, value } = event.target;
    setValues((current) => ({ ...current, [name]: value }));
    setErrors((current) => ({ ...current, [name]: undefined }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setServerError("");

    const found = validateLogin(values);
    setErrors(found);
    if (Object.keys(found).length > 0) return;

    setLoading(true);
    try {
      await login(values.email.trim(), values.password);
      navigate("/");
    } catch (error) {
      setServerError(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <header className="page__header auth-page__header">
        <h1>Connexion</h1>
        <p>Content de te revoir.</p>
      </header>

      <form className="form" onSubmit={handleSubmit} noValidate>
        {message && (
          <p className="notice" role="status">
            {message}
          </p>
        )}
        {serverError && (
          <p className="field__error" role="alert">
            {serverError}
          </p>
        )}

        <Input label="Email" id="email" name="email" type="email" value={values.email} onChange={handleChange} error={errors.email} autoComplete="email" />
        <Input label="Mot de passe" id="password" name="password" type="password" value={values.password} onChange={handleChange} error={errors.password} autoComplete="current-password" />

        <Button type="submit" disabled={loading}>
          {loading ? "Connexion…" : "Se connecter"}
        </Button>
      </form>

      <p className="auth-switch">
        Pas encore de compte ? <Link to="/register">S'inscrire</Link>
      </p>
    </>
  );
}
