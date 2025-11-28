package ru.practicum.dto.event;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {

    private Long id;
    private String title;
    private String annotation;
    private String description;

    private Long categoryId;
    private Long initiatorId;
    private Long locationId;

    private LocalDateTime eventDate;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;

    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private EventState state;
    private Integer confirmedRequests;

    @Builder.Default
    private Integer views = 0;
}