package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.event.EventShortDto;

import java.util.Set;

@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    Set<EventShortDto> events;

    @NotNull(message = "id cannot be null")
    Long id;

    @NotNull(message = "pinned should be defined")
    Boolean pinned;

    @NotBlank(message = "title cannot be blank")
    String title;
}