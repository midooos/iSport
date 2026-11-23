package com.isport.dto;

import java.time.format.DateTimeFormatter;

import com.isport.entity.Event;
import com.isport.entity.User;
import com.isport.repository.EventSummary;

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

    // Depuis une ligne de la liste (GET /api/events), déjà calculée par la base
    public static EventResponse from(EventSummary row) {
        int participantsCount = row.participantsCount().intValue();

        return new EventResponse(
            row.id(),
            row.title(),
            row.sport(),
            row.startsAt().toLocalDate().toString(),
            row.startsAt().toLocalTime().format(TIME_FORMAT),
            row.place(),
            row.maxParticipants(),
            participantsCount,
            status(participantsCount, row.maxParticipants()),
            row.organizerId() == null ? null : new Organizer(row.organizerId(), row.organizerLogin()),
            Boolean.TRUE.equals(row.joined()));
    }

    private static String status(int participantsCount, int maxParticipants) {
        return participantsCount >= maxParticipants ? "COMPLET" : "OUVERT";
    }

    // Depuis l'entité (création, et plus tard inscription / désinscription)
    public static EventResponse from(Event event, Long currentUserId) {
        int participantsCount = event.getParticipants().size();
        String status = status(participantsCount, event.getMaxParticipants());

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
