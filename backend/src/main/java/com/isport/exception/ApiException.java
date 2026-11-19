package com.isport.exception;

import org.springframework.http.HttpStatus;

// Erreur renvoyée au client avec un code HTTP et un message lisible
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
