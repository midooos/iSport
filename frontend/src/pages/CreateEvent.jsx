import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Input from "../components/Input.jsx";
import Button from "../components/Button.jsx";
import { createEvent } from "../services/eventApi.js";

const SPORTS = ["Football", "Basketball", "Volley-ball", "Tennis", "Padel", "Running", "Autre"];

const EMPTY = { title: "", sport: "", date: "", time: "", place: "", maxParticipants: "" };

function validate(values) {
  const errors = {};
  if (!values.title.trim()) errors.title = "Donne un titre à ton événement.";
  if (!values.sport) errors.sport = "Choisis un sport.";
  if (!values.date) errors.date = "Indique la date.";
  if (!values.time) errors.time = "Indique l'heure.";
  if (!values.place.trim()) errors.place = "Indique le lieu.";

  if (values.date && values.time && new Date(`${values.date}T${values.time}`) < new Date()) {
    errors.date = "La date et l'heure doivent être dans le futur.";
  }

  const max = Number(values.maxParticipants);
  if (!Number.isInteger(max) || max < 2) {
    errors.maxParticipants = "Entre un nombre de participants d'au moins 2.";
  }
  return errors;
}

export default function CreateEvent() {
  const navigate = useNavigate();
  const [values, setValues] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const handleChange = (event) => {
    const { name, value } = event.target;
    setValues((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitError("");

    const found = validate(values);
    setErrors(found);
    if (Object.keys(found).length > 0) return;

    setSubmitting(true);
    try {
      await createEvent(values);
      navigate("/"); // retour au dashboard après création
    } catch (error) {
      setSubmitError(error.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <header className="page__header">
        <h1>Créer un événement</h1>
        <p>Renseigne les informations pour inviter des joueurs.</p>
      </header>

      <form className="form" onSubmit={handleSubmit} noValidate>
        <Input label="Titre" id="title" name="title" value={values.title} onChange={handleChange} error={errors.title} placeholder="Foot à 5 du vendredi" />

        <Input as="select" label="Sport" id="sport" name="sport" value={values.sport} onChange={handleChange} error={errors.sport}>
          <option value="">Choisir un sport</option>
          {SPORTS.map((sport) => (
            <option key={sport} value={sport}>{sport}</option>
          ))}
        </Input>

        <div className="form__row">
          <Input label="Date" id="date" name="date" type="date" value={values.date} onChange={handleChange} error={errors.date} />
          <Input label="Heure" id="time" name="time" type="time" value={values.time} onChange={handleChange} error={errors.time} />
        </div>

        <Input label="Lieu" id="place" name="place" value={values.place} onChange={handleChange} error={errors.place} placeholder="Complexe du Lac 2, Tunis" />

        <Input label="Nombre maximum de participants" id="maxParticipants" name="maxParticipants" type="number" min="2" value={values.maxParticipants} onChange={handleChange} error={errors.maxParticipants} hint="Les inscriptions se ferment quand ce nombre est atteint." />

        {submitError && (
          <p className="field__error" role="alert">{submitError}</p>
        )}

        <Button type="submit" disabled={submitting}>
          {submitting ? "Création en cours…" : "Créer l'événement"}
        </Button>
      </form>
    </>
  );
}