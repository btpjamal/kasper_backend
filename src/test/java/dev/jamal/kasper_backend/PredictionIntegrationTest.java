package dev.jamal.kasper_backend;

import dev.jamal.kasper_backend.DTO.CreateApiTokenRequestDTO;
import dev.jamal.kasper_backend.Entity.Role;
import dev.jamal.kasper_backend.Entity.User;
import dev.jamal.kasper_backend.Repository.UserRepository;
import dev.jamal.kasper_backend.Service.ApiTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class PredictionIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApiTokenService apiTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_USER_EMAIL = "integration-test@kasper.ia";
    private static final String TEST_TOKEN = "INTEGRATION_TEST_TOKEN_123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        if (userRepository.findByEmail(TEST_USER_EMAIL).isEmpty()) {
            User user = User.builder()
                    .name("Integration Tester")
                    .email(TEST_USER_EMAIL)
                    .password(passwordEncoder.encode("secretPassword123"))
                    .role(Role.USER)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);

            apiTokenService.create(TEST_USER_EMAIL, new CreateApiTokenRequestDTO("Integration Token", TEST_TOKEN));
        }
    }

    @Test
    @DisplayName("Deve retornar erro 401 quando requisição não fornecer token")
    void testPredictionWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/predictions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\": \"ALUNO-001\", \"features\": {\"marital_status\": 1}}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Token de Machine Learning não fornecido")));
    }

    @Test
    @DisplayName("Deve retornar erro 401 quando requisição fornecer token inválido/inexistente")
    void testPredictionWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/v1/predictions")
                        .header("Authorization", "Bearer TOKEN_TOTALMENTE_INVALIDO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\": \"ALUNO-001\", \"features\": {\"marital_status\": 1}}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Token de Machine Learning inválido")));
    }

    @Test
    @DisplayName("Deve integrar com sucesso com a API de ML para predição única e retornar o resultado do modelo")
    void testSinglePredictionSuccess() throws Exception {
        String payload = """
                {
                  "studentId": "ALUNO-TESTE-001",
                  "features": {
                    "marital_status": 1,
                    "course": 2,
                    "gender": 1,
                    "age_at_enrollment": 20,
                    "unemployment_rate": 10.8
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/predictions")
                        .header("Authorization", "Bearer " + TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.student_id").value("ALUNO-TESTE-001"))
                .andExpect(jsonPath("$.prediction").isNumber())
                .andExpect(jsonPath("$.prediction_label").isNotEmpty())
                .andExpect(jsonPath("$.risk_level").isNotEmpty())
                .andExpect(jsonPath("$.dropout_probability").isNumber())
                .andExpect(jsonPath("$.retention_probability").isNumber())
                .andExpect(jsonPath("$.model_used").value("Logistic Regression"));
    }

    @Test
    @DisplayName("Deve integrar com sucesso com a API de ML para predição em lote (múltipla) e retornar o sumário")
    void testBatchPredictionSuccess() throws Exception {
        String payload = """
                {
                  "students": [
                    {
                      "studentId": "ALUNO-1",
                      "marital_status": 1,
                      "debtor": 1,
                      "tuition_fees_up_to_date": 0
                    },
                    {
                      "studentId": "ALUNO-2",
                      "marital_status": 1,
                      "scholarship_holder": 1,
                      "tuition_fees_up_to_date": 1
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/predictions/batch")
                        .header("X-API-KEY", TEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_students").value(2))
                .andExpect(jsonPath("$.predictions").isArray())
                .andExpect(jsonPath("$.summary.total_students").value(2))
                .andExpect(jsonPath("$.summary.dropout_rate_percent").isNumber());
    }
}
