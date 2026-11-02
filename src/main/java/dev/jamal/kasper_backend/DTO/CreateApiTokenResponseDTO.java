package dev.jamal.kasper_backend.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateApiTokenResponseDTO(
        UUID id,
        UUID userId,
        String name,
        String token,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
    public CreateApiTokenResponseDTO(
            UUID id,
            String name,
            String token,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        this(id, null, name, token, createdAt, expiresAt);
    }
}
