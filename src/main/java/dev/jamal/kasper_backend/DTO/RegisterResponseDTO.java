package dev.jamal.kasper_backend.DTO;

import java.util.UUID;

public record RegisterResponseDTO(
        UUID id,
        String name,
        String email
) {
}
