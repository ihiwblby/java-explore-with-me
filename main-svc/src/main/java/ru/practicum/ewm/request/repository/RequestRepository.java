package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.request.model.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository  extends JpaRepository<Request, Long> {
    List<Request> findAllByRequesterId(Long requesterId);

    Optional<Request> findByRequesterIdAndEventId(Long requesterId, Long eventId);
}
