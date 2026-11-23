package com.isport.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.isport.entity.User;
import com.isport.repository.UserRepository;
import com.isport.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventCreateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String suffix;
    private String token;

    @BeforeEach
    void setUp() {
        suffix = UUID.randomUUID().toString().substring(0, 8);
        User user = userRepository.save(new User("orga" + suffix, "orga" + suffix + "@mail.com", "hash"));
        token = "Bearer " + jwtService.generateToken(user.getId());
    }

    private static String json(String title, String sport, String date, String time, String location, Object max) {
        return """
            { "title": %s, "sport": %s, "date": %s, "time": %s, "location": %s, "maxParticipants": %s }
            """.formatted(quote(title), quote(sport), quote(date), quote(time), quote(location), max);
    }

    private static String quote(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    private String nextWeek() {
        return LocalDate.now().plusDays(7).toString();
    }

    private ResultActions create(String body) throws Exception {
        return mockMvc.perform(post("/api/events")
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
    }

    @Test
    void creationValideRenvoie201EtLObjetEvenement() throws Exception {
        String sport = "foot-" + suffix;
        create(json("  Match du samedi  ", sport, nextWeek(), "18:00", "Stade municipal", 10))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.title").value("Match du samedi"))
            .andExpect(jsonPath("$.sport").value(sport))
            .andExpect(jsonPath("$.date").value(nextWeek()))
            .andExpect(jsonPath("$.time").value("18:00"))
            .andExpect(jsonPath("$.location").value("Stade municipal"))
            .andExpect(jsonPath("$.maxParticipants").value(10))
            .andExpect(jsonPath("$.participantsCount").value(0))
            .andExpect(jsonPath("$.status").value("OUVERT"))
            .andExpect(jsonPath("$.organizer.login").value("orga" + suffix))
            .andExpect(jsonPath("$.joined").value(false));
    }

    @Test
    void lEvenementCreeApparaitDansLaListe() throws Exception {
        String sport = "padel-" + suffix;
        create(json("Tournoi", sport, nextWeek(), "10:30", "Club", 4))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/events").param("sport", sport).header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].title", hasItem("Tournoi")));
    }

    @Test
    void sansTokenRenvoie401() throws Exception {
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("Match", "Football", nextWeek(), "18:00", "Stade", 10)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void titreVideRenvoie400() throws Exception {
        create(json("   ", "Football", nextWeek(), "18:00", "Stade", 10))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Le titre est obligatoire"));
    }

    @Test
    void sportManquantRenvoie400() throws Exception {
        create(json("Match", null, nextWeek(), "18:00", "Stade", 10))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Le sport est obligatoire"));
    }

    @Test
    void dateManquanteRenvoie400() throws Exception {
        create(json("Match", "Football", null, "18:00", "Stade", 10))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("La date est obligatoire"));
    }

    @Test
    void heureManquanteRenvoie400() throws Exception {
        create(json("Match", "Football", nextWeek(), null, "Stade", 10))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("L'heure est obligatoire"));
    }

    @Test
    void lieuManquantRenvoie400() throws Exception {
        create(json("Match", "Football", nextWeek(), "18:00", null, 10))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Le lieu est obligatoire"));
    }

    @Test
    void moinsDe2ParticipantsRenvoie400() throws Exception {
        create(json("Match", "Football", nextWeek(), "18:00", "Stade", 1))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Il faut au moins 2 participants"));
    }

    @Test
    void nombreDeParticipantsManquantRenvoie400() throws Exception {
        create(json("Match", "Football", nextWeek(), "18:00", "Stade", null))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Le nombre de participants est obligatoire"));
    }

    @Test
    void dateDejaPasseeRenvoie400() throws Exception {
        create(json("Match", "Football", LocalDate.now().minusDays(1).toString(), "18:00", "Stade", 10))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("La date de l'événement est déjà passée"));
    }

    @Test
    void dateMalFormeeRenvoie400() throws Exception {
        create(json("Match", "Football", "05/12/2026", "18:00", "Stade", 10))
            .andExpect(status().isBadRequest());
    }
}
