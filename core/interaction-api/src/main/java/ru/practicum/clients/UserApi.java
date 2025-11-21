package ru.practicum.clients;

import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.user.UserDtoOut;

import java.util.Map;
import java.util.Set;

@FeignClient(name = "user-service", path = "/api/users")
public interface UserApi {

    @GetMapping("/{id}")
    UserDtoOut getUser(@PathVariable @NotNull Long id);

    @PostMapping
    Map<Long, UserDtoOut> getUsers(@RequestBody @NotNull Set<Long> ids);

    @GetMapping("/check-exists/{id}")
    boolean existsById(@PathVariable @NotNull Long id);
}
