package dev.jamal.kasper_backend.Repository;

import dev.jamal.kasper_backend.Entity.ApiToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiTokenRepository
        extends JpaRepository<ApiToken, UUID> {

    List<ApiToken> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<ApiToken> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    Optional<ApiToken> findByTokenHashAndActiveTrue(
            String tokenHash
    );
}
