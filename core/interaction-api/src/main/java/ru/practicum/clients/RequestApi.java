package ru.practicum.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

@FeignClient(name = "request-service", path = "/api/requests")
public interface RequestApi {

    @PostMapping("/counts")
    Map<Long, Integer> getConfirmedRequestsForEvents(@RequestBody @NotNull Set<Long> ids);
}
