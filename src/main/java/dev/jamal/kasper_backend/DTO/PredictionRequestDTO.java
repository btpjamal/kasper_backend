package dev.jamal.kasper_backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record PredictionRequestDTO(

        @NotBlank(message = "O identificador do aluno é obrigatório")
        String studentId,

        @NotEmpty(message = "As características do aluno são obrigatórias")
        Map<String, Object> features
) {
}

//      {
//        "studentId": "ALUNO-123",
//        "features": {
//        "attendanceRate": 0.72,
//        "averageGrade": 5.8,
//        "failedSubjects": 3,
//        "age": 16
//        }
//      }