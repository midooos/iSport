const USE_MOCK = true; // passer à false quand KAN-51 (GET) et KAN-48 (POST) sont terminés

const MOCK_EVENTS = [
  { id: 1, title: "Foot à 5", sport: "Football", place: "Complexe du Lac 2, Tunis", startsAt: "2026-11-21T18:00", joined: 8, max: 10 },
  { id: 2, title: "Basket 3x3", sport: "Basketball", place: "Parc du Belvédère, Tunis", startsAt: "2026-11-22T10:30", joined: 6, max: 6 },
  { id: 3, title: "Sortie running", sport: "Running", place: "Corniche de La Marsa", startsAt: "2026-11-25T07:00", joined: 3, max: 15 },
  { id: 4, title: "Tournoi de padel", sport: "Padel", place: "Centre sportif El Menzah", startsAt: "2026-11-28T17:00", joined: 2, max: 8 },
];

// À adapter aux champs réels de l'entité Event (KAN-30, elhoussine)
const toEvent = (e) => ({
  id: e.id,
  title: e.title,
  sport: e.sport,
  place: e.place,
  startsAt: e.startsAt,
  joined: e.joined ?? 0,
  max: e.maxParticipants,
});

export async function fetchUpcomingEvents() {
  if (USE_MOCK) return MOCK_EVENTS;

  const response = await fetch("/api/events");
  if (!response.ok) throw new Error("Impossible de charger les événements.");
  const data = await response.json();
  return data.map(toEvent);
}

// values vient du formulaire : title, sport, date, time, place, maxParticipants
export async function createEvent(values) {
  const payload = {
    title: values.title.trim(),
    sport: values.sport,
    startsAt: `${values.date}T${values.time}`,
    place: values.place.trim(),
    maxParticipants: Number(values.maxParticipants),
  };

  if (USE_MOCK) {
    await new Promise((resolve) => setTimeout(resolve, 400));
    MOCK_EVENTS.push({
      id: Date.now(),
      title: payload.title,
      sport: payload.sport,
      place: payload.place,
      startsAt: payload.startsAt,
      joined: 0,
      max: payload.maxParticipants,
    });
    return payload;
  }

  // La clé "token" est à confirmer avec yessine (KAN-42)
  const token = localStorage.getItem("token");
  const response = await fetch("/api/events", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(payload),
  });

  if (response.status === 401) {
    throw new Error("Connecte-toi pour créer un événement.");
  }
  if (!response.ok) {
    throw new Error("Impossible de créer l'événement. Vérifie les champs et réessaie.");
  }
  return response.json();
}