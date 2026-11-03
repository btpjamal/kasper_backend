package dev.jamal.kasper_backend.Client;

import dev.jamal.kasper_backend.DTO.MlBatchPredictionResponseDTO;
import dev.jamal.kasper_backend.DTO.MlPredictionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MlClient {

    private final RestClient mlRestClient;

    /**
     * Realiza a predição individual enviando os atributos do estudante para /predict
     */
    public MlPredictionResponseDTO predict(Map<String, Object> features) {
        return mlRestClient
                .post()
                .uri("/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(features)
                .retrieve()
                .body(MlPredictionResponseDTO.class);
    }

    /**
     * Realiza a predição em lote enviando a lista de estudantes para /predict/batch
     */
    public MlBatchPredictionResponseDTO predictBatch(List<Map<String, Object>> students) {
        Map<String, Object> requestBody = Map.of("students", students);

        return mlRestClient
                .post()
                .uri("/predict/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(MlBatchPredictionResponseDTO.class);
    }
}
