package ru.practicum.events.compilation.mapper;


import lombok.experimental.UtilityClass;
import ru.practicum.events.compilation.dto.CompilationDto;
import ru.practicum.events.compilation.dto.NewCompilationDto;
import ru.practicum.events.compilation.model.Compilation;
import ru.practicum.events.event.mapper.EventMapper;
import ru.practicum.events.event.model.Event;

import java.util.Set;

@UtilityClass
public class CompilationMapper {

    public Compilation toEntity(NewCompilationDto dto, Set<Event> events) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null && dto.getPinned())
                .events(events)
                .build();
    }

    public CompilationDto toDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(compilation.getEvents().stream()
                        // TODO: fix it!
                        .map((Event event) -> EventMapper.toShortDto(event, null))
                        .toList())
                .build();
    }
}