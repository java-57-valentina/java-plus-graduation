package ru.practicum.requests.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.model.ParticipationRequest;

@UtilityClass
public class ParticipationRequestMapper {
    public static ParticipationRequestDto toDto(ParticipationRequest r) {
        return ParticipationRequestDto.builder()
                .id(r.getId())
                .created(r.getCreated())
                .event(r.getEventId())
                .requester(r.getRequesterId())
                .status(r.getStatus().name())
                .build();
    }
}