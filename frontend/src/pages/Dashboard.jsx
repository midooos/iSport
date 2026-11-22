import { useEffect, useMemo, useState } from "react";
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


// ======================================
// 1. FONCTION POUR NORMALISER LE TEXTE
// ======================================

function normalize(text) {

    let result = text.normalize("NFD");

    result = result.replace(/[\u0300-\u036f]/g, "");

    result = result.toLowerCase();

    result = result.trim();

    return result;
}


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

    // Le texte recherché
    const [query, setQuery] = useState("");

    // ID de l'événement en cours de modification
    const [pendingId, setPendingId] = useState(null);

    // Message d'erreur
    const [actionError, setActionError] = useState("");


    // ======================================
    // 4. RECUPERER LES EVENEMENTS
    // ======================================

    async function getEvents() {

        try {

            const data = await fetchUpcomingEvents();

            setEvents(data);

            setStatus("ready");

        }
        catch (error) {

            setStatus("error");

            console.log(error);

        }

    }


    useEffect(function() {

        getEvents();

    }, []);


    // ======================================
    // 5. RECHERCHER UN SPORT
    // ======================================

    const filtered = [];

    const search = normalize(query);

    for (let i = 0; i < events.length; i++) {

        const event = events[i];

        const sport = normalize(event.sport);

        if (sport.includes(search)) {

            filtered.push(event);

        }

    }


    // Vérifier si l'utilisateur fait une recherche
    let isSearching = false;

    if (query.trim() !== "") {
        isSearching = true;
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

            <header className="page__header">

                <h1>Événements à venir</h1>

                <p>
                    Choisis un match près de chez toi.
                </p>

            </header>


            {/* Barre de recherche */}

            <div className="search">

                <Input
                    label="Rechercher par sport"
                    id="search"
                    type="search"
                    value={query}
                    onChange={function(event) {
                        setQuery(event.target.value);
                    }}
                    placeholder="Football, padel, running..."
                />

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
                    Impossible de charger les événements.
                    Réessaie dans un instant.
                </p>

            )}


            {/* Vérifier si la liste est vide */}

            {status === "ready" && events.length === 0 && (

                <p>
                    Aucun événement à venir.

                    <Link to="/events/new">
                        Crée le premier.
                    </Link>

                </p>

            )}


            {/* Afficher les événements */}

            {status === "ready" && events.length > 0 && (

                <>

                    <p className="search__count">

                        {filtered.length}

                        {filtered.length > 1
                            ? " événements"
                            : " événement"}

                        {isSearching === true
                            ? " pour " + query.trim()
                            : ""}

                    </p>


                    {/* Aucun résultat trouvé */}

                    {filtered.length === 0 ? (

                        <div className="search__empty">

                            <p>
                                Aucun événement ne correspond à ce sport.
                            </p>

                            <Button
                                variant="secondary"
                                onClick={function() {
                                    setQuery("");
                                }}
                            >
                                Effacer la recherche
                            </Button>

                        </div>

                    ) : (

                        /* Afficher les cartes */

                        <ul className="event-list">

                            {filtered.map(function(event) {

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

                    )}

                </>

            )}

        </>

    );

}

export default Dashboard;
