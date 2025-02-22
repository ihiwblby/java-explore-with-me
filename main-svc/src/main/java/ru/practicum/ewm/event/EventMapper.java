package ru.practicum.ewm.event;

public class EventMapper {
    public static EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder().build();
    }
}
