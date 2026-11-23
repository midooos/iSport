package com.isport.dto;

import java.time.format.DateTimeFormatter;

import com.isport.entity.Event;
import com.isport.entity.User;

// Objet événement tel que décrit dans docs/api-contract.md
public record EventResponse(
    Long id,
    String title,
    String sport,
    String date,
    String time,
    String location,
    int maxParticipants,
    int participantsCount,
    String status,
    Organizer organizer,
    boolean joined) {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public record Organizer(Long id, String login) {
    }

    public static EventResponse from(Event event, Long currentUserId) {
        int participantsCount = event.getParticipants().size();
        String status = participantsCount >= event.getMaxParticipants() ? "COMPLET" : "OUVERT";

        boolean joined = currentUserId != null && event.getParticipants().stream()
            .anyMatch(user -> currentUserId.equals(user.getId()));

        User organizer = event.getOrganizer();

        return new EventResponse(
            event.getId(),
            event.getTitle(),
            event.getSport(),
            event.getStartsAt().toLocalDate().toString(),
            event.getStartsAt().toLocalTime().format(TIME_FORMAT),
            event.getPlace(),
            event.getMaxParticipants(),
            participantsCount,
            status,
            organizer == null ? null : new Organizer(organizer.getId(), organizer.getLogin()),
            joined);
    }
}
