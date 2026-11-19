package com.isport.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-hours}") long expirationHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationHours * 60 * 60 * 1000;
    }

    public String generateToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .issuedAt(now)
            .expiration(new Date(now.getTime() + expirationMillis))
            .signWith(key)
            .compact();
    }

    public Long extractUserId(String token) {
        try {
            if (!isCanonicalCompactJwt(token)) {
                return null;
            }

            String subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
            return Long.valueOf(subject);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isCanonicalCompactJwt(String token) {
        if (token == null) {
            return false;
        }

        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            return false;
        }

        Base64.Decoder decoder = Base64.getUrlDecoder();
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        for (String part : parts) {
            if (part.isEmpty()) {
                return false;
            }
            try {
                if (!encoder.encodeToString(decoder.decode(part)).equals(part)) {
                    return false;
                }
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return true;
    }
}
