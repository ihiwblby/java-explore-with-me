package ru.practicum.server.mapper;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.server.model.Statistics;

public class StatsMapper {
    public static EndpointHitDto toEndpointHitDto(Statistics statistics) {
        return EndpointHitDto.builder()
                .app(statistics.getApp())
                .uri(statistics.getUri())
                .timestamp(statistics.getTimestamp())
                .build();
    }

    public static Statistics toStatistic(EndpointHitDto endpointHitDto) {
        return Statistics.builder()
                .id(null)
                .app(endpointHitDto.getApp())
                .uri(endpointHitDto.getUri())
                .ip(endpointHitDto.getIp())
                .timestamp(endpointHitDto.getTimestamp())
                .build();
    }
}