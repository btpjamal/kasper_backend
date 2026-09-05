package dev.jamal.kasper_backend.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiTokenResponseDTO(

        UUID id,
        String name,
        String tokenPrefix,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime lastUsedAt,
        LocalDateTime expiresAt
) {
}
