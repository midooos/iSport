package com.isport.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.isport.entity.User;
import com.isport.repository.UserRepository;
import com.isport.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void renvoieLeProfilDeLUtilisateurConnecteSansMotDePasse() throws Exception {
        String s = UUID.randomUUID().toString().substring(0, 8);
        User user = userRepository.save(new User("user" + s, "user" + s + "@mail.com", "hash-secret"));

        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + jwtService.generateToken(user.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(user.getId()))
            .andExpect(jsonPath("$.login").value("user" + s))
            .andExpect(jsonPath("$.email").value("user" + s + "@mail.com"))
            .andExpect(content().string(not(containsString("password"))))
            .andExpect(content().string(not(containsString("hash-secret"))));
    }

    @Test
    void sansTokenRenvoie401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void avecUnTokenInvalideRenvoie401() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer pas-un-vrai-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void avecLeTokenDUnCompteInexistantRenvoie401() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + jwtService.generateToken(999_999L)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Vous devez être connecté"));
    }
}
