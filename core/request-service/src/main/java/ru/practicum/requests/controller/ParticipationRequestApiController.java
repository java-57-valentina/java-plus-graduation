package ru.practicum.requests.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.clients.RequestApi;
import ru.practicum.requests.service.ParticipationRequestService;

import java.util.Map;
import java.util.Set;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/requests")
public class ParticipationRequestApiController implements RequestApi {

    private final ParticipationRequestService service;

    @Override
    @PostMapping("/counts")
    public Map<Long, Integer> getConfirmedRequestsForEvents(@RequestBody @NotNull Set<Long> ids) {
        log.debug("api request for get confirmed requests for events: {}", ids);
        return service.getConfirmedRequests(ids);
    }
}
