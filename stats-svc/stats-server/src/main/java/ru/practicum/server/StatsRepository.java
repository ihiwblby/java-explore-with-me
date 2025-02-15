package ru.practicum.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Statistics, Long> {

    @Query("""
            SELECT new ru.practicum.dto.ViewStatsDto(s.app, s.uri, COUNT(DISTINCT s.ip))
            FROM Statistics AS s
            WHERE s.timestamp BETWEEN :start AND :end
            GROUP BY s.app, s.uri
            ORDER BY COUNT(DISTINCT s.ip) DESC
            """)
    List<ViewStatsDto> findStatsWithUniqueIpNoUris(LocalDateTime start,
                                                   LocalDateTime end);

    @Query("""
            SELECT new ru.practicum.dto.ViewStatsDto(s.app, s.uri, COUNT(s.ip))
            FROM Statistics AS s
            WHERE s.timestamp BETWEEN :start AND :end
            GROUP BY s.app, s.uri
            ORDER BY COUNT(s.ip) DESC
            """)
    List<ViewStatsDto> findStatsNoUniqueIpNoUris(LocalDateTime start,
                                                 LocalDateTime end);

    @Query("""
            SELECT new ru.practicum.dto.ViewStatsDto(s.app, s.uri, COUNT(DISTINCT s.ip))
            FROM Statistics AS s
            WHERE s.uri IN :uris AND s.timestamp BETWEEN :start AND :end
            GROUP BY s.app, s.uri
            ORDER BY COUNT(DISTINCT s.ip) DESC
            """)
    List<ViewStatsDto> findStatsWithUrisWithUniqueIp(LocalDateTime start,
                                                     LocalDateTime end,
                                                     List<String> uris);

    @Query("""
            SELECT new ru.practicum.dto.ViewStatsDto(s.app, s.uri, COUNT(s.ip))
            FROM Statistics AS s
            WHERE s.uri IN :uris AND s.timestamp BETWEEN :start AND :end
            GROUP BY s.app, s.uri
            ORDER BY COUNT(s.ip) DESC
            """)
    List<ViewStatsDto> findStatsWithUrisNoUniqueIp(LocalDateTime start,
                                                   LocalDateTime end,
                                                   List<String> uris);
}