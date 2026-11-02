package dev.jamal.kasper_backend.Service;

import dev.jamal.kasper_backend.Client.MlClient;
import dev.jamal.kasper_backend.DTO.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final MlClient mlClient;

    /**
     * Executa predição individual consumindo o modelo de Machine Learning
     */
    public PredictionResponseDTO predict(PredictionRequestDTO request) {
        Map<String, Object> features = request.getFeatures();

        if (features == null || features.isEmpty()) {
            throw new IllegalArgumentException(
                    "As características do estudante são obrigatórias para a predição."
            );
        }

        MlPredictionResponseDTO mlResponse = mlClient.predict(features);

        if (mlResponse == null) {
            throw new IllegalStateException(
                    "O modelo de Machine Learning não retornou uma resposta válida."
            );
        }

        return new PredictionResponseDTO(
                request.getStudentId(),
                mlResponse.prediction(),
                mlResponse.predictionLabel(),
                mlResponse.riskLevel(),
                mlResponse.dropoutProbability(),
                mlResponse.retentionProbability(),
                mlResponse.modelUsed(),
                mlResponse.modelAccuracy(),
                mlResponse.timestamp()
        );
    }

    /**
     * Executa predição múltipla (em lote) consumindo o modelo de Machine Learning
     */
    public BatchPredictionResponseDTO predictBatch(BatchPredictionRequestDTO request) {
        List<Map<String, Object>> students = request.students();

        if (students == null || students.isEmpty()) {
            throw new IllegalArgumentException(
                    "A lista de estudantes não pode ser vazia."
            );
        }

        // Se algum estudante vier no formato { "studentId": "...", "features": { ... } }, descompacta
        List<Map<String, Object>> normalizedStudents = new ArrayList<>();
        List<String> studentIds = new ArrayList<>();

        for (Map<String, Object> student : students) {
            if (student.containsKey("features") && student.get("features") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> featMap = (Map<String, Object>) student.get("features");
                normalizedStudents.add(featMap);
                studentIds.add(student.get("studentId") != null ? student.get("studentId").toString() : null);
            } else {
                normalizedStudents.add(student);
                studentIds.add(student.get("studentId") != null ? student.get("studentId").toString() : null);
            }
        }

        MlBatchPredictionResponseDTO mlBatchResponse = mlClient.predictBatch(normalizedStudents);

        if (mlBatchResponse == null) {
            throw new IllegalStateException(
                    "O modelo de Machine Learning não retornou uma resposta em lote válida."
            );
        }

        List<PredictionResponseDTO> responseList = new ArrayList<>();
        List<MlPredictionResponseDTO> mlPredictions = mlBatchResponse.predictions();

        for (int i = 0; i < mlPredictions.size(); i++) {
            MlPredictionResponseDTO item = mlPredictions.get(i);
            String id = (i < studentIds.size()) ? studentIds.get(i) : null;

            responseList.add(new PredictionResponseDTO(
                    id,
                    item.prediction(),
                    item.predictionLabel(),
                    item.riskLevel(),
                    item.dropoutProbability(),
                    item.retentionProbability(),
                    item.modelUsed(),
                    item.modelAccuracy(),
                    item.timestamp()
            ));
        }

        return new BatchPredictionResponseDTO(
                mlBatchResponse.totalStudents(),
                responseList,
                mlBatchResponse.summary(),
                mlBatchResponse.timestamp()
        );
    }
}
