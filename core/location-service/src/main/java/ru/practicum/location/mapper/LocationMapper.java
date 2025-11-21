package ru.practicum.location.mapper;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import ru.practicum.dto.location.LocationDtoOut;
import ru.practicum.dto.user.UserDtoOut;
import ru.practicum.location.dto.LocationCreateDto;
import ru.practicum.location.dto.LocationFullDtoOut;
import ru.practicum.location.dto.LocationPrivateDtoOut;
import ru.practicum.location.model.Location;

@UtilityClass
public class LocationMapper {
    public static Location fromDto(LocationCreateDto dto) {
        return Location.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
    }

    public static LocationDtoOut toDto(Location location) {
        return LocationDtoOut.builder()
                .id(location.getId())
                .name(location.getName())
                .address(location.getAddress())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();
    }

    public static LocationFullDtoOut toFullDto(Location location, @Nullable UserDtoOut creator) {
        return LocationFullDtoOut.builder()
                .id(location.getId())
                .name(location.getName())
                .address(location.getAddress())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .creator(creator)
                .state(location.getState())
                .build();
    }

    public static LocationPrivateDtoOut toPrivateDto(Location location) {
        return LocationPrivateDtoOut.builder()
                .id(location.getId())
                .name(location.getName())
                .address(location.getAddress())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .state(location.getState())
                .build();
    }
}
