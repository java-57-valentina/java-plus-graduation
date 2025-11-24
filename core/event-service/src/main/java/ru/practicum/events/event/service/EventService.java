package ru.practicum.events.event.service;

import ru.practicum.dto.event.EventDto;
import ru.practicum.events.event.dto.EventDtoOut;
import ru.practicum.events.event.dto.EventShortDtoOut;
import ru.practicum.events.event.dto.*;
import ru.practicum.events.event.model.EventAdminFilter;
import ru.practicum.events.event.model.EventFilter;

import java.util.Collection;

public interface EventService {

    EventDtoOut add(Long userId, EventCreateDto eventDto);

    EventDtoOut update(Long userId, Long eventId, EventUpdateDto updateRequest);

    EventDtoOut update(Long eventId, EventUpdateAdminDto eventDto);

    EventDtoOut findPublished(Long eventId);

    EventDtoOut find(Long userId, Long eventId);

    EventDto findPlainDto(Long eventId, Long userId);

    Collection<EventShortDtoOut> findShortEventsBy(EventFilter filter);

    Collection<EventDtoOut> findFullEventsBy(EventAdminFilter filter);

    Collection<EventShortDtoOut> findByInitiator(Long userId, Integer offset, Integer limit);

    boolean existsByLocationId(Long id);
}
