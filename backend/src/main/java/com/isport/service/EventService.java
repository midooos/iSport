package com.isport.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.isport.dto.EventResponse;
import com.isport.entity.Event;
import com.isport.repository.EventRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
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
