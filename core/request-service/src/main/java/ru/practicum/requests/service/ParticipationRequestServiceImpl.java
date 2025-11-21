package ru.practicum.requests.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.clients.EventApi;
import ru.practicum.clients.UserApi;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventState;
import ru.practicum.exception.ConditionNotMetException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NoAccessException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.mapper.ParticipationRequestMapper;
import ru.practicum.requests.model.ParticipationRequest;
import ru.practicum.requests.model.RequestStatus;
import ru.practicum.requests.model.RequestsCount;
import ru.practicum.requests.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.requests.model.RequestStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository repository;

    private final UserApi userClient;
    private final EventApi eventClient;

    /**
     * Создает запрос на участие пользователя в событии.
     *
     * @param userId  ID пользователя, который хочет подать заявку
     * @param eventId ID события, в котором хотят участвовать
     * @return DTO созданной заявки
     * @ throws NotFoundException        если пользователь или событие не найдены
     * @ throws ConditionNotMetException если заявка уже существует, или инициатор пытается участвовать в своём событии,
     *                                  или событие не опубликовано, или достигнут лимит участников
     */
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        log.debug("Пользователь {} пытается создать запрос на участие в событии {}", userId, eventId);

        EventDto event = eventClient.getEvent(eventId, Optional.empty());
        checkRequestNotExists(userId, eventId);
        checkNotEventInitiator(userId, event);
        checkEventIsPublished(event);
        checkParticipantLimit(event, eventId);

        ParticipationRequest request = ParticipationRequest.builder()
                .requesterId(userId)
                .eventId(eventId)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))
                .build();

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration())
            request.setStatus(RequestStatus.CONFIRMED);

        log.debug("Создана заявка от пользователя {} на участие в событии {} со статусом {}", userId, eventId, request.getStatus());
        return ParticipationRequestMapper.toDto(repository.save(request));
    }

    /**
     * Получает список всех заявок текущего пользователя.
     *
     * @param userId ID пользователя
     * @return список DTO заявок
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        return repository.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    /**
     * Обновляет статус заявок на участие в событии (подтверждение или отклонение).
     * <p>
     * Проверяет, что:
     * - пользователь является инициатором события;
     * - событие опубликовано;
     * - все заявки находятся в статусе ожидания;
     * - не превышен лимит участников.
     * </p>
     *
     * @param initiatorId  ID пользователя (инициатора события)
     * @param eventId ID события
     * @param request объект с новыми статусами и списком ID заявок
     * @return результат изменения статусов (подтверждённые и отклонённые заявки)
     * @throws NotFoundException        если событие не найдено
     * @ throws ForbiddenException       если пользователь не является инициатором
     * @throws ConditionNotMetException если событие не опубликовано или заявки не в статусе ожидания
     * @throws IllegalArgumentException если передан неверный статус
     */
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatuses(Long initiatorId,
                                                                Long eventId,
                                                                EventRequestStatusUpdateRequest request) {
        EventDto event = eventClient.getEvent(eventId, Optional.of(initiatorId));
        checkEventIsPublished(event);

        List<ParticipationRequest> requests = getPendingRequestsOrThrow(request.getRequestIds());

        return switch (request.getStatus()) {
            case "CONFIRMED" -> confirmRequests(event, requests);
            case "REJECTED" -> rejectRequests(requests);
            default -> throw new IllegalArgumentException("Incorrect status: " + request.getStatus());
        };
    }

    @Override
    public Map<Long, Integer> getConfirmedRequests(Set<Long> ids) {
        List<RequestsCount> requestsCounts = repository.countConfirmedRequestsForEvents(ids);
        return requestsCounts.stream().collect(
                Collectors.toMap(RequestsCount::getId, RequestsCount::getCount));
    }

    /**
     * Получает список заявок на участие в событии, созданном указанным пользователем.
     *
     * @param eventId     ID события
     * @param initiatorId ID пользователя (инициатора события)
     * @return список заявок на участие в событии
     * @throws NotFoundException если событие или пользователь не найдены
     * @throws NoAccessException если пользователь не является инициатором события
     */
    @Override
    public List<ParticipationRequestDto> getRequestsForEvent(Long eventId, Long initiatorId) {
        log.debug("getRequestsForEvent: {} of user: {}", eventId, initiatorId);

        eventClient.getEvent(eventId, Optional.of(initiatorId));

        List<ParticipationRequest> allByEventId = repository.findAllByEventId(eventId);

        return allByEventId.stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    /**
     * Отменяет заявку пользователя на участие.
     *
     * @param userId    ID пользователя
     * @param requestId ID заявки на участие
     * @return DTO отменённой заявки
     * @throws NotFoundException  если заявка не найдена
     * @throws ForbiddenException если пользователь не является автором заявки
     */
    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.debug("Пользователь {} отменяет заявку с ID {}", userId, requestId);

        ParticipationRequest request = repository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("ParticipationRequest", requestId));

        if (!request.getRequesterId().equals(userId)) {
            throw new ForbiddenException("Only the author of the application can cancel it.");
        }

        request.setStatus(CANCELED);
        return ParticipationRequestMapper.toDto(repository.save(request));
    }

    // Проверка: заявка уже существует? Если да — кидаем ошибку (не надо дублировать).
    private void checkRequestNotExists(Long userId, Long eventId) {
        if (repository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConditionNotMetException("Participation request has already been sent.");
        }
    }

    // Проверяем, что инициатор события не пытается подать заявку на своё событие (это нечестно).
    private void checkNotEventInitiator(Long userId, EventDto event) {
        if (event.getInitiatorId().equals(userId)) {
            throw new ConditionNotMetException("Initiator cannot participate in their own event.");
        }
    }

    // Проверяем, что событие опубликовано (в смысле — не в черновике и не отменено).
    private void checkEventIsPublished(EventDto event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionNotMetException("Cannot participate in an unpublished event.");
        }
    }

    // Проверяем, не достигнут ли лимит участников события.
    private void checkParticipantLimit(EventDto event, Long eventId) {
        if (event.getParticipantLimit() == 0)
            return;

        long confirmed = repository.countByEventIdAndStatus(eventId, CONFIRMED);
        log.debug("event: {}, confirmed requests: {}", eventId, confirmed);
        if (confirmed >= event.getParticipantLimit()) {
            throw new ConditionNotMetException("Event participant limit has been reached.");
        }
    }

    /**
     * Проверяет, что все заявки находятся в статусе ожидания (PENDING).
     *
     * @param requestIds список ID заявок
     * @return список найденных заявок
     * @throws ConditionNotMetException если хотя бы одна заявка не в статусе PENDING
     */
    private List<ParticipationRequest> getPendingRequestsOrThrow(List<Long> requestIds) {
        List<ParticipationRequest> requests = repository.findAllById(requestIds);
        boolean hasNonPending = requests.stream()
                .anyMatch(r -> r.getStatus() != RequestStatus.PENDING);

        if (hasNonPending) {
            throw new ConditionNotMetException("Request must have status PENDING");
        }

        return requests;
    }

    /**
     * Подтверждает заявки на участие, если не превышен лимит участников.
     * Если лимит достигнут — остальные заявки отклоняются.
     *
     * @param event    событие, к которому относятся заявки
     * @param requests список заявок
     * @return результат обработки заявок
     */
    private EventRequestStatusUpdateResult confirmRequests(EventDto event, List<ParticipationRequest> requests) {
        checkIfLimitAvailableOrThrow(event);

        int limit = event.getParticipantLimit();
        long confirmedCount = repository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        int available = limit - (int) confirmedCount;

        List<ParticipationRequest> confirmed = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (shouldAutoConfirm(event)) {
                confirmRequest(request, confirmed);
            } else if (available > 0) {
                confirmRequest(request, confirmed);
                available--;
            } else {
                rejectRequest(request, rejected);
            }
        }

        repository.saveAll(requests);

        return new EventRequestStatusUpdateResult(
                confirmed.stream().map(ParticipationRequestMapper::toDto).toList(),
                rejected.stream().map(ParticipationRequestMapper::toDto).toList()
        );
    }

    /**
     * Проверяет, достигнут ли лимит участников события, и выбрасывает исключение, если да.
     */
    private void checkIfLimitAvailableOrThrow(EventDto event) {
        int limit = event.getParticipantLimit();
        long confirmedCount = repository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        if (limit != 0 && Boolean.TRUE.equals(event.getRequestModeration()) && confirmedCount >= limit) {
            throw new ConditionNotMetException("The limit of the participants of the event will reach");
        }
    }

    /**
     * Определяет, должна ли заявка подтверждаться автоматически.
     */
    private boolean shouldAutoConfirm(EventDto event) {
        return event.getParticipantLimit() == 0 || Boolean.FALSE.equals(event.getRequestModeration());
    }

    /**
     * Подтверждает заявку и добавляет её в список подтверждённых.
     */
    private void confirmRequest(ParticipationRequest request, List<ParticipationRequest> confirmed) {
        request.setStatus(RequestStatus.CONFIRMED);
        confirmed.add(request);
    }

    /**
     * Отклоняет заявку и добавляет её в список отклонённых.
     */
    private void rejectRequest(ParticipationRequest request, List<ParticipationRequest> rejected) {
        request.setStatus(RequestStatus.REJECTED);
        rejected.add(request);
    }


    /**
     * Массово отклоняет все переданные заявки.
     *
     * @param requests список заявок
     * @return результат с отклонёнными заявками
     */
    private EventRequestStatusUpdateResult rejectRequests(List<ParticipationRequest> requests) {
        for (ParticipationRequest r : requests) {
            r.setStatus(RequestStatus.REJECTED);
        }

        repository.saveAll(requests);

        return new EventRequestStatusUpdateResult(
                List.of(),
                requests.stream().map(ParticipationRequestMapper::toDto).toList()
        );
    }

}