package ru.practicum.events.event.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.clients.EventApi;
import ru.practicum.dto.event.EventDto;
import ru.practicum.events.event.service.EventService;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventApiController  implements EventApi {

    private final EventService eventService;

    @Override
    @GetMapping("/{eventId}")
    public EventDto getEvent(@PathVariable @NotNull Long eventId,
                             @RequestParam Optional<Long> userId) {
        log.debug("api request for get event {} {}", eventId,
                userId.map(aLong -> "of user " + aLong).orElse(""));
        return eventService.findPlainDto(eventId, userId.orElse(null));
    }

    @Override
    @GetMapping("/check-exists/{id}")
    public boolean existsByLocationId(Long id) {
        return eventService.existsByLocationId(id);
    }
}
