import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import Card from "../components/Card.jsx";
import Badge from "../components/Badge.jsx";
import Input from "../components/Input.jsx";
import Button from "../components/Button.jsx";
import { fetchUpcomingEvents } from "../services/eventApi.js";

// Minuscules et sans accents, pour que « foot » trouve « Football »
const normalize = (text) =>
  text.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().trim();

function EventCard({ event }) {
  const date = new Date(event.startsAt);
  const day = date.getDate();
  const month = date.toLocaleDateString("fr-FR", { month: "short" });
  const time = date.toLocaleTimeString("fr-FR", { hour: "2-digit", minute: "2-digit" });
  const remaining = event.max - event.joined;
  const isFull = remaining <= 0;

  return (
    <Card className="event">
      <div className="event__date" aria-hidden="true">
        <span className="event__day">{day}</span>
        <span className="event__month">{month}</span>
      </div>

      <div className="event__body">
        <h2 className="event__title">{event.title}</h2>
        <p className="event__sport">{event.sport}</p>
        <p className="event__place">{event.place}, à {time}</p>
      </div>

      <div className="event__status">
        {isFull ? (
          <Badge variant="full">Complet</Badge>
        ) : (
          <Badge variant="open">
            {remaining} {remaining > 1 ? "places restantes" : "place restante"}
          </Badge>
        )}
      </div>
    </Card>
  );
}

export default function Dashboard() {
  const [events, setEvents] = useState([]);
  const [status, setStatus] = useState("loading"); // loading | ready | error
  const [query, setQuery] = useState("");

  useEffect(() => {
    fetchUpcomingEvents()
      .then((data) => {
        setEvents(data);
        setStatus("ready");
      })
      .catch(() => setStatus("error"));
  }, []);

  // Filtre par sport (côté front). Quand KAN-54 sera prêt, on pourra
  // le remplacer par un appel GET /api/events?sport=...
  const filtered = useMemo(() => {
    const search = normalize(query);
    if (!search) return events;
    return events.filter((event) => normalize(event.sport).includes(search));
  }, [events, query]);

  const isSearching = query.trim() !== "";

  return (
    <>
      <header className="page__header">
        <h1>Événements à venir</h1>
        <p>Choisis un match près de chez toi.</p>
      </header>

      <div className="search">
        <Input
          label="Rechercher par sport"
          id="search"
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Football, padel, running…"
        />
      </div>

      {status === "loading" && <p>Chargement des événements…</p>}

      {status === "error" && (
        <p role="alert">Impossible de charger les événements. Réessaie dans un instant.</p>
      )}

      {status === "ready" && events.length === 0 && (
        <p>
          Aucun événement à venir. <Link to="/events/new">Crée le premier.</Link>
        </p>
      )}

      {status === "ready" && events.length > 0 && (
        <>
          <p className="search__count" aria-live="polite">
            {filtered.length} {filtered.length > 1 ? "événements" : "événement"}
            {isSearching ? ` pour « ${query.trim()} »` : ""}
          </p>

          {filtered.length === 0 ? (
            <div className="search__empty">
              <p>Aucun événement ne correspond à ce sport.</p>
              <Button variant="secondary" onClick={() => setQuery("")}>
                Effacer la recherche
              </Button>
            </div>
          ) : (
            <ul className="event-list">
              {filtered.map((event) => (
                <li key={event.id}>
                  <EventCard event={event} />
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </>
  );
}