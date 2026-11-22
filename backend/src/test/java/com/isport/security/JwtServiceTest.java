package com.isport.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "un-secret-de-test-assez-long-pour-hs256-0123456789";

    @Test
    void leTokenContientLIdDeLUtilisateur() {
        JwtService jwt = new JwtService(SECRET, 24);
        String token = jwt.generateToken(42L);
        assertEquals(42L, jwt.extractUserId(token));
    }

    @Test
    void unTokenModifieEstRefuse() {
        JwtService jwt = new JwtService(SECRET, 24);
        String token = jwt.generateToken(42L);
        assertNull(jwt.extractUserId(token + "x"));
    }

    @Test
    void unTokenSigneAvecUnAutreSecretEstRefuse() {
        JwtService autre = new JwtService("un-autre-secret-de-test-assez-long-0123456789ab", 24);
        JwtService jwt = new JwtService(SECRET, 24);
        assertNull(jwt.extractUserId(autre.generateToken(42L)));
    }

    @Test
    void unTokenExpireEstRefuse() {
        JwtService jwt = new JwtService(SECRET, -1);
        assertNull(jwt.extractUserId(jwt.generateToken(42L)));
    }
}
