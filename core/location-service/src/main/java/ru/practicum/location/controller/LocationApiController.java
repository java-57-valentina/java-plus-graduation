package ru.practicum.location.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.clients.LocationApi;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.LocationDtoOut;
import ru.practicum.location.service.LocationService;

import java.util.Map;
import java.util.Set;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/locations")
public class LocationApiController implements LocationApi {

    private final LocationService service;

    @Override
    @PostMapping("/get-or-create")
    public LocationDtoOut getOrCreateLocation(@RequestBody @Valid @NotNull LocationDto location) {
        return service.getOrCreate(location);
    }

    @Override
    @GetMapping("/{id}")
    public LocationDtoOut getLocation(@PathVariable Long id) {
        return service.getById(id);
    }

    @Override
    @PostMapping
    public Map<Long, LocationDtoOut> getLocations(@RequestBody @NotNull Set<Long> locationsIds) {
        return service.getLocations(locationsIds);
    }
}
