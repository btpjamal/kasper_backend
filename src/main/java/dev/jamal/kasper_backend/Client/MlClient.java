package dev.jamal.kasper_backend.Client;

import dev.jamal.kasper_backend.DTO.MlPredictionRequestDTO;
import dev.jamal.kasper_backend.DTO.MlPredictionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class MlClient {

    private final RestClient mlRestClient;

    public MlPredictionResponseDTO predict(
            MlPredictionRequestDTO request
    ) {

        return mlRestClient
                .post()
                .uri("/predict")
                .body(request)
                .retrieve()
                .body(MlPredictionResponseDTO.class);
    }
}

//POST http://localhost:8000/predict
//Content-Type: application/json
//
//{
//    "features": {
//    "attendanceRate": 0.72,
//            "averageGrade": 5.8,
//            "failedSubjects": 3,
//            "age": 16
//}
//}