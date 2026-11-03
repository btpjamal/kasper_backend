package dev.jamal.kasper_backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

public record BatchPredictionRequestDTO(
        @NotEmpty(message = "A lista de estudantes não pode ser vazia")
        @JsonProperty("students")
        List<Map<String, Object>> students
) {
}
