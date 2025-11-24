package ru.practicum.events.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.dto.user.UserDtoOut;
import ru.practicum.events.category.dto.CategoryDtoOut;
import ru.practicum.events.constants.Constants;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDtoOut {

    private Long id;
    private String title;
    private String annotation;
    private CategoryDtoOut category;
    private UserDtoOut initiator;

    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    private Boolean paid;
    private Integer confirmedRequests;

    @Builder.Default
    private Integer views = 0;
}