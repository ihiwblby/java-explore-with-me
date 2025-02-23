package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.EventPublicParams;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/events")
@Validated
public class EventPublicController {
    EventService eventService;
    static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @GetMapping
    public List<EventShortDto> getAll(@RequestParam(defaultValue = "") String text,
                                      @RequestParam(required = false) List<Long> categories,
                                      @RequestParam(required = false) Boolean paid,
                                      @RequestParam(required = false) String rangeStart,
                                      @RequestParam(required = false) String rangeEnd,
                                      @RequestParam(defaultValue = "false") boolean onlyAvailable,
                                      @RequestParam(required = false) String sort,
                                      @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                      @RequestParam(defaultValue = "10") @Positive int size,
                                      HttpServletRequest request) {
        System.out.println("EventPublicController get /events");

        LocalDateTime start = (rangeStart != null)
                ? LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                : LocalDateTime.now();

        LocalDateTime end = (rangeEnd != null)
                ? LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
                : LocalDateTime.now().plusYears(1);

        String normalizedText = Objects.requireNonNullElse(text, "").trim().toLowerCase();

        EventPublicParams eventParams = EventPublicParams.builder()
                .text(normalizedText)
                .categories(categories)
                .paid(paid)
                .rangeStart(start)
                .rangeEnd(end)
                .onlyAvailable(onlyAvailable)
                .from(from)
                .size(size)
                .build();
        if (sort != null) {
            eventParams.setSort(sort);
        }
        return eventService.publicGetAll(eventParams, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getById(@PathVariable @Positive Long id,
                                HttpServletRequest request) {
        System.out.println("EventPublicController get /events/{id}" + id);
        return eventService.publicGetById(id, request);
    }
}