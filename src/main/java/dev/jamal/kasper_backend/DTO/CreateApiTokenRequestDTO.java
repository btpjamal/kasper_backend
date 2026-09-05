package dev.jamal.kasper_backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateApiTokenRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100)
        String name
) {
}
