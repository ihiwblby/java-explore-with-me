package ru.practicum.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EndpointHitDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class StatsController {
    StatsClient statsClient;
    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @PostMapping(value = "/hit")
    public ResponseEntity<Object> create(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        return statsClient.create(endpointHitDto);
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> get(
            @RequestParam @NotBlank @DateTimeFormat(pattern = DATE_FORMAT) String start,
            @RequestParam @NotBlank @DateTimeFormat(pattern = DATE_FORMAT) String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") @NotNull Boolean unique) {
        return statsClient.get(start, end, uris, unique);
    }
}