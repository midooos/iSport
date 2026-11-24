package com.isport.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.isport.dto.CreateEventRequest;
import com.isport.dto.EventResponse;
import com.isport.service.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // GET /api/events et GET /api/events?sport=football
    // Le JwtAuthFilter place l'id de l'utilisateur connecté comme « principal »
    @GetMapping
    public List<EventResponse> list(@RequestParam(required = false) String sport,
                                    @AuthenticationPrincipal Long userId) {
        return eventService.findUpcoming(sport, userId);
    }

    // POST /api/events : route protégée (token obligatoire, voir SecurityConfig)
    // 201 + l'objet événement, 400 si un champ est invalide, 401 si pas connecté
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@Valid @RequestBody CreateEventRequest request,
                                @AuthenticationPrincipal Long userId) {
        return eventService.create(request, userId);
    }

    @PostMapping("/{id}/join")
    public EventResponse join(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return eventService.join(id, userId);
    }

    @PostMapping("/{id}/leave")
    public EventResponse leave(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return eventService.leave(id, userId);
    }
}
