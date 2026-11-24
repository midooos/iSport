package com.isport.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.isport.entity.Event;
import com.isport.entity.User;
import com.isport.repository.EventRepository;
import com.isport.repository.UserRepository;
import com.isport.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventJoinLeaveTest {

    private static final int CONCURRENT_USERS = 10;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private User user;
    private String token;
    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = UUID.randomUUID().toString().substring(0, 8);
        user = saveUser("user");
        token = tokenFor(user);
    }

    private User saveUser(String prefix) {
        return userRepository.save(new User(
            prefix + suffix,
            prefix + suffix + "@mail.com",
            "hash"));
    }

    private String tokenFor(User user) {
        return "Bearer " + jwtService.generateToken(user.getId());
    }

    private Event saveEvent(int maxParticipants, User... participants) {
        Event event = new Event(
            "Match " + suffix,
            "sport-" + suffix,
            LocalDateTime.now().plusDays(1),
            "Stade municipal",
            maxParticipants);
        event.setOrganizer(user);
        for (User participant : participants) {
            event.getParticipants().add(participant);
        }
        return eventRepository.save(event);
    }

    private ResultActions join(Long eventId, String authorization) throws Exception {
        return mockMvc.perform(post("/api/events/{id}/join", eventId)
            .header("Authorization", authorization));
    }

    private ResultActions leave(Long eventId, String authorization) throws Exception {
        return mockMvc.perform(post("/api/events/{id}/leave", eventId)
            .header("Authorization", authorization));
    }

    @Test
    void rejoindreAjouteLeParticipantEtRenvoieSonEtat() throws Exception {
        Event event = saveEvent(4);

        join(event.getId(), token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.participantsCount").value(1))
            .andExpect(jsonPath("$.joined").value(true))
            .andExpect(jsonPath("$.status").value("OUVERT"));
    }

    @Test
    void rejoindreDeuxFoisRenvoie409() throws Exception {
        Event event = saveEvent(4);

        join(event.getId(), token).andExpect(status().isOk());
        join(event.getId(), token)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Vous participez déjà à cet événement"));
    }

    @Test
    void rejoindreUnEvenementCompletRenvoie409EtLeStatutComplet() throws Exception {
        Event event = saveEvent(2, saveUser("autre"), saveUser("encore"));

        join(event.getId(), token)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("L'événement est complet"));

        mockMvc.perform(get("/api/events")
                .param("sport", event.getSport())
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("COMPLET"));
    }

    @Test
    void quitterRetireLeParticipantEtRenvoieSonEtat() throws Exception {
        Event event = saveEvent(4, user);

        leave(event.getId(), token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.participantsCount").value(0))
            .andExpect(jsonPath("$.joined").value(false))
            .andExpect(jsonPath("$.status").value("OUVERT"));
    }

    @Test
    void quitterSansEtreInscritRenvoie409() throws Exception {
        Event event = saveEvent(4);

        leave(event.getId(), token)
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Vous ne participez pas à cet événement"));
    }

    @Test
    void quitterUnEvenementCompletLeRouvre() throws Exception {
        Event event = saveEvent(2, user, saveUser("autre"));

        leave(event.getId(), token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.participantsCount").value(1))
            .andExpect(jsonPath("$.joined").value(false))
            .andExpect(jsonPath("$.status").value("OUVERT"));
    }

    @Test
    void evenementInconnuRenvoie404() throws Exception {
        join(Long.MAX_VALUE, token).andExpect(status().isNotFound());
        leave(Long.MAX_VALUE, token).andExpect(status().isNotFound());
    }

    @Test
    void routesSansTokenRenvoient401() throws Exception {
        mockMvc.perform(post("/api/events/{id}/join", 1))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/events/{id}/leave", 1))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void inscriptionsSimultaneesRespectentLaCapacite() throws Exception {
        Event event = saveEvent(3);
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        CountDownLatch ready = new CountDownLatch(CONCURRENT_USERS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> responses = new ArrayList<>();

        try {
            for (int i = 0; i < CONCURRENT_USERS; i++) {
                String participantToken = tokenFor(saveUser("participant" + i));
                responses.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Le démarrage des requêtes a expiré");
                    }
                    return mockMvc.perform(post("/api/events/{id}/join", event.getId())
                            .header("Authorization", participantToken))
                        .andReturn()
                        .getResponse()
                        .getStatus();
                }));
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();

            long successfulResponses = 0;
            long conflictResponses = 0;
            for (Future<Integer> response : responses) {
                int responseStatus = response.get(30, TimeUnit.SECONDS);
                if (responseStatus == 200) {
                    successfulResponses++;
                } else if (responseStatus == 409) {
                    conflictResponses++;
                }
            }

            assertEquals(3, successfulResponses);
            assertEquals(7, conflictResponses);
            mockMvc.perform(get("/api/events")
                    .param("sport", event.getSport())
                    .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].participantsCount").value(3))
                .andExpect(jsonPath("$[0].status").value("COMPLET"));
        } finally {
            executor.shutdownNow();
        }
    }
}
