package ru.practicum.ewm.compilation.service;

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
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompilationServiceImpl implements CompilationService {
    CompilationRepository compilationRepository;
    EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from, size, Sort.by("id").ascending());

        Page<Compilation> compilationPage = (pinned == null)
                ? compilationRepository.findAll(pageable)
                : compilationRepository.findAllByPinned(pinned, pageable);

        return compilationPage.getContent().stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException("Подборка с ID " + compId + " не найдена"));
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        if (compilationRepository.findByTitle(newCompilationDto.getTitle()).isPresent()) {
            throw new ConflictException("Такая подборка уже существует");
        }

        Compilation compilationToCreate = CompilationMapper.toCompilation(newCompilationDto);

        Set<Long> eventIds = newCompilationDto.getEvents();
        Set<Event> events = eventIds != null && !eventIds.isEmpty()
                ? new HashSet<>(eventRepository.findAllById(eventIds))
                : Collections.emptySet();

        compilationToCreate.setEvents(events);
        Compilation newCompilation = compilationRepository.save(compilationToCreate);

        return CompilationMapper.toCompilationDto(newCompilation);
    }

    @Override
    public void delete(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException("Подборка с ID " + compId + " не найдена"));
        compilationRepository.delete(compilation);
    }

    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest updateComp) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException("Подборка с ID " + compId + " не найдена"));

        if (updateComp.getPinned() != null && !updateComp.getPinned().equals(compilation.getPinned())) {
            compilation.setPinned(updateComp.getPinned());
        }

        if (updateComp.getTitle() != null && !updateComp.getTitle().isBlank()
                && !updateComp.getTitle().equals(compilation.getTitle())) {
            compilation.setTitle(updateComp.getTitle());
        }

        if (updateComp.getEvents() != null && !updateComp.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateComp.getEvents()));
            compilation.setEvents(events);
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(updatedCompilation);
    }
}