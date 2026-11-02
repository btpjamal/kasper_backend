package dev.jamal.kasper_backend.Controller;

import dev.jamal.kasper_backend.DTO.BatchPredictionRequestDTO;
import dev.jamal.kasper_backend.DTO.BatchPredictionResponseDTO;
import dev.jamal.kasper_backend.DTO.PredictionRequestDTO;
import dev.jamal.kasper_backend.DTO.PredictionResponseDTO;
import dev.jamal.kasper_backend.Security.ApiTokenPrincipal;
import dev.jamal.kasper_backend.Service.PredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    /**
     * Endpoint para previsão individual de evasão escolar.
     * Exige autenticação via API Token.
     */
    @PostMapping
    public ResponseEntity<PredictionResponseDTO> predict(
            @Valid @RequestBody PredictionRequestDTO request,
            @AuthenticationPrincipal ApiTokenPrincipal principal
    ) {
        PredictionResponseDTO response = predictionService.predict(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para previsão em lote (múltipla) de evasão escolar.
     * Exige autenticação via API Token.
     */
    @PostMapping("/batch")
    public ResponseEntity<BatchPredictionResponseDTO> predictBatch(
            @Valid @RequestBody BatchPredictionRequestDTO request,
            @AuthenticationPrincipal ApiTokenPrincipal principal
    ) {
        BatchPredictionResponseDTO response = predictionService.predictBatch(request);
        return ResponseEntity.ok(response);
    }
}
