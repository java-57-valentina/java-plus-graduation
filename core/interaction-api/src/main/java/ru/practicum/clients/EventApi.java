package ru.practicum.clients;

import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.event.EventDto;

import java.util.Optional;


@FeignClient(name = "main-service", path = "/api/events")
public interface EventApi {

    @GetMapping("/{eventId}")
    EventDto getEvent(@PathVariable @NotNull Long eventId,
                      @RequestParam(required = false) Optional<Long> userId);
}
