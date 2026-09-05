package dev.jamal.kasper_backend.Controller;

import dev.jamal.kasper_backend.DTO.PredictionRequestDTO;
import dev.jamal.kasper_backend.DTO.PredictionResponseDTO;
import dev.jamal.kasper_backend.Security.ApiTokenPrincipal;
import dev.jamal.kasper_backend.Service.PredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping
    public ResponseEntity<PredictionResponseDTO> predict(

            @Valid
            @RequestBody
            PredictionRequestDTO request,

            @AuthenticationPrincipal
            ApiTokenPrincipal principal
    ) {

        PredictionResponseDTO response =
                predictionService.predict(request);

        return ResponseEntity.ok(response);
    }
}
