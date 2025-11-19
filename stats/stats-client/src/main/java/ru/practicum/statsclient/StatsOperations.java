package ru.practicum.statsclient;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsdto.StatsDtoOut;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@FeignClient(name = "stats-server")
public interface StatsOperations {

    String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    void add(@RequestBody @Valid HitDto hitDto);

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    Collection<StatsDtoOut> select(
            @RequestParam @NotNull @DateTimeFormat(pattern = DATETIME_FORMAT) LocalDateTime start,
            @RequestParam @NotNull @DateTimeFormat(pattern = DATETIME_FORMAT) LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") Boolean unique);
}