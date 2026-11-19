package com.isport.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static String registerJson(String login, String email, String password, String confirm) {
        return """
            { "login": "%s", "email": "%s", "password": "%s", "confirmPassword": "%s" }
            """.formatted(login, email, password, confirm);
    }

    private void register(String login, String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson(login, email, password, password)))
            .andExpect(status().isCreated());
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void inscriptionValideRenvoie201SansMotDePasse() throws Exception {
        String s = uniqueSuffix();
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson("user" + s, "user" + s + "@mail.com", "motdepasse1", "motdepasse1")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.login").value("user" + s))
            .andExpect(jsonPath("$.email").value("user" + s + "@mail.com"))
            .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void motDePasseTropCourtRenvoie400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson("court", "court@mail.com", "abc", "abc")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Le mot de passe doit contenir au moins 8 caractères"));
    }

    @Test
    void emailInvalideRenvoie400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson("bad", "pas-un-email", "motdepasse1", "motdepasse1")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("L'email n'est pas valide"));
    }

    @Test
    void confirmationDifferenteRenvoie400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson("conf", "conf@mail.com", "motdepasse1", "autrechose1")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("La confirmation ne correspond pas au mot de passe"));
    }

    @Test
    void emailDejaUtiliseRenvoie409() throws Exception {
        String s = uniqueSuffix();
        register("a" + s, "dup" + s + "@mail.com", "motdepasse1");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson("b" + s, "dup" + s + "@mail.com", "motdepasse1", "motdepasse1")))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Cet email est déjà utilisé"));
    }

    @Test
    void connexionValideRenvoieUnToken() throws Exception {
        String s = uniqueSuffix();
        register("log" + s, "log" + s + "@mail.com", "motdepasse1");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"email\": \"log" + s + "@mail.com\", \"password\": \"motdepasse1\" }"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.user.login").value("log" + s));
    }

    @Test
    void mauvaisMotDePasseRenvoie401() throws Exception {
        String s = uniqueSuffix();
        register("bad" + s, "bad" + s + "@mail.com", "motdepasse1");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"email\": \"bad" + s + "@mail.com\", \"password\": \"mauvais123\" }"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Email ou mot de passe incorrect"));
    }

    @Test
    void routeProtegeeSansTokenRenvoie401() throws Exception {
        mockMvc.perform(get("/api/route-protegee"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Vous devez être connecté"));
    }

    @Test
    void routeProtegeeAvecTokenInvalideRenvoie401() throws Exception {
        mockMvc.perform(get("/api/route-protegee").header("Authorization", "Bearer n-importe-quoi"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void routeProtegeeAvecBonTokenPasseLeFiltre() throws Exception {
        String s = uniqueSuffix();
        register("ok" + s, "ok" + s + "@mail.com", "motdepasse1");
        String reponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"email\": \"ok" + s + "@mail.com\", \"password\": \"motdepasse1\" }"))
            .andReturn().getResponse().getContentAsString();
        String token = reponse.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        // La route n'existe pas : avec un bon token on passe le filtre et on obtient 404 (et non 401)
        mockMvc.perform(get("/api/route-protegee").header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }
}
