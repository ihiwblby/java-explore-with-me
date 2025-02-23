package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.EventAdminParams;
import ru.practicum.ewm.event.model.EventPublicParams;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface EventService {
    List<EventShortDto> publicGetAll(EventPublicParams eventParams, HttpServletRequest request);

    EventFullDto publicGetById(Long id, HttpServletRequest request);

    List<EventFullDto> adminGetAll(EventAdminParams eventAdminParams);

    EventFullDto adminUpdateById(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> privateGetAllByUserId(Long userId, int from, int size);

    EventFullDto privateGetByUserIdAndEventId(Long userId, Long eventId);

    List<ParticipationRequestDto> privateGetAllParticipationRequests(Long userId, Long eventId);

    EventFullDto create(Long userId, NewEventDto newEventDto);

    EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    Map<String, List<ParticipationRequestDto>> approveRequests(Long userId, Long eventId,
                                                               EventRequestStatusUpdateRequest statusUpdateRequest);
}