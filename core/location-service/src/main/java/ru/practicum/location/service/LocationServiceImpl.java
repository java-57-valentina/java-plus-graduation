package ru.practicum.location.service;

import feign.FeignException;
import feign.RetryableException;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.clients.EventApi;
import ru.practicum.clients.UserApi;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.location.LocationDtoOut;
import ru.practicum.dto.user.UserDtoOut;
import ru.practicum.exception.ConditionNotMetException;
import ru.practicum.exception.DuplicateLocationsException;
import ru.practicum.exception.NoAccessException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.location.dto.*;
import ru.practicum.location.mapper.LocationMapper;
import ru.practicum.location.model.*;
import ru.practicum.location.repository.LocationRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    private static final double NEARBY_RADIUS = 50; // meters

    private final LocationRepository locationRepository;
    private final EventApi eventClient;

    private final UserApi userClient;

    @Override
    @Transactional
    public LocationFullDtoOut addLocationByAdmin(LocationCreateDto dto) {
        Location location = LocationMapper.fromDto(dto);
        location.setState(LocationState.APPROVED);
        return LocationMapper.toFullDto(locationRepository.save(location), null);
    }

    @Override
    @Transactional
    public LocationPrivateDtoOut addLocation(Long userId, LocationCreateDto dto) {
        if (!userClient.existsById(userId))
            throw new NotFoundException("User", userId);

        checkForDuplicate(dto.getName(), dto.getLatitude(), dto.getLongitude());

        Location location = LocationMapper.fromDto(dto);
        location.setCreatorId(userId);
        Location saved = locationRepository.save(location);
        return LocationMapper.toPrivateDto(saved);
    }

    @Override
    @Transactional
    public LocationFullDtoOut update(Long id, LocationUpdateAdminDto dto) {
        log.debug("update location by admin: {}", dto);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location", id));

        Optional.ofNullable(dto.getName()).ifPresent(location::setName);
        Optional.ofNullable(dto.getAddress()).ifPresent(location::setAddress);
        Optional.ofNullable(dto.getLatitude()).ifPresent(location::setLatitude);
        Optional.ofNullable(dto.getLongitude()).ifPresent(location::setLongitude);
        Optional.ofNullable(dto.getState()).ifPresent(
                state -> changeLocationState(location, state));

        UserDtoOut creator = getCreatorDto(location.getCreatorId());
        return LocationMapper.toFullDto(location, creator);
    }

    @Override
    @Transactional
    public LocationPrivateDtoOut update(Long id, Long userId, LocationUpdateUserDto dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location", id));

        if (location.getState() != LocationState.PENDING) {
            throw new ConditionNotMetException("Cannot update published or rejected location");
        }

        checkModificationAccess(location, userId, "edit");

        boolean needToCheckDuplicates =
                (dto.getName() != null && !dto.getName().equals(location.getName())) ||
                (dto.getLatitude() != null && !dto.getLatitude().equals(location.getLatitude())) ||
                (dto.getLongitude() != null && !dto.getLongitude().equals(location.getLongitude()));

        if (needToCheckDuplicates) {
            final String name = Optional.ofNullable(dto.getName()).orElse(location.getName());
            final Double lat = Optional.ofNullable(dto.getLatitude()).orElse(location.getLatitude());
            final Double lon = Optional.ofNullable(dto.getLongitude()).orElse(location.getLongitude());
            checkForDuplicate(name, lat, lon);
        }

        Optional.ofNullable(dto.getName()).ifPresent(location::setName);
        Optional.ofNullable(dto.getAddress()).ifPresent(location::setAddress);
        Optional.ofNullable(dto.getLatitude()).ifPresent(location::setLatitude);
        Optional.ofNullable(dto.getLongitude()).ifPresent(location::setLongitude);

        return LocationMapper.toPrivateDto(location);
    }

    @Override
    public LocationDtoOut getById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location", id));
        return LocationMapper.toDto(location);
    }

    private void checkForDuplicate(String name, Double lat, Double lon) {
        log.debug("need check  for duplicates");
        Optional<Location> existing = locationRepository.findDuplicates(name, lat, lon, NEARBY_RADIUS);

        if (existing.isPresent()) {
            log.warn("Nearby location: {}", existing.get());
            throw new DuplicateLocationsException(getDuplicateErrorMessage(existing.get()));
        }
    }

    @Override
    public LocationDtoOut getApproved(Long id) {
        Location location = locationRepository.findByIdAndState(id, LocationState.APPROVED)
                .orElseThrow(() -> new NotFoundException("Location", id));

        return LocationMapper.toDto(location);
    }

    private @Nullable UserDtoOut getCreatorDto(Long creatorId) {
        if (creatorId == null)
            return null;

        try {
            return userClient.getUser(creatorId);
        } catch (FeignException.NotFound e) {
            log.error("FeignException.NotFound");
            log.error("{}: Failed to get data of creator id: {} ({})", e.getClass(), creatorId, e.getMessage());
            return UserDtoOut.builder().id(creatorId).build();
        } catch (Exception e) {
            log.error("{}: Failed to get data of creator id: {} ({})", e.getClass(), creatorId, e.getMessage());
            return UserDtoOut.builder().id(creatorId).build();
        }
    }

    private void changeLocationState(Location location, LocationState state) {
        log.debug("changeLocationState id:{} state: {} -> {}", location.getId(), location.getState(), state);
        if (location.getState() == state)
            return;

        if (state == LocationState.PENDING || state == LocationState.AUTO_GENERATED) {
            throw new ConditionNotMetException(
                    String.format("Cannot change state from %s to %s", location.getState(), state));
        }
        location.setState(state);
    }

    private static String getDuplicateErrorMessage(@NotNull Location existing) {
        Long id = existing.getId();
        switch (existing.getState()) {
            case LocationState.APPROVED -> {
                return String.format("Please use existing location (id=%d)", id);
            }
            case LocationState.PENDING -> {
                return String.format("A request to create this location already exists (id=%d). Please wait for approval.", id);
            }
            case LocationState.REJECTED -> {
                return  "The request for creating this location was rejected earlier. Please contact admin.";
            }
        }
        return "";
    }

    @Override
    public Collection<LocationFullDtoOut> findAllByFilter(LocationAdminFilter filter) {
        Specification<Location> spec = buildSpecification(filter);
        List<Location> locations = locationRepository.findAll(spec, filter.getPageable()).getContent();
        return locations.stream()
                // TODO: fix it
                .map((Location location) -> LocationMapper.toFullDto(location, null))
                .toList();
    }

    @Override
    public Collection<LocationPrivateDtoOut> findAllByFilter(Long userId, LocationPrivateFilter filter) {

        if (!userClient.existsById(userId))
            throw new NotFoundException("User", userId);

        Specification<Location> spec = buildSpecification(userId, filter);
        List<Location> locations = locationRepository.findAll(spec, filter.getPageable()).getContent();
        return locations.stream()
                .map(LocationMapper::toPrivateDto)
                .toList();
    }

    @Override
    public Collection<LocationDtoOut> findAllByFilter(LocationPublicFilter filter) {
        Specification<Location> spec = buildSpecification(filter);
        List<Location> locations = locationRepository.findAll(spec, filter.getPageable()).getContent();
        return locations.stream()
                .map(LocationMapper::toDto)
                .toList();
    }

    @Override
    public Map<Long, LocationDtoOut> getLocations(Set<Long> ids) {
        List<Location> users = locationRepository.findAllById(ids);
        return users.stream().map(LocationMapper::toDto).collect(Collectors.toMap(
                LocationDtoOut::getId,
                Function.identity()
        ));
    }

    @Override
    public LocationFullDtoOut getByIdForAdmin(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location", id));

        UserDtoOut creator = getCreatorDto(location.getCreatorId());
        return LocationMapper.toFullDto(location, creator);
    }

    @Override
    @Transactional
    public LocationDtoOut getOrCreate(LocationDto location) {
        return LocationMapper.toDto(getOrCreateLocation(location));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        validateLocationHasNoEvents(id);
        locationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Location", id));

        if (location.getState() == LocationState.APPROVED) {
            throw new ConditionNotMetException("Cannot delete published location");
        }

        checkModificationAccess(location, userId, "delete");
        validateLocationHasNoEvents(id);

        locationRepository.deleteById(id);
    }

    private void validateLocationHasNoEvents(Long id) {
        boolean eventsExist = false;
        try {
            eventsExist = eventClient.existsByLocationId(id);
        } catch (FeignException e) {
            log.error("{}: {}", e.getClass(), e.getMessage());
            throw new ConditionNotMetException("Cannot check that location has no events");
        }
        if (eventsExist)
            throw new ConditionNotMetException("There are events in this location");
    }

    private void checkModificationAccess(Location location, Long userId, String action) {
        if (location.getCreatorId() == null || !location.getCreatorId().equals(userId)) {
            throw new NoAccessException("Only creator can " + action + " this location");
        }
    }

    private Location getOrCreateLocation(LocationDto location) {

        if (location.getId() != null) {
            return locationRepository.findByIdAndState(location.getId(), LocationState.APPROVED)
                    .orElseThrow(() -> new NotFoundException("Location", location.getId()));
        }

        if (location.getLatitude() != null && location.getLongitude() != null) {
            Optional<Location> nearByAutoGenerated = locationRepository.findNearByAutoGenerated(
                    location.getLatitude(), location.getLongitude());

            return nearByAutoGenerated.orElseGet(()
                    -> createAutoGeneratedLocation(location.getLatitude(), location.getLongitude()));
        }

        throw new ConditionNotMetException("Invalid location");
    }

    @Transactional
    private Location createAutoGeneratedLocation(Double lat, Double lon) {
        Location location = Location.builder()
                .latitude(lat)
                .longitude(lon)
                .state(LocationState.AUTO_GENERATED)
                .build();
        return locationRepository.save(location);
    }

    private Specification<Location> buildSpecification(LocationAdminFilter filter) {
        return Stream.of(
                        optionalSpec(LocationSpecifications.withTextContains(filter.getText())),
                        optionalSpec(LocationSpecifications.withCreator(filter.getCreator())),
                        optionalSpec(LocationSpecifications.withCoordinates(filter.getZone())),
                        optionalSpec(LocationSpecifications.withState(filter.getState()))
                )
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse((root, query, cb) -> cb.conjunction());
    }

    private Specification<Location> buildSpecification(Long userId, LocationPrivateFilter filter) {
        return Stream.of(
                        optionalSpec(LocationSpecifications.withCreator(userId)),
                        optionalSpec(LocationSpecifications.withState(filter.getState())),
                        optionalSpec(LocationSpecifications.withTextContains(filter.getText())),
                        optionalSpec(LocationSpecifications.withCoordinates(filter.getZone()))
                )
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse((root, query, cb) -> cb.conjunction());
    }

    private Specification<Location> buildSpecification(LocationPublicFilter filter) {
        return Stream.of(
                        optionalSpec(LocationSpecifications.withState(LocationState.APPROVED)),
                        optionalSpec(LocationSpecifications.withTextContains(filter.getText())),
                        optionalSpec(LocationSpecifications.withCoordinates(filter.getZone()))
                )
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse((root, query, cb) -> cb.conjunction());
    }

    private static <T> Specification<T> optionalSpec(Specification<T> spec) {
        return spec;
    }
}
