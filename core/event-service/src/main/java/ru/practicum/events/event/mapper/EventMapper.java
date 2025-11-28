package ru.practicum.events.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.location.LocationDtoOut;
import ru.practicum.dto.user.UserDtoOut;
import ru.practicum.events.category.mapper.CategoryMapper;
import ru.practicum.events.event.dto.EventCreateDto;
import ru.practicum.events.event.dto.EventDtoOut;
import ru.practicum.events.event.dto.EventShortDtoOut;
import ru.practicum.events.event.model.Event;

@UtilityClass
public class EventMapper {
    public static Event fromDto(EventCreateDto eventDto) {
        return Event.builder()
                .annotation(eventDto.getAnnotation())
                .title(eventDto.getTitle())
                .paid(eventDto.getPaid())
                .eventDate(eventDto.getEventDate())
                .description(eventDto.getDescription())
                .participantLimit(eventDto.getParticipantLimit())
                .requestModeration(eventDto.getRequestModeration())
                .build();
    }

    public static EventDtoOut toDto(Event event, UserDtoOut userDto, LocationDtoOut location) {
        return EventDtoOut.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .title(event.getTitle())
                .category(CategoryMapper.toDto(event.getCategory()))
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .description(event.getDescription())
                .initiator(userDto)
                .createdOn(event.getCreatedAt())
                .state(event.getState())
                .confirmedRequests(event.getConfirmedRequests())
                .views(event.getViews())
                .location(location)
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .build();
    }

    public static EventShortDtoOut toShortDto(Event event, UserDtoOut userDto) {
        return EventShortDtoOut.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .title(event.getTitle())
                .category(CategoryMapper.toDto(event.getCategory()))
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .initiator(userDto)
                .confirmedRequests(event.getConfirmedRequests())
                .views(event.getViews())
                .build();
    }

    public static EventDto toPlainDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .title(event.getTitle())
                .categoryId(event.getCategory().getId())
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .description(event.getDescription())
                .initiatorId(event.getInitiatorId())
                .createdOn(event.getCreatedAt())
                .state(event.getState())
                .confirmedRequests(event.getConfirmedRequests())
                .views(event.getViews())
                .locationId(event.getLocationId())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .build();
    }
}
