package dev.jamal.kasper_backend.DTO;

public record MlPredictionResponseDTO(

        double dropoutProbability,
        boolean dropoutPrediction,
        String modelVersion
) {
}

//        {
//        "dropoutProbability": 0.8234,
//        "dropoutPrediction": true,
//        "modelVersion": "1.0.0"
//        }