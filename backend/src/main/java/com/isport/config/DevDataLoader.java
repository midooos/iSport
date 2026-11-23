package com.isport.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.isport.entity.Event;
import com.isport.entity.User;
import com.isport.repository.EventRepository;
import com.isport.repository.UserRepository;

// Données d'exemple, chargées uniquement avec le profil "dev"
// Compte de démo : demo@isport.com / motdepasse1
@Configuration
@Profile("dev")
public class DevDataLoader {

    @Bean
    CommandLineRunner loadDevData(UserRepository userRepository, EventRepository eventRepository,
                                  PasswordEncoder passwordEncoder) {
        return args -> {
            String password = passwordEncoder.encode("motdepasse1");
            User demo = userRepository.save(new User("demo", "demo@isport.com", password));
            User sami = userRepository.save(new User("sami", "sami@isport.com", password));
            User ines = userRepository.save(new User("ines", "ines@isport.com", password));

            // Football : un ouvert (demo inscrit), un complet
            Event foot = event("Foot à 5", "Football", 1, 18, 0, "Complexe du Lac 2, Tunis", 10, sami);
            foot.getParticipants().add(sami);
            foot.getParticipants().add(demo);
            eventRepository.save(foot);

            Event footComplet = event("Match du dimanche", "Football", 6, 10, 0, "Stade d'El Menzah", 2, ines);
            footComplet.getParticipants().add(ines);
            footComplet.getParticipants().add(sami);
            eventRepository.save(footComplet);

            eventRepository.save(event("Basket 3x3", "Basketball", 2, 10, 30, "Parc du Belvédère, Tunis", 6, ines));
            eventRepository.save(event("Sortie running", "Running", 3, 7, 0, "Corniche de La Marsa", 15, demo));
            eventRepository.save(event("Tournoi de padel", "Padel", 5, 17, 0, "Centre sportif El Menzah", 8, sami));
            eventRepository.save(event("Double mixte", "Tennis", 4, 9, 0, "Tennis club de Carthage", 4, ines));

            // Événement passé : ne doit pas apparaître dans la liste
            eventRepository.save(event("Match d'hier", "Football", -1, 18, 0, "Complexe du Lac 2, Tunis", 10, sami));
        };
    }

    private static Event event(String title, String sport, int inDays, int hour, int minute,
                               String place, int maxParticipants, User organizer) {
        LocalDateTime startsAt = LocalDateTime.of(LocalDate.now().plusDays(inDays), LocalTime.of(hour, minute));
        Event event = new Event(title, sport, startsAt, place, maxParticipants);
        event.setOrganizer(organizer);
        return event;
    }
}
