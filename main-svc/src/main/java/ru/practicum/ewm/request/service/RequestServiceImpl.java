package ru.practicum.ewm.request.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.model.EventStatus;
import ru.practicum.ewm.exception.AccessDeniedException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestServiceImpl implements RequestService {
    RequestRepository requestRepository;
    UserRepository userRepository;
    EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAll(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Пользователя с id " + userId + " не существует");
        }
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        return requests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователя с id " + userId + " не существует"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id " + eventId + " не найдено"));

        verifyRequest(event, userId, eventId);

        Request request = new Request();
        request.setStatus((event.getRequestModeration().equals(Boolean.FALSE)
                || event.getParticipantLimit() == null
                || event.getParticipantLimit() == 0)
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING);

        request.setEvent(event);
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());

        Request savedRequest = requestRepository.save(request);
        return RequestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователя с id " + userId + " не существует"));

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка с id " + requestId + " не найдена"));

        RequestStatus requestStatus = request.getStatus();

        if (requestStatus.equals(RequestStatus.CANCELED) || requestStatus.equals(RequestStatus.REJECTED)) {
            throw new ConflictException("Заявка с id " + requestId + " уже отменена");
        }

        boolean isUserInitiator = request.getRequester().getId().equals(userId);

        if (!isUserInitiator && requestStatus.equals(RequestStatus.PENDING)) {
            throw new AccessDeniedException("Инициатор события может отменить событие только на этапе ожидания публикации");
        }

        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toParticipationRequestDto(request);
    }

    private void verifyRequest(Event event, Long userId, Long eventId) {
        Optional<Request> request = requestRepository.findByRequesterIdAndEventId(userId, eventId);

        if (request.isPresent()) {
            throw new ConflictException("Заявка уже отправлена");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new AccessDeniedException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        if (!event.getState().equals(EventStatus.PUBLISHED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        if (event.getParticipantLimit() > 0
                && event.getParticipantLimit() <= requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)) {
            throw new ConflictException("Превышен лимит участников события");
        }
    }
}