package ru.practicum.ewm.comment.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentServiceImpl implements CommentService {
    CommentRepository commentRepository;
    UserRepository userRepository;
    EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public CommentDto get(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с id " + commentId + " не найден"));

        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> publicGetAllByEvent(Long eventId, int from, int size) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id " + eventId + " не найдено"));

        Pageable pageable = PageRequest.of(from, size);
        Page<Comment> comments = commentRepository.findVisibleCommentsByEvent(eventId, pageable);

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> adminGetAllByEvent(Long eventId, int from, int size) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id " + eventId + " не найдено"));

        Pageable pageable = PageRequest.of(from, size);
        Page<Comment> comments = commentRepository.findAllCommentsByEvent(eventId, pageable);

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));

        List<Comment> comments = commentRepository.findVisibleCommentsByUser(userId);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllByUserAndEvent(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id " + eventId + " не найдено"));

        List<Comment> comments = commentRepository.findVisibleCommentsByUserAndEvent(userId, eventId);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    public CommentDto create(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с id " + eventId + " не найдено"));

        Comment newComment = CommentMapper.toComment(newCommentDto);
        newComment.setAuthor(user);
        newComment.setEvent(event);
        newComment.setCreated(LocalDateTime.now());
        newComment.setIsHidden(Boolean.FALSE);
        Comment savedComment = commentRepository.save(newComment);

        return CommentMapper.toCommentDto(savedComment);
    }

    @Override
    public void privateDelete(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с id " + commentId + " не найден"));

        if (comment.getIsHidden().equals(Boolean.TRUE)) {
            throw new ConflictException("Комментарий уже удалён");
        }

        comment.setIsHidden(Boolean.TRUE);
        commentRepository.save(comment);
    }

    @Override
    public CommentDto update(Long userId, Long commentId, NewCommentDto newCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + userId + " не найден"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с id " + commentId + " не найден"));

        String textToUpdate = newCommentDto.getText().trim();
        if (comment.getText().equals(textToUpdate)) {
            throw new ConflictException("Отсутствует новый текст");
        }

        comment.setText(textToUpdate);
        comment.setUpdated(LocalDateTime.now());
        Comment updatedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(updatedComment);
    }

    @Override
    public void adminDelete(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с id " + commentId + " не найден"));
        commentRepository.delete(comment);
    }

    @Override
    public CommentDto changeVisibility(Long commentId, Boolean isHidden) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с id " + commentId + " не найден"));

        if (comment.getIsHidden().equals(isHidden)) {
            throw new ConflictException("Этот статус уже установлен");
        }

        comment.setIsHidden(isHidden);
        Comment updatedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(updatedComment);
    }
}