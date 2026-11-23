package com.isport.repository;

import java.time.LocalDateTime;

// Une ligne de la liste du dashboard, lue en une seule requête SQL :
// le nombre de participants et « joined » sont calculés par la base,
// sans charger la liste des participants en mémoire.
public record EventSummary(
    Long id,
    String title,
    String sport,
    LocalDateTime startsAt,
    String place,
    Integer maxParticipants,
    Long organizerId,
    String organizerLogin,
    Number participantsCount,
    Boolean joined) {
}
