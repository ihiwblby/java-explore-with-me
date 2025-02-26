package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRequestDto {
    @NotBlank(message = "name cannot be blank")
    @Size(min = 2, max = 250)
    String name;

    @NotBlank(message = "email cannot be blank")
    @Email
    @Size(min = 6, max = 254)
    String email;
}