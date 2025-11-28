package ru.practicum.location.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.practicum.dto.user.UserDtoOut;
import ru.practicum.location.model.LocationState;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LocationFullDtoOut {
    private Long id;
    private String name;
    private String address;
    @JsonProperty(value = "lat")
    private Double latitude;
    @JsonProperty(value = "lon")
    private Double longitude;
    private UserDtoOut creator;
    private LocationState state;
}
