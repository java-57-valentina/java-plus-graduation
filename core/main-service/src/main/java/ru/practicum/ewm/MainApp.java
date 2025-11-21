package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.clients.LocationApi;
import ru.practicum.clients.RequestApi;
import ru.practicum.clients.UserApi;
import ru.practicum.statsclient.StatsOperations;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(clients = { StatsOperations.class, RequestApi.class, UserApi.class, LocationApi.class })
public class MainApp {
    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }
}