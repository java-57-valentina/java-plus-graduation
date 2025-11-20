package ru.practicum.clients;

import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.event.EventDto;


@FeignClient(name = "events", path = "/api/events")
public interface EventApi {

    @GetMapping
    EventDto getEventWithCreator(@RequestParam @NotNull Long eventId,
                                 @RequestParam @NotNull Long userId);

    @GetMapping("/{eventId}")
    EventDto getEvent(@PathVariable @NotNull Long eventId);
}
