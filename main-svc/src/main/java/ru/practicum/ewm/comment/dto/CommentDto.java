package ru.practicum.ewm.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    @NotNull(message = "ID cannot be null")
    Long id;

    @NotNull(message = "event ID cannot be null")
    Long eventId;

    @NotBlank(message = "name cannot be blank")
    @Size(min = 2, max = 250)
    String authorName;

    @NotBlank(message = "text cannot be blank")
    @Length(min = 1, max = 1000)
    String text;

    @NotNull(message = "created date cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    LocalDateTime created;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    LocalDateTime updated;

    @NotNull(message = "hidden field cannot be null")
    Boolean isHidden;
}