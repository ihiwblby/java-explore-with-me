package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.location.dto.LocationDto;

import java.time.LocalDateTime;

@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @NotBlank(message = "annotation cannot be blank")
    @Length(min = 20, max = 2000)
    String annotation;

    @NotNull(message = "Category cannot be null")
    @Positive(message = "category id should be positive")
    Long category;

    @NotBlank(message = "description cannot be blank")
    @Length(min = 20, max = 7000)
    String description;

    @NotNull(message = "Event date cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    LocalDateTime eventDate;

    @NotNull(message = "Location cannot be null")
    @Valid
    LocationDto location;

    @NotNull(message = "Paid field cannot be null")
    Boolean paid;

    @PositiveOrZero
    Integer participantLimit;

    Boolean requestModeration;

    @NotBlank(message = "title cannot be blank")
    @Length(min = 3, max = 120)
    String title;
}