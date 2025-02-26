package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.NewUserRequestDto;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAll(List<Long> ids, int from, int size);

    UserDto create(NewUserRequestDto newUserRequestDto);

    void delete(Long id);
}