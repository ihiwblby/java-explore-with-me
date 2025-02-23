package ru.practicum.ewm.event.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.practicum.ewm.event.model.EventUserState;

@Getter
@EqualsAndHashCode(callSuper = true)
public class UpdateEventUserRequest extends UpdateEventRequest {
    EventUserState stateAction;
}