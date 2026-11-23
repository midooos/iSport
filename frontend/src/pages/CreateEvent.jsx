import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Input from "../components/Input.jsx";
import Button from "../components/Button.jsx";
import { createEvent } from "../services/eventApi.js";
import { SPORTS } from "../utils/sports.js";


function CreateEvent() {

    const navigate = useNavigate();


    // Les informations du formulaire
    const [title, setTitle] = useState("");
    const [sport, setSport] = useState("");
    const [date, setDate] = useState("");
    const [time, setTime] = useState("");
    const [place, setPlace] = useState("");
    const [maxParticipants, setMaxParticipants] = useState("");


    // Gestion des erreurs et du chargement
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);


    // ======================================
    // 1. ENVOYER LE FORMULAIRE
    // ======================================

    async function handleSubmit(event) {

        event.preventDefault();

        let newError = "";


        // Vérifier le titre
        if (title.trim() === "") {

            newError = "Donne un titre à ton événement.";

        }

        // Vérifier le sport
        else if (sport === "") {

            newError = "Choisis un sport.";

        }

        // Vérifier la date
        else if (date === "") {

            newError = "Indique la date.";

        }

        // Vérifier l'heure
        else if (time === "") {

            newError = "Indique l'heure.";

        }

        // Vérifier le lieu
        else if (place.trim() === "") {

            newError = "Indique le lieu.";

        }

        // Vérifier le nombre de participants
        else if (
            maxParticipants === "" ||
            Number(maxParticipants) < 2 ||
            Number.isInteger(Number(maxParticipants)) === false
        ) {

            newError = "Entre un nombre de participants d'au moins 2.";

        }

        // Vérifier que la date est dans le futur
        else if (new Date(date + "T" + time) < new Date()) {

            newError = "La date et l'heure doivent être dans le futur.";

        }


        // Afficher l'erreur si nécessaire
        if (newError !== "") {

            setError(newError);

            return;

        }


        // ======================================
        // 2. ENVOYER LES DONNEES A L'API
        // ======================================

        setError("");

        setLoading(true);


        try {

            const values = {
                title: title,
                sport: sport,
                date: date,
                time: time,
                place: place,
                maxParticipants: maxParticipants
            };

            await createEvent(values);


            // Retourner au Dashboard
            alert("Événement créé avec succès !");

            navigate("/");

        }

        catch (error) {

            setError(error.message);

            console.log(error);

        }

        finally {

            setLoading(false);

        }

    }


    // ======================================
    // 3. AFFICHER LE FORMULAIRE
    // ======================================

    return (

        <div>

            <header className="page__header">

                <h1>Créer un événement</h1>

                <p>
                    Renseigne les informations pour inviter des joueurs.
                </p>

            </header>


            <form
                className="form"
                onSubmit={handleSubmit}
                noValidate
            >


                {/* Titre */}

                <Input
                    label="Titre"
                    id="title"
                    name="title"
                    type="text"
                    value={title}
                    onChange={function(event) {
                        setTitle(event.target.value);
                    }}
                    placeholder="Foot à 5 du vendredi"
                />


                {/* Sport */}

                <Input
                    as="select"
                    label="Sport"
                    id="sport"
                    name="sport"
                    value={sport}
                    onChange={function(event) {
                        setSport(event.target.value);
                    }}
                >

                    <option value="">
                        Choisir un sport
                    </option>

                    {SPORTS.map(function(sportName) {

                        return (

                            <option
                                key={sportName}
                                value={sportName}
                            >
                                {sportName}
                            </option>

                        );

                    })}

                </Input>


                {/* Date et heure */}

                <div className="form__row">

                    <Input
                        label="Date"
                        id="date"
                        name="date"
                        type="date"
                        value={date}
                        onChange={function(event) {
                            setDate(event.target.value);
                        }}
                    />

                    <Input
                        label="Heure"
                        id="time"
                        name="time"
                        type="time"
                        value={time}
                        onChange={function(event) {
                            setTime(event.target.value);
                        }}
                    />

                </div>


                {/* Lieu */}

                <Input
                    label="Lieu"
                    id="place"
                    name="place"
                    type="text"
                    value={place}
                    onChange={function(event) {
                        setPlace(event.target.value);
                    }}
                    placeholder="Complexe du Lac 2, Tunis"
                />


                {/* Nombre maximum de participants */}

                <Input
                    label="Nombre maximum de participants"
                    id="maxParticipants"
                    name="maxParticipants"
                    type="number"
                    value={maxParticipants}
                    onChange={function(event) {
                        setMaxParticipants(event.target.value);
                    }}
                    hint="Les inscriptions se ferment quand ce nombre est atteint."
                />


                {/* Message d'erreur */}

                {error !== "" && (

                    <p className="field__error" role="alert">
                        {error}
                    </p>

                )}


                {/* Bouton de création */}

                <Button
                    type="submit"
                    disabled={loading}
                >

                    {loading === true
                        ? "Création en cours..."
                        : "Créer l'événement"}

                </Button>


            </form>

        </div>

    );

}

export default CreateEvent;
