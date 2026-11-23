package com.isport.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.isport.repository.EventRepository;

// Au démarrage : remplit la colonne sport_key des événements déjà en base
// (bases PostgreSQL locales créées avant l'ajout de cette colonne).
// Ne fait rien si tout est déjà rempli.
@Component
public class SportKeyBackfill {

    private final EventRepository eventRepository;

    public SportKeyBackfill(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void fillMissingSportKeys() {
        eventRepository.fillMissingSportKeys();
    }
}
