package com.isport.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.isport.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    // Événements à venir, du plus proche au plus lointain
    @EntityGraph(attributePaths = {"organizer", "participants"})
    List<Event> findByStartsAtGreaterThanEqualOrderByStartsAtAsc(LocalDateTime from);

    // Même chose, limité à un sport (sans tenir compte des majuscules)
    @EntityGraph(attributePaths = {"organizer", "participants"})
    List<Event> findBySportIgnoreCaseAndStartsAtGreaterThanEqualOrderByStartsAtAsc(String sport, LocalDateTime from);
}
