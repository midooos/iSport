package com.isport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

// Index pour la liste du dashboard (GET /api/events) :
// - starts_at : « à venir » (starts_at >= maintenant) + tri par date
// - (sport_key, starts_at) : filtre par sport + tri par date, en un seul parcours d'index
// - organizer_id : clé étrangère (PostgreSQL ne l'indexe pas tout seul)
@Entity
@Table(name = "events", indexes = {
    @Index(name = "idx_events_starts_at", columnList = "starts_at"),
    @Index(name = "idx_events_sport_key_starts_at", columnList = "sport_key, starts_at"),
    @Index(name = "idx_events_organizer_id", columnList = "organizer_id")
})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 50)
    private String sport;

    // Sport en minuscules, pour filtrer sans tenir compte des majuscules tout en utilisant un index
    // (un « upper(sport) = upper(?) » ne peut pas utiliser un index classique)
    @Column(name = "sport_key", length = 50)
    private String sportKey;

    // Date et heure de l'événement en un seul champ (le front envoie « startsAt »)
    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false, length = 200)
    private String place;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Utilisateur qui a créé l'événement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    // Utilisateurs inscrits à l'événement
    // La clé primaire (event_id, user_id) sert au comptage des participants et à « joined » ;
    // l'index (user_id, event_id) sert à retrouver les événements d'un utilisateur
    @ManyToMany
    @JoinTable(
        name = "event_participants",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"),
        indexes = @Index(name = "idx_event_participants_user_id", columnList = "user_id, event_id"))
    private Set<User> participants = new HashSet<>();

    protected Event() {
        // requis par JPA
    }

    public Event(String title, String sport, LocalDateTime startsAt, String place, int maxParticipants) {
        this.title = title;
        setSport(sport);
        this.startsAt = startsAt;
        this.place = place;
        this.maxParticipants = maxParticipants;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSport() { return sport; }
    public void setSport(String sport) {
        this.sport = sport;
        this.sportKey = toSportKey(sport);
    }

    public static String toSportKey(String sport) {
        return sport == null ? null : sport.trim().toLowerCase(Locale.ROOT);
    }

    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public User getOrganizer() { return organizer; }
    public void setOrganizer(User organizer) { this.organizer = organizer; }

    public Set<User> getParticipants() { return participants; }
}
