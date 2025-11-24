package ru.practicum.events.event.controller;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventState;
import ru.practicum.events.event.dto.EventDtoOut;
import ru.practicum.events.event.dto.EventShortDtoOut;
import ru.practicum.events.event.model.EventFilter;
import ru.practicum.events.event.service.EventService;
import ru.practicum.exception.InvalidRequestException;
import ru.practicum.statsclient.StatsOperations;
import ru.practicum.statsdto.HitDto;

import static ru.practicum.events.constants.Constants.DATE_TIME_FORMAT;
import static ru.practicum.events.constants.Constants.STATS_EVENTS_URL;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;
    private final StatsOperations statsClient;

    @Value("${spring.application.name:ewm}")
    private String appName;

    // Получение событий с возможностью фильтрации
    @GetMapping
    public Collection<EventShortDtoOut> getEvents(
            @Size(min = 3, max = 1000, message = "Текст должен быть длиной от 3 до 1000 символов")
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) Long location,
//            @RequestParam(required = false) @DecimalMin("-90.0")  @DecimalMax("90.0")  Double lat,
//            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double lon,
            @RequestParam(defaultValue = "10.0") @DecimalMin("0.0") Double radius,
            @RequestParam(defaultValue = "EVENT_DATE") String sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        EventFilter filter = EventFilter.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .locationId(location)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .state(EventState.PUBLISHED)
                .build();

        log.debug("request for getting events (public)");

//        if (lat != null && lon != null)
//            filter.setZone(new Zone(lat, lon, radius));

        if (filter.getRangeStart() != null && filter.getRangeEnd() != null
                && filter.getRangeStart().isAfter(filter.getRangeEnd())) {
            throw new InvalidRequestException("The start date of the range must be earlier than the end date.");
        }

        Collection<EventShortDtoOut> events = eventService.findShortEventsBy(filter);

        Collection<Long> ids = events.stream()
                .map(EventShortDtoOut::getId)
                .toList();

        writeStatisticsByIds(ids, request.getRemoteAddr());
        writeStatisticsByUris(List.of("/events"), request.getRemoteAddr());

        return events;
    }

    @GetMapping("/{eventId}")
    public EventDtoOut get(@PathVariable @Min(1) Long eventId,
                           HttpServletRequest request) {

        log.debug("request for published event id:{}", eventId);
        EventDtoOut dtoOut = eventService.findPublished(eventId);

        writeStatisticsByIds(List.of(eventId), request.getRemoteAddr());

        return dtoOut;
    }

    private void writeStatisticsByIds(Collection<Long> ids, String ip) {
        writeStatisticsByUris(ids.stream().map(id -> STATS_EVENTS_URL + id).toList(), ip);
    }

    private void writeStatisticsByUris(Collection<String> uris, String ip) {
        try {
            for (String uri : uris)
                statsClient.add(new HitDto(appName, uri, ip, LocalDateTime.now()));

        } catch (FeignException ex) {
            log.error(ex.getMessage());
        }
    }
}
