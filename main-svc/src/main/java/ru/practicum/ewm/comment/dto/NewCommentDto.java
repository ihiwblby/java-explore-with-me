package ru.practicum.ewm.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class NewCommentDto {
    @NotBlank(message = "name cannot be blank")
    @Size(min = 2, max = 250)
    String authorName;

    @NotBlank(message = "text cannot be blank")
    @Length(min = 1, max = 1000)
    String text;
}