package br.com.jobhunter.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class JobApiIntegrationTest {

    private static final String INGEST_TOKEN = "test-ingest-token";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JobRepository jobRepository;

    @BeforeEach
    void cleanDatabase() {
        jobRepository.deleteAll();
    }

    @Test
    void createsAndSearchesJob() throws Exception {
        mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Desenvolvedor Java",
                                  "company": "Acme",
                                  "location": "Remoto",
                                  "source": "manual",
                                  "remote": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("Desenvolvedor Java"));

        mockMvc.perform(get("/api/v1/jobs").param("q", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].company").value("Acme"));
    }

    @Test
    void keepsLegacyApiPathAvailable() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void rejectsInvalidJobPayload() throws Exception {
        mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": ""}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].message").isNotEmpty());
    }

    @Test
    void returnsNotFoundForUnknownJob() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void ingestRequiresValidToken() throws Exception {
        String body = """
                {"jobs":[{"title":"Backend Engineer","company":"Acme","source":"telegram"}]}
                """;

        mockMvc.perform(post("/api/v1/jobs/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/v1/jobs/ingest")
                        .header("X-Ingest-Token", "wrong-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/jobs/ingest")
                        .header("X-Ingest-Token", INGEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.created").value(1))
                .andExpect(jsonPath("$.data.updated").value(0));

        assertThat(jobRepository.count()).isEqualTo(1);
    }

    @Test
    void ingestDeduplicatesUsingExternalIdAndFingerprint() throws Exception {
        String first = """
                {"jobs":[{"title":"Engenheiro de Dados","company":"Acme","location":"Sao Paulo",
                           "source":"telegram","externalId":"10:20"}]}
                """;

        mockMvc.perform(post("/api/v1/jobs/ingest")
                        .header("X-Ingest-Token", INGEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.created").value(1));

        mockMvc.perform(post("/api/v1/jobs/ingest")
                        .header("X-Ingest-Token", INGEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.created").value(0))
                .andExpect(jsonPath("$.data.updated").value(1));

        String sameJobDifferentExternalId = """
                {"jobs":[{"title":"ENGENHEIRO DE DADOS","company":"acme","location":"Sao Paulo",
                           "source":"linkedin","externalId":"abc-123"}]}
                """;

        mockMvc.perform(post("/api/v1/jobs/ingest")
                        .header("Authorization", "Bearer " + INGEST_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sameJobDifferentExternalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.created").value(0))
                .andExpect(jsonPath("$.data.updated").value(1));

        assertThat(jobRepository.count()).isEqualTo(1);
    }
}
