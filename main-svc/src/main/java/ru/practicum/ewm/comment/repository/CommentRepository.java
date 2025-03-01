package ru.practicum.ewm.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.comment.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c " +
            "FROM Comment c " +
            "WHERE c.author.id = :userId AND c.isHidden = false " +
            "ORDER BY c.created DESC")
    List<Comment> findVisibleCommentsByUser(@Param("userId") Long userId);

    @Query("SELECT c " +
            "FROM Comment c " +
            "WHERE c.author.id = :userId AND c.event.id = :eventId AND c.isHidden = false " +
            "ORDER BY c.created DESC")
    List<Comment> findVisibleCommentsByUserAndEvent(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query("SELECT c " +
            "FROM Comment c " +
            "WHERE c.event.id = :eventId " +
            "ORDER BY c.created DESC")
    Page<Comment> findAllCommentsByEvent(@Param("eventId") Long eventId, Pageable pageable);

    @Query("SELECT c " +
            "FROM Comment c " +
            "WHERE c.event.id = :eventId AND c.isHidden = false " +
            "ORDER BY c.created DESC")
    Page<Comment> findVisibleCommentsByEvent(@Param("eventId") Long eventId, Pageable pageable);
}