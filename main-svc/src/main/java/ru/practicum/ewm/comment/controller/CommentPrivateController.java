package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentUpdateDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/users/{userId}/comments")
@Validated
public class CommentPrivateController {
    CommentService commentService;

    @GetMapping
    public List<CommentDto> getAllByUser(@PathVariable @Positive Long userId) {
        return commentService.getAllByUser(userId);
    }

    @GetMapping("/{eventId}")
    public List<CommentDto> getAllByUserAndEvent(@PathVariable @Positive Long userId,
                                         @PathVariable @Positive Long eventId) {
        return commentService.getAllByUserAndEvent(userId, eventId);
    }

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable @Positive Long userId,
                             @PathVariable @Positive Long eventId,
                             @RequestBody @Valid NewCommentDto newCommentDto) {
        return commentService.create(userId, eventId, newCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long userId,
                       @PathVariable @Positive Long commentId) {
        commentService.privateDelete(userId, commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(@PathVariable @Positive Long userId,
                             @PathVariable @Positive Long commentId,
                             @RequestBody @Valid CommentUpdateDto commentUpdateDto) {
        return commentService.update(userId, commentId, commentUpdateDto);
    }
}