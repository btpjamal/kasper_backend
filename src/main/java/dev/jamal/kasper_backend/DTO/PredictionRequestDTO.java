package dev.jamal.kasper_backend.DTO;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PredictionRequestDTO {

    @JsonProperty("studentId")
    private String studentId;

    @JsonProperty("features")
    private Map<String, Object> features;

    // Captura qualquer outro campo solto enviado diretamente no JSON
    private Map<String, Object> additionalProperties = new HashMap<>();

    public PredictionRequestDTO() {
    }

    public PredictionRequestDTO(String studentId, Map<String, Object> features) {
        this.studentId = studentId;
        this.features = features;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Map<String, Object> getFeatures() {
        if (features != null && !features.isEmpty()) {
            return features;
        }
        return additionalProperties;
    }

    public void setFeatures(Map<String, Object> features) {
        this.features = features;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        if (!"studentId".equals(name) && !"features".equals(name) && !"token".equals(name)) {
            this.additionalProperties.put(name, value);
        }
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
}
