package ru.practicum.ewm.comment.mapper;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.model.Comment;

public class CommentMapper {
    public static Comment toComment(NewCommentDto newCommentDto) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .eventId(comment.getEvent().getId())
                .authorName(comment.getAuthor().getName())
                .text(comment.getText())
                .created(comment.getCreated())
                .updated(comment.getUpdated() != null ? comment.getUpdated() : null)
                .isHidden(comment.getIsHidden())
                .build();
    }
}