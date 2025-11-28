package ru.practicum.clients;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.LocationDtoOut;

import java.util.Map;
import java.util.Set;

@Validated
@FeignClient(name = "location-service", path = "/api/locations")
public interface LocationApi {

    @PostMapping("/get-or-create")
    LocationDtoOut getOrCreateLocation(@RequestBody @Valid @NotNull LocationDto location);

    @GetMapping("/{id}")
    LocationDtoOut getLocation(@PathVariable Long id);

    @PostMapping
    Map<Long, LocationDtoOut> getLocations(@RequestBody @NotNull Set<Long> locationsIds);
}
