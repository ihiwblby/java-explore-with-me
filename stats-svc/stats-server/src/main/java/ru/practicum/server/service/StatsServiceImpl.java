package ru.practicum.server.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.exception.ValidationException;
import ru.practicum.server.repository.StatsRepository;
import ru.practicum.server.mapper.StatsMapper;
import ru.practicum.server.model.Statistics;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatsServiceImpl implements StatsService {
    StatsRepository statsRepository;

    @Override
    @Transactional
    public EndpointHitDto create(EndpointHitDto endpointHitDto) {
        Statistics statistics = StatsMapper.toStatistic(endpointHitDto);
        Statistics savedStatistics = statsRepository.save(statistics);
        return StatsMapper.toEndpointHitDto(savedStatistics);
    }

    @Override
    @Transactional
    public List<ViewStatsDto> get(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("start cannot be after end");
        }

        if (uris == null || uris.isEmpty()) {
            return getStatsWithoutUris(start, end, unique);
        } else {
            return getStatsWithUris(start, end, uris, unique);
        }
    }

    private List<ViewStatsDto> getStatsWithoutUris(LocalDateTime start, LocalDateTime end, Boolean unique) {
        if (unique) {
            return statsRepository.findStatsWithUniqueIpNoUris(start, end);
        } else {
            return statsRepository.findStatsNoUniqueIpNoUris(start, end);
        }
    }

    private List<ViewStatsDto> getStatsWithUris(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (unique) {
            return statsRepository.findStatsWithUrisWithUniqueIp(start, end, uris);
        } else {
            return statsRepository.findStatsWithUrisNoUniqueIp(start, end, uris);
        }
    }
}