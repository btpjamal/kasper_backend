package dev.jamal.kasper_backend.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BatchPredictionResponseDTO(
        @JsonProperty("total_students")
        Integer totalStudents,

        @JsonProperty("predictions")
        List<PredictionResponseDTO> predictions,

        @JsonProperty("summary")
        Map<String, Object> summary,

        @JsonProperty("timestamp")
        Object timestamp
) {
}
