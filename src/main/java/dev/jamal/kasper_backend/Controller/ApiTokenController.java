package dev.jamal.kasper_backend.Controller;

import dev.jamal.kasper_backend.DTO.ApiTokenResponseDTO;
import dev.jamal.kasper_backend.DTO.CreateApiTokenRequestDTO;
import dev.jamal.kasper_backend.DTO.CreateApiTokenResponseDTO;
import dev.jamal.kasper_backend.Service.ApiTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class ApiTokenController {

    private final ApiTokenService apiTokenService;

    @PostMapping
    public ResponseEntity<CreateApiTokenResponseDTO> create(
            @Valid @RequestBody CreateApiTokenRequestDTO request,
            Authentication authentication
    ) {

        CreateApiTokenResponseDTO response =
                apiTokenService.create(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApiTokenResponseDTO>> list(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                apiTokenService.list(
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/{id}/revoke")
    public ResponseEntity<ApiTokenResponseDTO> revoke(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                apiTokenService.revoke(
                        authentication.getName(),
                        id
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        apiTokenService.delete(
                authentication.getName(),
                id
        );

        return ResponseEntity.noContent().build();
    }
}
