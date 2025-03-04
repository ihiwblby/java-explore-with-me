package ru.practicum.ewm.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventAdminParams;
import ru.practicum.ewm.event.model.EventAdminState;
import ru.practicum.ewm.event.model.EventPublicParams;
import ru.practicum.ewm.event.model.EventStatus;
import ru.practicum.ewm.event.model.EventUserState;
import ru.practicum.ewm.exception.AccessDeniedException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.location.mapper.LocationMapper;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.repository.LocationRepository;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventServiceImpl implements EventService {
    @Value("${server.application.name:ewm-service}")
    String applicationName;

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    final EventRepository eventRepository;
    final UserRepository userRepository;
    final RequestRepository requestRepository;
    final CategoryRepository categoryRepository;
    final LocationRepository locationRepository;
    final StatsClient statsClient;
    final ObjectMapper objectMapper;
    final CommentRepository commentRepository;

    @Override
    public List<EventShortDto> publicGetAll(EventPublicParams eventParams, HttpServletRequest request) {
        if (eventParams.getRangeStart() != null && eventParams.getRangeEnd() != null && eventParams.getRangeEnd().isBefore(eventParams.getRangeStart())) {
            throw new ValidationException("Дата окончания не может быть раньше даты начала");
        }

        saveStatHit(request);
        Pageable pageable = getPageable(eventParams);
        Specification<Event> specification = buildSpecification(eventParams);

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();
        List<EventShortDto> result = events.stream()
                .map(EventMapper::toEventShortDto)
                .toList();

        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(events
                .stream()
                .map(Event::getId)
                .toList(), RequestStatus.CONFIRMED);

        Map<Long, List<Request>> confirmedRequestsCountMap = requests.stream()
                .collect(Collectors.groupingBy(r -> r.getEvent().getId()));

        Map<Long, Long> viewStatsMap = getEventViews(events);

        for (EventShortDto event : result) {
            List<Request> eventRequests = confirmedRequestsCountMap.getOrDefault(event.getId(), List.of());
            event.setConfirmedRequests(eventRequests.size());

            Long viewsFromMap = viewStatsMap.getOrDefault(event.getId(), 0L);
            event.setViews(viewsFromMap);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto publicGetById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id " + eventId + " не найдено"));

        if (!EventStatus.PUBLISHED.equals(event.getState())) {
            throw new EntityNotFoundException("Событие не опубликовано");
        }

        saveStatHit(request);

        long views = getEventViews(List.of(event)).getOrDefault(event.getId(), 1L);
        Map<Long, List<Request>> confirmedRequests = getConfirmedRequestsCount(List.of(event));
        Page<Comment> comments = commentRepository.findVisibleCommentsByEvent(event.getId(),
                PageRequest.of(0, 10));
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setViews(views);
        eventFullDto.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), List.of()).size());
        eventFullDto.setComments(commentDtos);

        return eventFullDto;
    }

    @Override
    public List<EventFullDto> adminGetAll(EventAdminParams eventAdminParams) {
        Pageable pageable = PageRequest.of(eventAdminParams.getFrom(),
                eventAdminParams.getSize(), Sort.by("id").ascending());

        Specification<Event> specification = Specification.where(null);
        specification = buildSpecification(eventAdminParams, specification);

        Page<Event> events = eventRepository.findAll(specification, pageable);

        List<EventFullDto> result = events.getContent().stream()
                .map(EventMapper::toEventFullDto)
                .toList();

        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(events
                .stream().map(Event::getId).toList(), RequestStatus.CONFIRMED);

        Map<Long, List<Request>> confirmedRequestsCountMap = requests.stream()
                .collect(Collectors.groupingBy(r -> r.getEvent().getId()));

        for (EventFullDto event : result) {
            List<Request> eventRequests = confirmedRequestsCountMap.getOrDefault(event.getId(), List.of());
            event.setConfirmedRequests(eventRequests.size());
        }

        return result;
    }

    @Override
    public EventFullDto adminUpdateById(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id " + eventId + " не найдено"));

        if (updateEventAdminRequest.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(updateEventAdminRequest.getEventDate(), formatter);
            if (eventDate.isBefore(LocalDateTime.now().plusMinutes(59))) {
                throw new ValidationException("Дата и время на которые намечено событие не может быть раньше, чем через час от текущего момента");
            }
        }

        if (!event.getState().equals(EventStatus.PENDING)) {
            throw new AccessDeniedException("Событие не в статусе PENDING редактировать нельзя");
        }

        boolean isUpdated = checkUpdate(event, updateEventAdminRequest);
        if (isUpdated) {
            Event updatedEvent = eventRepository.save(event);
            return EventMapper.toEventFullDto(updatedEvent);
        }
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> privateGetAllByUserId(Long userId, int from, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));

        Pageable pageable = PageRequest.of(from, size, Sort.by("id").ascending());
        Page<Event> eventsPage = eventRepository.findByInitiatorId(userId, pageable);

        return eventsPage.stream()
                .map(EventMapper::toEventShortDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto privateGetByUserIdAndEventId(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id " + eventId + " не найдено"));

        if (!event.getInitiator().equals(user)) {
            throw new AccessDeniedException("Пользователь не является инициатором события");
        }

        Page<Comment> comments = commentRepository.findVisibleCommentsByEvent(event.getId(),
                PageRequest.of(0, 10));
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setComments(commentDtos);

        return eventFullDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> privateGetAllParticipationRequests(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id " + eventId + " не найдено"));

        if (!event.getInitiator().equals(user)) {
            throw new AccessDeniedException("Пользователь не является инициатором события");
        }

        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new EntityNotFoundException("Категория с id " + newEventDto.getCategory() + " не найдена"));

        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(), formatter);
        if (eventDate.isBefore(LocalDateTime.now().plusMinutes(119))) {
            throw new ValidationException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
        }

        Event newEvent = EventMapper.toEvent(newEventDto);
        newEvent.setCreatedOn(LocalDateTime.now());
        newEvent.setCategory(category);
        newEvent.setState(EventStatus.PENDING);
        newEvent.setInitiator(user);

        Location location = locationRepository.save(LocationMapper.toLocation(newEventDto.getLocation()));
        newEvent.setLocation(location);

        Event createdEvent = eventRepository.save(newEvent);

        EventFullDto eventFullDto = EventMapper.toEventFullDto(createdEvent);
        eventFullDto.setConfirmedRequests(0);
        eventFullDto.setViews(0L);

        return eventFullDto;
    }

    @Override
    public EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id " + eventId + " не найдено"));

        if (!event.getInitiator().equals(user)) {
            throw new AccessDeniedException("Редактировать событие может только инициатор события");
        }

        if (event.getState().equals(EventStatus.PUBLISHED)) {
            throw new ConflictException("Нельзя поменять уже опубликованное событие");
        }

        if (updateEventUserRequest.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(updateEventUserRequest.getEventDate(), formatter);
            if (eventDate.isBefore(LocalDateTime.now().plusMinutes(119))) {
                throw new ValidationException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
            }
        }

        boolean isUpdated = checkUpdate(event, updateEventUserRequest);

        if (isUpdated) {
            Event updatedEvent = eventRepository.save(event);
            return EventMapper.toEventFullDto(updatedEvent);
        }
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public Map<String, List<ParticipationRequestDto>> approveRequests(Long userId, Long eventId,
                                                                      EventRequestStatusUpdateRequest statusUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id " + eventId + " не найдено"));

        if (!event.getInitiator().equals(user)) {
            throw new AccessDeniedException("Пользователь не является инициатором этого события");
        }

        Set<Long> requestIds = statusUpdateRequest.getRequestIds();
        List<Request> requests = requestRepository.findAllByIdIn(requestIds);

        if (event.getRequestModeration().equals(Boolean.TRUE)
                && event.getParticipantLimit() > 0
                && (Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())
                && statusUpdateRequest.getStatus().equals(RequestStatus.CONFIRMED))) {
            throw new ConflictException("Лимит заявок на участие в событии исчерпан");
        }

        boolean doRequestsBelongToEvent = requests.stream().allMatch(request -> request.getEvent().getId().equals(eventId));
        if (!doRequestsBelongToEvent) {
            throw new ConflictException("Запросы не относятся к данному событию");
        }

        Map<String, List<ParticipationRequestDto>> requestMap = new HashMap<>();

        if (statusUpdateRequest.getStatus().equals(RequestStatus.REJECTED)) {
            if (requests.stream().anyMatch(request -> request.getStatus().equals(RequestStatus.CONFIRMED))) {
                throw new ConflictException("Нельзя отменить подтвержденные заявки");
            }

            requests.forEach(request -> request.setStatus(RequestStatus.REJECTED));
            requestRepository.saveAll(requests);

            List<ParticipationRequestDto> rejectedRequests = requests.stream()
                    .map(RequestMapper::toParticipationRequestDto)
                    .toList();
            requestMap.put("rejectedRequests", rejectedRequests);

        } else {
            if (requests.stream().anyMatch(request -> !request.getStatus().equals(RequestStatus.PENDING))) {
                throw new ConflictException("Можно изменять статус только у заявок в ожидании");
            }

            int confirmedRequests = event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0;
            int availableSlots = event.getParticipantLimit() - confirmedRequests;

            List<Request> confirmedList = new ArrayList<>();
            List<Request> rejectedList = new ArrayList<>();

            for (int i = 0; i < requests.size(); i++) {
                if (i < availableSlots) {
                    requests.get(i).setStatus(RequestStatus.CONFIRMED);
                    confirmedList.add(requests.get(i));
                } else {
                    requests.get(i).setStatus(RequestStatus.REJECTED);
                    rejectedList.add(requests.get(i));
                }
            }

            requestRepository.saveAll(requests);

            List<ParticipationRequestDto> confirmedRequestsDto = confirmedList.stream()
                    .map(RequestMapper::toParticipationRequestDto)
                    .toList();
            requestMap.put("confirmedRequests", confirmedRequestsDto);

            List<ParticipationRequestDto> rejectedRequestsDto = rejectedList.stream()
                    .map(RequestMapper::toParticipationRequestDto)
                    .toList();
            requestMap.put("rejectedRequests", rejectedRequestsDto);

            event.setConfirmedRequests(confirmedList.size() + confirmedRequests);
            eventRepository.save(event);
        }
        return requestMap;
    }

    private boolean checkUpdate(Event oldEvent, UpdateEventRequest newEvent) {
        boolean isUpdated = false;

        if (newEvent.getAnnotation() != null && !newEvent.getAnnotation().equals(oldEvent.getAnnotation())) {
            oldEvent.setAnnotation(newEvent.getAnnotation().trim());
            isUpdated = true;
        }

        if (newEvent.getCategory() != null) {
            Category newCategory = categoryRepository.findById(newEvent.getCategory())
                    .orElseThrow(() -> new EntityNotFoundException("Категория не найдена"));
            if (!oldEvent.getCategory().equals(newCategory)) {
                oldEvent.setCategory(newCategory);
                isUpdated = true;
            }
        }

        if (newEvent.getDescription() != null && !newEvent.getDescription().equals(oldEvent.getDescription())) {
            oldEvent.setDescription(newEvent.getDescription().trim());
            isUpdated = true;
        }

        if (newEvent.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(newEvent.getEventDate(), formatter);
            if (!eventDate.equals(oldEvent.getEventDate())) {
                oldEvent.setEventDate(eventDate);
                isUpdated = true;
            }
        }

        if (newEvent.getLocation() != null) {
            Location newLocation = LocationMapper.toLocation(newEvent.getLocation());
            if (!oldEvent.getLocation().equals(newLocation)) {
                oldEvent.setLocation(newLocation);
                isUpdated = true;
            }
        }

        if (newEvent.getPaid() != null && !newEvent.getPaid().equals(oldEvent.getPaid())) {
            oldEvent.setPaid(newEvent.getPaid());
            isUpdated = true;
        }

        if (newEvent.getParticipantLimit() != null && !newEvent.getParticipantLimit().equals(oldEvent.getParticipantLimit())) {
            oldEvent.setParticipantLimit(newEvent.getParticipantLimit());
            isUpdated = true;
        }

        if (newEvent.getRequestModeration() != null && !newEvent.getRequestModeration().equals(oldEvent.getRequestModeration())) {
            oldEvent.setRequestModeration(newEvent.getRequestModeration());
            isUpdated = true;
        }

        if (newEvent instanceof UpdateEventUserRequest) {
            EventUserState newStateAction = ((UpdateEventUserRequest) newEvent).getStateAction();
            switch (newStateAction) {
                case null:
                    break;
                case SEND_TO_REVIEW:
                    oldEvent.setState(EventStatus.PENDING);
                    isUpdated = true;
                    break;
                case CANCEL_REVIEW:
                    oldEvent.setState(EventStatus.CANCELED);
                    isUpdated = true;
                    break;
            }
        } else if (newEvent instanceof UpdateEventAdminRequest) {
            EventAdminState newStateAction = ((UpdateEventAdminRequest) newEvent).getStateAction();
            switch (newStateAction) {
                case null:
                    break;
                case PUBLISH_EVENT:
                    oldEvent.setState(EventStatus.PUBLISHED);
                    isUpdated = true;
                    break;
                case REJECT_EVENT:
                    oldEvent.setState(EventStatus.CANCELED);
                    isUpdated = true;
                    break;
            }
        }

        if (newEvent.getTitle() != null && !newEvent.getTitle().equals(oldEvent.getTitle())) {
            oldEvent.setTitle(newEvent.getTitle().trim());
            isUpdated = true;
        }

        return isUpdated;
    }

    private Specification<Event> buildSpecification(EventAdminParams eventAdminParams, Specification<Event> specification) {
        if (eventAdminParams.getUsers() != null && !eventAdminParams.getUsers().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("initiator").get("id").in(eventAdminParams.getUsers()));
        }

        if (eventAdminParams.getStates() != null && !eventAdminParams.getStates().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("state").as(String.class).in(eventAdminParams.getStates()));
        }

        if (eventAdminParams.getCategories() != null && !eventAdminParams.getCategories().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(eventAdminParams.getCategories()));
        }

        if (eventAdminParams.getRangeStart() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), eventAdminParams.getRangeStart()));
        }

        if (eventAdminParams.getRangeEnd() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), eventAdminParams.getRangeEnd()));
        }

        return specification;
    }

    private Map<Long, Long> getEventViews(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        LocalDateTime startDate = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new IllegalStateException("Не удалось определить дату самого раннего события"));

        ResponseEntity<Object> response = statsClient.get(
                startDate.toString(),
                LocalDateTime.now().toString(),
                uris,
                true
        );

        Object responseBody = response.getBody();

        if (!(responseBody instanceof List<?> rawList)) {
            System.err.println("Unexpected response format: " + responseBody);
            return Map.of();
        }

        List<ViewStatsDto> viewStatsDto = rawList.stream()
                .filter(Map.class::isInstance)
                .map(map -> objectMapper.convertValue(map, ViewStatsDto.class))
                .filter(stats -> stats.getUri().startsWith("/events/"))
                .toList();

        return viewStatsDto.stream()
                .collect(Collectors.toMap(
                        stats -> Long.parseLong(stats.getUri().substring("/events/".length())),
                        ViewStatsDto::getHits
                ));
    }

    private Map<Long, List<Request>> getConfirmedRequestsCount(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED);

        return requests.stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId()));
    }

    private void saveStatHit(HttpServletRequest request) {
        statsClient.create(EndpointHitDto.builder()
                .app(applicationName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }

    private Specification<Event> buildSpecification(EventPublicParams eventParams) {
        Specification<Event> specification = Specification.where((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("state"), EventStatus.PUBLISHED));

        if (eventParams.getText() != null) {
            String searchText = "%" + eventParams.getText().toLowerCase() + "%";
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), searchText),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchText)
                    ));
        }

        if (eventParams.getCategories() != null && !eventParams.getCategories().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(eventParams.getCategories()));
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = Optional.ofNullable(eventParams.getRangeStart()).orElse(now);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventDate"), startDateTime));

        if (eventParams.getRangeEnd() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("eventDate"), eventParams.getRangeEnd()));
        }

        if (Boolean.TRUE.equals(eventParams.getOnlyAvailable())) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThan(root.get("participantLimit"), 0));
        }

        return specification;
    }

    private Pageable getPageable(EventPublicParams eventParams) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        if (eventParams.getSort().equals("EVENT_DATE")) {
            sort = Sort.by(Sort.Direction.ASC, "eventDate");
        } else if (eventParams.getSort().equals("VIEWS")) {
            sort = Sort.by(Sort.Direction.DESC, "views");
        }

        return PageRequest.of(eventParams.getFrom(), eventParams.getSize(), sort);
    }
}