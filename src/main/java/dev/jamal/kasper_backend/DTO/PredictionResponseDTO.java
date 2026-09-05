package dev.jamal.kasper_backend.DTO;

public record PredictionResponseDTO(

        String studentId,
        double dropoutProbability,
        boolean dropoutPrediction,
        String riskLevel,
        String modelVersion
) {
}

//        {
//        "studentId": "ALUNO-123",
//        "dropoutProbability": 0.8234,
//        "dropoutPrediction": true,
//        "riskLevel": "HIGH",
//        "modelVersion": "1.0.0"
//        }