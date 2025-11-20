package ru.practicum.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users", path = "/api/users")
public interface UserApi {

    @GetMapping("/{id}")
    boolean existsById(@PathVariable Long id);
}
