package dev.jamal.kasper_backend.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MlPredictionResponseDTO(
        @JsonProperty("prediction")
        @JsonAlias({"dropout_prediction", "dropoutPrediction"})
        Integer prediction,

        @JsonProperty("prediction_label")
        @JsonAlias({"predictionLabel"})
        String predictionLabel,

        @JsonProperty("risk_level")
        @JsonAlias({"riskLevel"})
        String riskLevel,

        @JsonProperty("dropout_probability")
        @JsonAlias({"dropoutProbability"})
        Double dropoutProbability,

        @JsonProperty("retention_probability")
        @JsonAlias({"retentionProbability"})
        Double retentionProbability,

        @JsonProperty("model_used")
        @JsonAlias({"modelUsed", "modelVersion", "model_version"})
        String modelUsed,

        @JsonProperty("model_accuracy")
        @JsonAlias({"modelAccuracy"})
        Double modelAccuracy,

        @JsonProperty("timestamp")
        Object timestamp
) {
}
