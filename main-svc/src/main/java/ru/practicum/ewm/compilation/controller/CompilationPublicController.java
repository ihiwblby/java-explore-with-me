package ru.practicum.ewm.compilation.controller;

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
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.compilation.dto.CompilationDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/compilations")
@Validated
public class CompilationPublicController {
    CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getAll(@RequestParam (required = false) Boolean pinned,
                                       @RequestParam (required = false, defaultValue = "0") @PositiveOrZero int from,
                                       @RequestParam (required = false, defaultValue = "10") @Positive int size) {
        return compilationService.getAll(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getById(@PathVariable @Positive Long compId) {
        return compilationService.getById(compId);
    }
}
