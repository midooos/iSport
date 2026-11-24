package com.isport.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.isport.dto.CreateEventRequest;
import com.isport.dto.EventResponse;
import com.isport.entity.Event;
import com.isport.entity.User;
import com.isport.exception.ApiException;
import com.isport.repository.EventRepository;
import com.isport.repository.EventSummary;
import com.isport.repository.UserRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    // US-05 : créer un événement ; l'utilisateur connecté en est l'organisateur
    @Transactional
    public EventResponse create(CreateEventRequest request, Long currentUserId) {
        LocalDateTime startsAt = LocalDateTime.of(request.date(), request.time());
        if (startsAt.isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La date de l'événement est déjà passée");
        }

        User organizer = userRepository.findById(currentUserId)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Vous devez être connecté"));

        Event event = new Event(
            request.title().trim(),
            request.sport().trim(),
            startsAt,
            request.location().trim(),
            request.maxParticipants());
        event.setOrganizer(organizer);

        return EventResponse.from(eventRepository.save(event), currentUserId);
    }

    @Transactional
    public EventResponse join(Long eventId, Long currentUserId) {
        Event event = eventRepository.findByIdForUpdate(eventId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Événement introuvable"));

        if (event.getParticipants().stream().anyMatch(user -> currentUserId.equals(user.getId()))) {
            throw new ApiException(HttpStatus.CONFLICT, "Vous participez déjà à cet événement");
        }
        if (event.getParticipants().size() >= event.getMaxParticipants()) {
            throw new ApiException(HttpStatus.CONFLICT, "L'événement est complet");
        }

        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Vous devez être connecté"));
        event.getParticipants().add(user);

        return EventResponse.from(event, currentUserId);
    }

    @Transactional
    public EventResponse leave(Long eventId, Long currentUserId) {
        Event event = eventRepository.findByIdForUpdate(eventId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Événement introuvable"));

        User participant = event.getParticipants().stream()
            .filter(user -> currentUserId.equals(user.getId()))
            .findFirst()
            .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "Vous ne participez pas à cet événement"));
        event.getParticipants().remove(participant);

        return EventResponse.from(event, currentUserId);
    }

    // US-06 et US-15 : événements à venir, éventuellement filtrés par sport
    @Transactional(readOnly = true)
    public List<EventResponse> findUpcoming(String sport, Long currentUserId) {
        LocalDateTime now = LocalDateTime.now();

        // Une seule requête SQL, quel que soit le nombre d'événements et de participants
        List<EventSummary> rows;
        if (sport == null || sport.isBlank()) {
            rows = eventRepository.findUpcoming(now, currentUserId);
        } else {
            rows = eventRepository.findUpcomingBySport(Event.toSportKey(sport), now, currentUserId);
        }

        return rows.stream()
            .map(EventResponse::from)
            .toList();
    }
}
