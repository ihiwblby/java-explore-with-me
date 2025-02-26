package ru.practicum.ewm.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.comment.dto.CommentDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/comments")
@Validated
public class CommentAdminController {
    CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long commentId) {
        commentService.adminDelete(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto changeVisibility(@PathVariable @Positive Long commentId,
                                       @RequestParam Boolean isHidden) {
        return commentService.changeVisibility(commentId, isHidden);
    }

    @GetMapping("/{eventId}")
    public List<CommentDto> adminGetAll(@PathVariable @Positive Long eventId,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.adminGetAllByEvent(eventId, from, size);
    }
}