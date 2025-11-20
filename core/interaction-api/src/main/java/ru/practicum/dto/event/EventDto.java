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
//    private CategoryDtoOut category;
    // private UserDtoOut initiator;
    private Long initiatorId;
//    private LocationDtoOut location;

    // @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    // @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    private LocalDateTime createdOn;

    // @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    private LocalDateTime publishedOn;

    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private EventState state;
    private Integer confirmedRequests;

    @Builder.Default
    private Integer views = 0;
}