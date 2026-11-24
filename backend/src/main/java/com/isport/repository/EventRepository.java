package com.isport.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.isport.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);

    // Une seule requête SQL par appel :
    // - l'organisateur est lu par jointure
    // - le nombre de participants et « joined » sont des sous-requêtes sur event_participants
    //   (clé primaire event_id, user_id), au lieu de charger tous les participants
    String UPCOMING_SUMMARY = """
        select new com.isport.repository.EventSummary(
            e.id, e.title, e.sport, e.startsAt, e.place, e.maxParticipants,
            o.id, o.login,
            size(e.participants),
            case when exists (
                select 1 from Event e2 join e2.participants p
                where e2.id = e.id and p.id = :userId
            ) then true else false end)
        from Event e
        left join e.organizer o
        """;

    // Événements à venir, du plus proche au plus lointain (index idx_events_starts_at)
    @Query(UPCOMING_SUMMARY + " where e.startsAt >= :from order by e.startsAt asc, e.id asc")
    List<EventSummary> findUpcoming(@Param("from") LocalDateTime from, @Param("userId") Long userId);

    // Même chose, pour un seul sport (index idx_events_sport_key_starts_at)
    @Query(UPCOMING_SUMMARY + " where e.sportKey = :sportKey and e.startsAt >= :from order by e.startsAt asc, e.id asc")
    List<EventSummary> findUpcomingBySport(@Param("sportKey") String sportKey,
                                           @Param("from") LocalDateTime from,
                                           @Param("userId") Long userId);

    // Remplit sport_key pour les événements créés avant l'ajout de cette colonne
    @Modifying
    @Query("update Event e set e.sportKey = lower(trim(e.sport)) where e.sportKey is null")
    int fillMissingSportKeys();
}
