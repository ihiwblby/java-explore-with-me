package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RequestRepository  extends JpaRepository<Request, Long> {
    List<Request> findAllByRequesterId(Long requesterId);

    Optional<Request> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<Request> findAllByIdIn(Set<Long> requestIds);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByEventIdInAndStatus(List<Long> eventIds, RequestStatus status);
}