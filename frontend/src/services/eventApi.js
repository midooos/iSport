
const USE_MOCK = true;


// ======================================
// 1. LES EVENEMENTS DE TEST
// ======================================

const MOCK_EVENTS = [
    {
        id: 1,
        title: "Foot à 5",
        sport: "Football",
        place: "Complexe du Lac 2, Tunis",
        startsAt: "2026-11-21T18:00",
        joined: 8,
        max: 10,
        joinedByMe: false
    },
    {
        id: 2,
        title: "Basket 3x3",
        sport: "Basketball",
        place: "Parc du Belvédère, Tunis",
        startsAt: "2026-11-22T10:30",
        joined: 6,
        max: 6,
        joinedByMe: false
    },
    {
        id: 3,
        title: "Sortie running",
        sport: "Running",
        place: "Corniche de La Marsa",
        startsAt: "2026-11-25T07:00",
        joined: 3,
        max: 15,
        joinedByMe: true
    },
    {
        id: 4,
        title: "Tournoi de padel",
        sport: "Padel",
        place: "Centre sportif El Menzah",
        startsAt: "2026-11-28T17:00",
        joined: 2,
        max: 8,
        joinedByMe: false
    }
];


// ======================================
// 2. TRANSFORMER LES DONNEES DU BACKEND
// ======================================

function toEvent(event) {

    let joined = event.joined;
    let joinedByMe = event.joinedByMe;

    if (joined === null || joined === undefined) {
        joined = 0;
    }

    if (joinedByMe === null || joinedByMe === undefined) {
        joinedByMe = false;
    }

    const newEvent = {
        id: event.id,
        title: event.title,
        sport: event.sport,
        place: event.place,
        startsAt: event.startsAt,
        joined: joined,
        max: event.maxParticipants,
        joinedByMe: joinedByMe
    };

    return newEvent;
}


// ======================================
// 3. GERER LES HEADERS ET LE TOKEN
// ======================================

function authHeaders() {

    const token = localStorage.getItem("token");

    const headers = {
        "Content-Type": "application/json"
    };

    if (token !== null) {
        headers.Authorization = "Bearer " + token;
    }

    return headers;
}


// ======================================
// 4. ATTENDRE QUELQUES MILLISECONDES
// ======================================

function wait(milliseconds) {

    return new Promise(function(resolve) {
        setTimeout(resolve, milliseconds);
    });

}


// ======================================
// 5. RECUPERER LES EVENEMENTS
// ======================================

export async function fetchUpcomingEvents() {

    if (USE_MOCK === true) {

        await wait(400);

        const events = [];

        for (let i = 0; i < MOCK_EVENTS.length; i++) {

            const event = { ...MOCK_EVENTS[i] };

            events.push(event);
        }

        return events;

    } else {

        const response = await fetch("/api/events");

        if (response.ok === false) {
            throw new Error("Impossible de charger les événements.");
        }

        const data = await response.json();

        const events = [];

        for (let i = 0; i < data.length; i++) {

            const event = toEvent(data[i]);

            events.push(event);
        }

        return events;
    }

}


// ======================================
// 6. CREER UN EVENEMENT
// ======================================

export async function createEvent(values) {

    const payload = {
        title: values.title.trim(),
        sport: values.sport,
        startsAt: values.date + "T" + values.time,
        place: values.place.trim(),
        maxParticipants: Number(values.maxParticipants)
    };


    if (USE_MOCK === true) {

        await wait(400);

        const newEvent = {
            id: Date.now(),
            title: payload.title,
            sport: payload.sport,
            place: payload.place,
            startsAt: payload.startsAt,
            joined: 0,
            max: payload.maxParticipants,
            joinedByMe: false
        };

        MOCK_EVENTS.push(newEvent);

        return payload;

    } else {

        const response = await fetch("/api/events", {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify(payload)
        });

        if (response.status === 401) {
            throw new Error("Connecte-toi pour créer un événement.");
        }

        if (response.ok === false) {
            throw new Error("Impossible de créer l'événement. Vérifie les champs et réessaie.");
        }

        const data = await response.json();

        return data;
    }

}


// ======================================
// 7. REJOINDRE UN EVENEMENT
// ======================================

export async function joinEvent(id) {

    if (USE_MOCK === true) {

        await wait(300);

        let event = null;

        // Chercher l'événement avec son ID
        for (let i = 0; i < MOCK_EVENTS.length; i++) {

            if (MOCK_EVENTS[i].id === id) {
                event = MOCK_EVENTS[i];
            }

        }

        if (event === null) {
            throw new Error("Événement introuvable.");
        }

        if (event.joinedByMe === true) {
            throw new Error("Tu participes déjà à cet événement.");
        }

        if (event.joined >= event.max) {
            throw new Error("Cet événement est complet.");
        }

        // Ajouter un participant
        event.joined = event.joined + 1;

        // Indiquer que l'utilisateur participe
        event.joinedByMe = true;

        return { ...event };

    } else {

        const response = await fetch("/api/events/" + id + "/join", {
            method: "POST",
            headers: authHeaders()
        });

        if (response.status === 401) {
            throw new Error("Connecte-toi pour rejoindre un événement.");
        }

        if (response.status === 409) {
            throw new Error("Événement complet ou déjà rejoint.");
        }

        if (response.ok === false) {
            throw new Error("Impossible de rejoindre l'événement. Réessaie.");
        }

        const data = await response.json();

        return toEvent(data);
    }

}


// ======================================
// 8. QUITTER UN EVENEMENT
// ======================================

export async function leaveEvent(id) {

    if (USE_MOCK === true) {

        await wait(300);

        let event = null;

        // Chercher l'événement avec son ID
        for (let i = 0; i < MOCK_EVENTS.length; i++) {

            if (MOCK_EVENTS[i].id === id) {
                event = MOCK_EVENTS[i];
            }

        }

        if (event === null) {
            throw new Error("Événement introuvable.");
        }

        if (event.joinedByMe === false) {
            throw new Error("Tu ne participes pas à cet événement.");
        }

        // Retirer un participant
        event.joined = event.joined - 1;

        // Indiquer que l'utilisateur ne participe plus
        event.joinedByMe = false;

        return { ...event };

    } else {

        const response = await fetch("/api/events/" + id + "/leave", {
            method: "POST",
            headers: authHeaders()
        });

        if (response.status === 401) {
            throw new Error("Connecte-toi pour quitter un événement.");
        }

        if (response.ok === false) {
            throw new Error("Impossible de quitter l'événement. Réessaie.");
        }

        const data = await response.json();

        return toEvent(data);
    }

}

