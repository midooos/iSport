package com.isport.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Corps de POST /api/events (voir docs/api-contract.md)
// La date s'écrit "2026-12-05" et l'heure "18:00"
public record CreateEventRequest(

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 120, message = "Le titre ne doit pas dépasser 120 caractères")
    String title,

    @NotBlank(message = "Le sport est obligatoire")
    @Size(max = 50, message = "Le sport ne doit pas dépasser 50 caractères")
    String sport,

    @NotNull(message = "La date est obligatoire")
    LocalDate date,

    @NotNull(message = "L'heure est obligatoire")
    LocalTime time,

    @NotBlank(message = "Le lieu est obligatoire")
    @Size(max = 200, message = "Le lieu ne doit pas dépasser 200 caractères")
    String location,

    @NotNull(message = "Le nombre de participants est obligatoire")
    @Min(value = 2, message = "Il faut au moins 2 participants")
    Integer maxParticipants) {
}
