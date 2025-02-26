package ru.practicum.ewm.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.comment.dto.CommentDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/comments")
@Validated
public class CommentPublicController {
    CommentService commentService;

    @GetMapping("/{eventId}")
    public List<CommentDto> publicGetAllByEvent(@PathVariable @Positive Long eventId,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.publicGetAllByEvent(eventId, from, size);
    }
}