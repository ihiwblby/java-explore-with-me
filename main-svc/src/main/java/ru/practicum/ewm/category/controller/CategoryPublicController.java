package ru.practicum.ewm.category.controller;

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
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.category.dto.CategoryDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/categories")
@Validated
public class CategoryPublicController {
    CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAll(@RequestParam (defaultValue = "0") @PositiveOrZero int from,
                                    @RequestParam (defaultValue = "10") @Positive int size) {
        return categoryService.getAll(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto get(@PathVariable @Positive Long catId) {
        return categoryService.get(catId);
    }
}