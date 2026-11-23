package com.isport.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.isport.dto.EventResponse;
import com.isport.service.EventService;

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
}
