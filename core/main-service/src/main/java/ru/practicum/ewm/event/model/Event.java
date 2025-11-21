package ru.practicum.ewm.event.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.dto.event.EventState;
import ru.practicum.ewm.category.model.Category;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean paid = false;

    @Builder.Default
    @Column(name = "participant_limit", columnDefinition = "integer default 0")
    private Integer participantLimit = 0;

    @Builder.Default
    @Column(name = "request_moderation", columnDefinition = "boolean default true")
    private Boolean requestModeration = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventState state = EventState.PENDING;

    @Transient
    @Builder.Default
    private Integer confirmedRequests = 0;

    @Transient
    @Builder.Default
    private Integer views = 0;
}