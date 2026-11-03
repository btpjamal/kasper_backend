package dev.jamal.kasper_backend.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PredictionResponseDTO(
        @JsonProperty("student_id")
        String studentId,

        @JsonProperty("prediction")
        Integer prediction,

        @JsonProperty("prediction_label")
        String predictionLabel,

        @JsonProperty("risk_level")
        String riskLevel,

        @JsonProperty("dropout_probability")
        Double dropoutProbability,

        @JsonProperty("retention_probability")
        Double retentionProbability,

        @JsonProperty("model_used")
        String modelUsed,

        @JsonProperty("model_accuracy")
        Double modelAccuracy,

        @JsonProperty("timestamp")
        Object timestamp
) {
    public PredictionResponseDTO(
            Integer prediction,
            String predictionLabel,
            String riskLevel,
            Double dropoutProbability,
            Double retentionProbability,
            String modelUsed,
            Double modelAccuracy,
            Object timestamp
    ) {
        this(null, prediction, predictionLabel, riskLevel, dropoutProbability, retentionProbability, modelUsed, modelAccuracy, timestamp);
    }
}
