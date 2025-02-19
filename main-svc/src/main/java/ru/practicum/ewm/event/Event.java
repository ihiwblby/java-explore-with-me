package ru.practicum.ewm.event;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "annotation", nullable = false, length = 2000)
    String annotation;

//    @ManyToOne
//    @JoinColumn(name = "category_id")
//    Category category;

    @Column(name = "confirmed_requests")
    int confirmedRequests;

    @Column(name = "creation_date")
    LocalDateTime creationDate;

    @Column(name = "description", length = 7000)
    String description;

    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "initiator_id")
    User initiator;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "location_id")
    Location location;

    @Column(name = "paid")
    boolean paid;

    @Column(name = "participant_limit")
    int participantLimit;

    @Column(name = "published_date")
    LocalDateTime publishedDate;

    @Column(name = "request_moderation")
    boolean requestModeration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    EventStatus eventStatus;

    @Column(name = "title", nullable = false, length = 120)
    String title;
}