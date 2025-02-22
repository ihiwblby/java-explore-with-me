package ru.practicum.ewm.compilation;

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
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.event.Event;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.event.EventShortDto;
import ru.practicum.ewm.exception.ConflictException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
        compilationToCreate.setPinned(Boolean.TRUE.equals(newCompilationDto.getPinned()));

        Set<EventShortDto> eventDtos = Optional.ofNullable(newCompilationDto.getEvents()).orElse(Collections.emptySet());
        Set<Long> eventIds = eventDtos.stream()
                .map(EventShortDto::getId)
                .collect(Collectors.toSet());

        Set<Event> events = eventIds.isEmpty() ? Collections.emptySet() : new HashSet<>(eventRepository.findAllById(eventIds));

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
            Set<Long> eventIds = updateComp.getEvents().stream()
                    .map(EventShortDto::getId)
                    .collect(Collectors.toSet());

            Set<Event> events = new HashSet<>(eventRepository.findAllById(eventIds));
            compilation.setEvents(events);
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(updatedCompilation);
    }
}