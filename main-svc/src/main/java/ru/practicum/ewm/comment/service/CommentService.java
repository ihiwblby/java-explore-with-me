package ru.practicum.ewm.comment.service;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {
    List<CommentDto> publicGetAllByEvent(Long eventId, int from, int size);

    List<CommentDto> adminGetAllByEvent(Long eventId, int from, int size);

    List<CommentDto> getAllByUser(Long userId);

    List<CommentDto> getAllByUserAndEvent(Long userId, Long eventId);

    CommentDto create(Long userId, Long eventId, NewCommentDto newCommentDto);

    void privateDelete(Long userId, Long commentId);

    CommentDto update(Long userId, Long commentId, NewCommentDto newCommentDto);

    void adminDelete(Long commentId);

    CommentDto changeVisibility(Long commentId, Boolean isHidden);

    CommentDto get(Long commentId);
}