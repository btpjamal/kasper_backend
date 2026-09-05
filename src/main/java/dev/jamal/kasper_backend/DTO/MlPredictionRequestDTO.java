package dev.jamal.kasper_backend.DTO;

import java.util.Map;

public record MlPredictionRequestDTO(

        Map<String, Object> features
) {
}
