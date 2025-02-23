package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class EventShortDto {
    @NotBlank(message = "annotation cannot be blank")
    @Length(max = 2000)
    String annotation;

    @NotNull(message = "Category cannot be null")
    CategoryDto category;

    Integer confirmedRequests;

    @NotNull(message = "Event date cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    LocalDateTime eventDate;

    Long id;

    @NotNull(message = "Initiator cannot be null")
    UserShortDto initiator;

    @NotNull(message = "Paid field cannot be null")
    Boolean paid;

    @NotBlank(message = "title cannot be blank")
    @Length(max = 120)
    String title;

    Long views;
}