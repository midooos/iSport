package com.isport.controller;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.isport.entity.Event;
import com.isport.entity.User;
import com.isport.repository.EventRepository;
import com.isport.repository.UserRepository;
import com.isport.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventControllerTest {

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

    // Chaque test utilise des noms de sport uniques, car la base H2 est partagée entre les tests
    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = UUID.randomUUID().toString().substring(0, 8);
        user = userRepository.save(new User("user" + suffix, "user" + suffix + "@mail.com", "hash"));
        token = "Bearer " + jwtService.generateToken(user.getId());
    }

    private Event saveEvent(String title, String sport, LocalDateTime startsAt, int maxParticipants) {
        Event event = new Event(title, sport, startsAt, "Stade municipal", maxParticipants);
        event.setOrganizer(user);
        return eventRepository.save(event);
    }

    @Test
    void sansTokenRenvoie401() throws Exception {
        mockMvc.perform(get("/api/events"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void sansFiltreRenvoieTousLesSports() throws Exception {
        String foot = "foot-" + suffix;
        String padel = "padel-" + suffix;
        saveEvent("Match", foot, LocalDateTime.now().plusDays(1), 10);
        saveEvent("Tournoi", padel, LocalDateTime.now().plusDays(2), 4);

        mockMvc.perform(get("/api/events").header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].sport", hasItems(foot, padel)));
    }

    @Test
    void leFiltreParSportNeGardeQueCeSportEtIgnoreLesMajuscules() throws Exception {
        String foot = "foot-" + suffix;
        saveEvent("Match du soir", foot, LocalDateTime.now().plusDays(3), 10);
        saveEvent("Match du matin", foot, LocalDateTime.now().plusDays(1), 10);
        saveEvent("Tournoi", "padel-" + suffix, LocalDateTime.now().plusDays(2), 4);

        mockMvc.perform(get("/api/events")
                .param("sport", foot.toUpperCase())
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].title").value("Match du matin"))
            .andExpect(jsonPath("$[1].title").value("Match du soir"));
    }

    @Test
    void lesEvenementsPassesNeSontPasRenvoyes() throws Exception {
        String foot = "foot-" + suffix;
        saveEvent("Match d'hier", foot, LocalDateTime.now().minusDays(1), 10);
        saveEvent("Match de demain", foot, LocalDateTime.now().plusDays(1), 10);

        mockMvc.perform(get("/api/events").param("sport", foot).header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title").value("Match de demain"));
    }

    @Test
    void unSportSansEvenementRenvoieUneListeVide() throws Exception {
        mockMvc.perform(get("/api/events").param("sport", "inconnu-" + suffix).header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void lObjetEvenementSuitLeContrat() throws Exception {
        String foot = "foot-" + suffix;
        Event event = new Event("Match complet", foot, LocalDateTime.of(2099, 12, 5, 18, 0), "Stade municipal", 2);
        event.setOrganizer(user);
        event.getParticipants().add(user);
        event.getParticipants().add(userRepository.save(new User("autre" + suffix, "autre" + suffix + "@mail.com", "hash")));
        eventRepository.save(event);

        mockMvc.perform(get("/api/events").param("sport", foot).header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").isNumber())
            .andExpect(jsonPath("$[0].title").value("Match complet"))
            .andExpect(jsonPath("$[0].sport").value(foot))
            .andExpect(jsonPath("$[0].date").value("2099-12-05"))
            .andExpect(jsonPath("$[0].time").value("18:00"))
            .andExpect(jsonPath("$[0].location").value("Stade municipal"))
            .andExpect(jsonPath("$[0].maxParticipants").value(2))
            .andExpect(jsonPath("$[0].participantsCount").value(2))
            .andExpect(jsonPath("$[0].status").value("COMPLET"))
            .andExpect(jsonPath("$[0].organizer.login").value("user" + suffix))
            .andExpect(jsonPath("$[0].joined").value(true));
    }
}
