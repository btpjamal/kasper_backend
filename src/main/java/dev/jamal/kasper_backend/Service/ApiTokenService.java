package dev.jamal.kasper_backend.Service;

import dev.jamal.kasper_backend.DTO.ApiTokenResponseDTO;
import dev.jamal.kasper_backend.DTO.CreateApiTokenRequestDTO;
import dev.jamal.kasper_backend.DTO.CreateApiTokenResponseDTO;
import dev.jamal.kasper_backend.Entity.ApiToken;
import dev.jamal.kasper_backend.Entity.User;
import dev.jamal.kasper_backend.Repository.ApiTokenRepository;
import dev.jamal.kasper_backend.Repository.UserRepository;
import dev.jamal.kasper_backend.Security.ApiTokenPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiTokenService {

    private static final String TOKEN_PREFIX = "edupredict_sk_";

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final ApiTokenRepository apiTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateApiTokenResponseDTO create(
            String userEmail,
            CreateApiTokenRequestDTO request
    ) {

        User user = getUser(userEmail);

        String rawToken = generateToken();

        String tokenHash = hashToken(rawToken);

        String displayPrefix = rawToken.substring(
                0,
                Math.min(rawToken.length(), 24)
        ) + "...";

        LocalDateTime now = LocalDateTime.now();

        ApiToken apiToken = ApiToken.builder()
                .name(request.name().trim())
                .tokenHash(tokenHash)
                .tokenPrefix(displayPrefix)
                .active(true)
                .createdAt(now)
                .expiresAt(now.plusDays(90))
                .user(user)
                .build();

        ApiToken savedToken =
                apiTokenRepository.save(apiToken);

        return new CreateApiTokenResponseDTO(
                savedToken.getId(),
                savedToken.getName(),
                rawToken,
                savedToken.getCreatedAt(),
                savedToken.getExpiresAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ApiTokenResponseDTO> list(
            String userEmail
    ) {

        User user = getUser(userEmail);

        return apiTokenRepository
                .findAllByUserIdOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ApiTokenResponseDTO revoke(
            String userEmail,
            UUID tokenId
    ) {

        User user = getUser(userEmail);

        ApiToken token = apiTokenRepository
                .findByIdAndUserId(
                        tokenId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Token não encontrado"
                        )
                );

        token.setActive(false);

        return toResponse(token);
    }

    @Transactional
    public void delete(
            String userEmail,
            UUID tokenId
    ) {

        User user = getUser(userEmail);

        ApiToken token = apiTokenRepository
                .findByIdAndUserId(
                        tokenId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Token não encontrado"
                        )
                );

        apiTokenRepository.delete(token);
    }

    private User getUser(String email) {

        return userRepository
                .findByEmail(email.toLowerCase())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado"
                        )
                );
    }

    private String generateToken() {

        byte[] randomBytes = new byte[32];

        SECURE_RANDOM.nextBytes(randomBytes);

        String randomPart =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(randomBytes);

        return TOKEN_PREFIX + randomPart;
    }

    public String hashToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 não disponível",
                    e
            );
        }
    }

    private ApiTokenResponseDTO toResponse(
            ApiToken token
    ) {

        return new ApiTokenResponseDTO(
                token.getId(),
                token.getName(),
                token.getTokenPrefix(),
                token.isActive(),
                token.getCreatedAt(),
                token.getLastUsedAt(),
                token.getExpiresAt()
        );
    }

    @Transactional
    public ApiTokenPrincipal authenticate(String rawToken) {

        if (rawToken == null ||
                !rawToken.startsWith(TOKEN_PREFIX)) {

            throw new BadCredentialsException(
                    "API Token inválido"
            );
        }

        String tokenHash = hashToken(rawToken);

        ApiToken apiToken = apiTokenRepository
                .findByTokenHashAndActiveTrue(tokenHash)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "API Token inválido"
                        )
                );

        LocalDateTime now = LocalDateTime.now();

        if (apiToken.getExpiresAt() != null &&
                apiToken.getExpiresAt().isBefore(now)) {

            throw new BadCredentialsException(
                    "API Token expirado"
            );
        }

        apiToken.setLastUsedAt(now);

        User user = apiToken.getUser();

        return new ApiTokenPrincipal(
                apiToken.getId(),
                user.getId(),
                user.getEmail()
        );
    }
}
