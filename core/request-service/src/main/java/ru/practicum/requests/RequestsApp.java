package ru.practicum.requests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.clients.EventApi;
import ru.practicum.clients.UserApi;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(clients = { UserApi.class, EventApi.class })
public class RequestsApp {
    public static void main(String[] args) {
        SpringApplication.run(RequestsApp.class, args);
    }
}
