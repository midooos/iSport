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

    // US-06 et US-15 : événements à venir, éventuellement filtrés par sport
    @Transactional(readOnly = true)
    public List<EventResponse> findUpcoming(String sport, Long currentUserId) {
        LocalDateTime now = LocalDateTime.now();

        List<Event> events;
        if (sport == null || sport.isBlank()) {
            events = eventRepository.findByStartsAtGreaterThanEqualOrderByStartsAtAsc(now);
        } else {
            events = eventRepository.findBySportIgnoreCaseAndStartsAtGreaterThanEqualOrderByStartsAtAsc(sport.trim(), now);
        }

        return events.stream()
            .map(event -> EventResponse.from(event, currentUserId))
            .toList();
    }
}
