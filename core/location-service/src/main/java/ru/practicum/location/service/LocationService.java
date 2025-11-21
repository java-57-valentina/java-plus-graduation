package ru.practicum.location.service;


import jakarta.validation.constraints.NotNull;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.LocationDtoOut;
import ru.practicum.location.dto.*;
import ru.practicum.location.model.LocationAdminFilter;
import ru.practicum.location.model.LocationPrivateFilter;
import ru.practicum.location.model.LocationPublicFilter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface LocationService {

    LocationPrivateDtoOut addLocation(Long userId, LocationCreateDto dto);

    LocationFullDtoOut addLocationByAdmin(LocationCreateDto dto);

    LocationFullDtoOut update(Long id, LocationUpdateAdminDto dto);

    LocationPrivateDtoOut update(Long id, Long userId, LocationUpdateUserDto dto);

    LocationDtoOut getById(Long id);

    LocationDtoOut getApproved(Long id);

    LocationFullDtoOut getByIdForAdmin(Long id);

    LocationDtoOut getOrCreate(LocationDto location);

    Collection<LocationFullDtoOut> findAllByFilter(LocationAdminFilter filter);

    Collection<LocationPrivateDtoOut> findAllByFilter(Long userId, LocationPrivateFilter filter);

    Collection<LocationDtoOut> findAllByFilter(LocationPublicFilter filter);

    Map<Long, LocationDtoOut> getLocations(@NotNull Set<Long> locationsIds);

    void delete(Long id);

    void delete(Long id, Long userId);
}
