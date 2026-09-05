package dev.jamal.kasper_backend.Security;

import java.util.UUID;

public record ApiTokenPrincipal(

        UUID tokenId,
        UUID userId,
        String email
) {
}
