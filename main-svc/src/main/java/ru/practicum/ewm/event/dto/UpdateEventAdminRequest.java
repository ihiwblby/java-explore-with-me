package ru.practicum.ewm.event.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.practicum.ewm.event.model.EventAdminState;

@Getter
@EqualsAndHashCode(callSuper = true)
public class UpdateEventAdminRequest extends UpdateEventRequest {
    EventAdminState stateAction;
}