package dev.jamal.kasper_backend.Service;

import dev.jamal.kasper_backend.Client.MlClient;
import dev.jamal.kasper_backend.DTO.MlPredictionRequestDTO;
import dev.jamal.kasper_backend.DTO.MlPredictionResponseDTO;
import dev.jamal.kasper_backend.DTO.PredictionRequestDTO;
import dev.jamal.kasper_backend.DTO.PredictionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final MlClient mlClient;

    public PredictionResponseDTO predict(
            PredictionRequestDTO request
    ) {

        MlPredictionRequestDTO mlRequest =
                new MlPredictionRequestDTO(
                        request.features()
                );

        MlPredictionResponseDTO mlResponse =
                mlClient.predict(mlRequest);

        if (mlResponse == null) {
            throw new IllegalStateException(
                    "O modelo não retornou uma resposta"
            );
        }

        String riskLevel =
                calculateRiskLevel(
                        mlResponse.dropoutProbability()
                );

        return new PredictionResponseDTO(
                request.studentId(),
                mlResponse.dropoutProbability(),
                mlResponse.dropoutPrediction(),
                riskLevel,
                mlResponse.modelVersion()
        );
    }

    private String calculateRiskLevel(
            double probability
    ) {

        if (probability >= 0.70) {
            return "HIGH";
        }

        if (probability >= 0.40) {
            return "MEDIUM";
        }

        return "LOW";
    }
}
