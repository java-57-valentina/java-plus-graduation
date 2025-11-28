package ru.practicum.users.service;

import jakarta.validation.constraints.NotNull;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.dto.user.UserDtoOut;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

    UserDtoOut createUser(NewUserRequest request);

    UserDtoOut getUser(@NotNull Long id);

    boolean existsById(Long id);

    List<UserDtoOut> getUsers(List<Long> ids, int from, int size);

    void deleteUser(Long userId);

    Map<Long, UserDtoOut> getUsers(@NotNull Set<Long> ids);
}