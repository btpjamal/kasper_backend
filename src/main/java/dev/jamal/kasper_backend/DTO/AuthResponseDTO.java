package dev.jamal.kasper_backend.DTO;

import java.util.UUID;

public record AuthResponseDTO(
        UUID userId,
        String name,
        String email,
        String token,
        String tokenType,
        long expiresIn
) {
}
