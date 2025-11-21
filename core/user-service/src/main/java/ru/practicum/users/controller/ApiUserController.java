package ru.practicum.users.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.clients.UserApi;
import ru.practicum.dto.user.UserDtoOut;
import ru.practicum.users.service.UserService;

import java.util.Map;
import java.util.Set;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ApiUserController implements UserApi {

    private final UserService userService;

    @Override
    @GetMapping("/{id}")
    public UserDtoOut getUser(@PathVariable @NotNull Long id) {
        return userService.getUser(id);
    }

    @Override
    @PostMapping
    public Map<Long, UserDtoOut> getUsers(@RequestBody @NotNull Set<Long> ids) {
        log.debug("api request for get confirmed requests for events: {}", ids);
        return userService.getUsers(ids);
    }

    @Override
    @GetMapping("/check-exists/{id}")
    public boolean existsById(@PathVariable @NotNull Long id) {
        return userService.existsById(id);
    }
}