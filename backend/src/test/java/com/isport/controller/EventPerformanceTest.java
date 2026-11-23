package com.isport.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.isport.entity.Event;
import com.isport.entity.User;
import com.isport.repository.EventRepository;
import com.isport.repository.UserRepository;
import com.isport.security.JwtService;

import jakarta.persistence.EntityManagerFactory;

// Le dashboard (GET /api/events) doit répondre en moins de 2 secondes,
// avec une seule requête SQL quel que soit le nombre d'événements et de participants.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventPerformanceTest {

    private static final int EVENTS = 300;
    private static final int USERS = 8;
    private static final long MAX_MILLIS = 2_000;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private String sport;
    private String token;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        sport = "perf-" + suffix;

        List<User> users = new ArrayList<>();
        for (int i = 0; i < USERS; i++) {
            users.add(userRepository.save(new User("perf" + i + suffix, "perf" + i + suffix + "@mail.com", "hash")));
        }
        token = "Bearer " + jwtService.generateToken(users.get(0).getId());

        // 300 événements à venir, chacun avec 0 à 7 participants
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < EVENTS; i++) {
            Event event = new Event("Match " + i, sport, LocalDateTime.now().plusHours(i + 1), "Stade", 10);
            event.setOrganizer(users.get(i % USERS));
            for (int j = 0; j < i % USERS; j++) {
                event.getParticipants().add(users.get(j));
            }
            events.add(event);
        }
        eventRepository.saveAll(events);
    }

    private Statistics statistics() {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        return statistics;
    }

    private long timeRequest(MockHttpServletRequestBuilder request, int expectedMinSize) throws Exception {
        long start = System.nanoTime();
        mockMvc.perform(request.header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(expectedMinSize))));
        return (System.nanoTime() - start) / 1_000_000;
    }

    @Test
    void laListeFiltreeFaitUneSeuleRequeteSqlEtRepondEnMoinsDe2Secondes() throws Exception {
        Statistics statistics = statistics();

        long millis = timeRequest(get("/api/events").param("sport", sport), EVENTS);

        assertEquals(1, statistics.getPrepareStatementCount(),
            "GET /api/events?sport= doit faire une seule requête SQL (pas de N+1)");
        assertTrue(millis < MAX_MILLIS, "Réponse en " + millis + " ms (maximum " + MAX_MILLIS + " ms)");
    }

    @Test
    void laListeCompleteFaitUneSeuleRequeteSqlEtRepondEnMoinsDe2Secondes() throws Exception {
        Statistics statistics = statistics();

        long millis = timeRequest(get("/api/events"), EVENTS);

        assertEquals(1, statistics.getPrepareStatementCount(),
            "GET /api/events doit faire une seule requête SQL (pas de N+1)");
        assertTrue(millis < MAX_MILLIS, "Réponse en " + millis + " ms (maximum " + MAX_MILLIS + " ms)");
    }

    @Test
    void lesCompteursRestentJustesAvecLaRequeteOptimisee() throws Exception {
        // L'événement n°3 a 3 participants (users 0, 1, 2) : l'utilisateur connecté (user 0) y est inscrit
        // L'événement n°7 a 7 participants sur 10 : toujours ouvert
        mockMvc.perform(get("/api/events").param("sport", sport).header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Match 0"))
            .andExpect(jsonPath("$[0].participantsCount").value(0))
            .andExpect(jsonPath("$[0].joined").value(false))
            .andExpect(jsonPath("$[3].title").value("Match 3"))
            .andExpect(jsonPath("$[3].participantsCount").value(3))
            .andExpect(jsonPath("$[3].joined").value(true))
            .andExpect(jsonPath("$[7].participantsCount").value(7))
            .andExpect(jsonPath("$[7].status").value("OUVERT"));
    }
}
