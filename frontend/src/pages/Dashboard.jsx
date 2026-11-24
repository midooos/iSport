import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Card from "../components/Card.jsx";
import Badge from "../components/Badge.jsx";
import Input from "../components/Input.jsx";
import Button from "../components/Button.jsx";




import {
    fetchUpcomingEvents,
    joinEvent,
    leaveEvent
} from "../services/eventApi.js";

import { SPORTS } from "../utils/sports.js";


// ======================================
// 2. COMPOSANT POUR AFFICHER UN EVENEMENT
// ======================================

function EventCard(props) {

    const event = props.event;

    const date = new Date(event.startsAt);

    const day = date.getDate();

    const month = date.toLocaleDateString("fr-FR", {
        month: "short"
    });

    const time = date.toLocaleTimeString("fr-FR", {
        hour: "2-digit",
        minute: "2-digit"
    });

    // Calculer les places restantes
    const remaining = event.max - event.joined;

    let isFull = false;

    if (remaining <= 0) {
        isFull = true;
    }


    return (

        <Card className="event">

            <div className="event__date">

                <span className="event__day">
                    {day}
                </span>

                <span className="event__month">
                    {month}
                </span>

            </div>


            <div className="event__body">

                <h2 className="event__title">
                    {event.title}
                </h2>

                <p className="event__sport">
                    {event.sport}
                </p>

                <p className="event__place">
                    {event.place}, à {time}
                </p>

            </div>


            <div className="event__status">

                {/* Afficher le nombre de places restantes */}

                {isFull === true ? (

                    <Badge variant="full">
                        Complet
                    </Badge>

                ) : (

                    <Badge variant="open">

                        {remaining}

                        {remaining > 1
                            ? " places restantes"
                            : " place restante"}

                    </Badge>

                )}


                {/* Bouton Quitter ou Rejoindre */}

                {event.joinedByMe === true ? (

                    <Button
                        variant="secondary"
                        onClick={function() {
                            props.onLeave(event.id);
                        }}
                        disabled={props.pending}
                    >

                        {props.pending === true
                            ? "En cours..."
                            : "Quitter"}

                    </Button>

                ) : (

                    <Button
                        onClick={function() {
                            props.onJoin(event.id);
                        }}
                        disabled={isFull === true || props.pending === true}
                    >

                        {props.pending === true
                            ? "En cours..."
                            : "Rejoindre"}

                    </Button>

                )}

            </div>

        </Card>

    );

}


// ======================================
// 3. COMPOSANT PRINCIPAL DASHBOARD
// ======================================

function Dashboard() {


    // Les événements
    const [events, setEvents] = useState([]);

    // L'état du chargement
    const [status, setStatus] = useState("loading");

    // Le sport choisi dans le filtre ("" = tous les sports)
    const [sport, setSport] = useState("");

    // Message d'erreur de chargement
    const [loadError, setLoadError] = useState("");

    // ID de l'événement en cours de modification
    const [pendingId, setPendingId] = useState(null);

    // Message d'erreur
    const [actionError, setActionError] = useState("");


    // ======================================
    // 4. RECUPERER LES EVENEMENTS (FILTRES PAR SPORT)
    // ======================================

    // Rechargé à chaque changement de sport : le filtre est fait par le back
    // (GET /api/events?sport=...)
    useEffect(function() {

        // Si l'utilisateur change de sport avant la fin du chargement,
        // on ignore l'ancienne réponse pour ne pas afficher la mauvaise liste
        let ignore = false;

        async function getEvents() {

            setStatus("loading");

            try {

                const data = await fetchUpcomingEvents(sport);

                if (ignore === false) {

                    setEvents(data);

                    setStatus("ready");

                }

            }
            catch (error) {

                if (ignore === false) {

                    setLoadError(error.message);

                    setStatus("error");

                }

            }

        }

        getEvents();

        return function() {
            ignore = true;
        };

    }, [sport]);


    // Vérifier si un sport est choisi
    let isFiltering = false;

    if (sport !== "") {
        isFiltering = true;
    }


    // ======================================
    // 6. MODIFIER UN EVENEMENT DANS LA LISTE
    // ======================================

    function replaceEvent(updatedEvent) {

        const newEvents = [];

        for (let i = 0; i < events.length; i++) {

            if (events[i].id === updatedEvent.id) {

                newEvents.push(updatedEvent);

            } else {

                newEvents.push(events[i]);

            }

        }

        setEvents(newEvents);

    }


    // ======================================
    // 7. REJOINDRE OU QUITTER UN EVENEMENT
    // ======================================

    async function runAction(id, action) {

        setActionError("");

        setPendingId(id);

        try {

            const updatedEvent = await action(id);

            replaceEvent(updatedEvent);

        }
        catch (error) {

            setActionError(error.message);

        }
        finally {

            setPendingId(null);

        }

    }


    // Fonction pour rejoindre
    function handleJoin(id) {

        runAction(id, joinEvent);

    }


    // Fonction pour quitter
    function handleLeave(id) {

        runAction(id, leaveEvent);

    }


    // ======================================
    // 8. AFFICHAGE DE LA PAGE
    // ======================================

    return (

        <>

            <header className="page__header dashboard__header">
                <div>
                    <p className="dashboard__eyebrow">iSport · rencontres locales</p>
                    <h1>On joue ?</h1>
                    <p>Des matchs proposés par les joueurs près de chez vous.</p>
                </div>

                <Link className="dashboard__create" to="/events/new">
                    Proposer une rencontre
                </Link>
            </header>


            <div className="dashboard__toolbar">
                <div className="dashboard__list-heading">
                    <h2>Les prochaines rencontres</h2>
                    {status === "ready" && (
                        <p>
                            {events.length} {events.length === 1 ? "événement" : "événements"}
                            {isFiltering === true ? " · " + sport : ""}
                        </p>
                    )}
                </div>

                <div className="search">

                    <Input
                        as="select"
                        label="Filtrer par sport"
                        id="sport-filter"
                        name="sport"
                        value={sport}
                        onChange={function(event) {
                            setSport(event.target.value);
                        }}
                    >

                        <option value="">
                            Tous les sports
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

                </div>
            </div>


            {/* Afficher une erreur d'action */}

            {actionError !== "" && (

                <p className="field__error action-error" role="alert">
                    {actionError}
                </p>

            )}


            {/* Afficher le chargement */}

            {status === "loading" && (

                <p>
                    Chargement des événements...
                </p>

            )}


            {/* Afficher une erreur de chargement */}

            {status === "error" && (

                <p role="alert">
                    {loadError}
                </p>

            )}


            {/* Aucun événement du tout */}

            {status === "ready" && events.length === 0 && isFiltering === false && (

                <p>
                    Aucun événement à venir.

                    <Link to="/events/new">
                        Crée le premier.
                    </Link>

                </p>

            )}


            {/* Aucun événement pour le sport choisi */}

            {status === "ready" && events.length === 0 && isFiltering === true && (

                <div className="search__empty">

                    <p>
                        Aucun événement à venir pour ce sport.
                    </p>

                    <Button
                        variant="secondary"
                        onClick={function() {
                            setSport("");
                        }}
                    >
                        Voir tous les sports
                    </Button>

                </div>

            )}


            {/* Afficher les événements */}

            {status === "ready" && events.length > 0 && (

                <>

                    {/* Afficher les cartes */}

                    <ul className="event-list">

                        {events.map(function(event) {

                            return (

                                <li key={event.id}>

                                    <EventCard
                                        event={event}

                                        pending={pendingId === event.id}

                                        onJoin={handleJoin}

                                        onLeave={handleLeave}
                                    />

                                </li>

                            );

                        })}

                    </ul>

                </>

            )}

        </>

    );

}

export default Dashboard;
