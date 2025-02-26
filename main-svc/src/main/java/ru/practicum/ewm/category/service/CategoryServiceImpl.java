package ru.practicum.ewm.category.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {
    CategoryRepository categoryRepository;
    EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAll(int from, int size) {
        Pageable pageable = PageRequest.of(from, size, Sort.by("id").ascending());

        Page<Category> categoryPage;
        categoryPage = categoryRepository.findAll(pageable);

        return categoryPage.getContent().stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto get(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException("Категория с ID " + catId + " не найдена"));
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        if (categoryRepository.findByName(newCategoryDto.getName()).isPresent()) {
            throw new ConflictException("Категория " + newCategoryDto.getName() + " уже существует");
        }
        Category categoryToCreate = CategoryMapper.toCategory(newCategoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(categoryToCreate));
    }

    @Override
    public void delete(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException("Категория с ID " + catId + " не найдена"));
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Категория с ID " + catId + " не может быть удалена, так как с ней связаны события");
        }

        categoryRepository.delete(category);
    }

    @Override
    public CategoryDto update(Long catId, NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException("Категория с ID " + catId + " не найдена"));

        String newCategoryName = newCategoryDto.getName().trim();

        if (!category.getName().equals(newCategoryName) && categoryRepository.existsByName(newCategoryName)) {
            throw new ConflictException("Категория " + newCategoryName + " уже существует");
        }

        category.setName(newCategoryName);
        category = categoryRepository.save(category);

        return CategoryMapper.toCategoryDto(category);
    }
}