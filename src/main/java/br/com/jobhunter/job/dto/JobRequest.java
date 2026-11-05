package br.com.jobhunter.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(name = "JobRequest", description = "Dados de uma vaga normalizada")
public record JobRequest(
        @Schema(description = "Titulo da vaga", example = "Desenvolvedor Java Backend")
        @NotBlank @Size(max = 500) String title,

        @Schema(description = "Empresa contratante", example = "Acme")
        @Size(max = 255) String company,

        @Schema(description = "Localizacao da vaga", example = "Sao Paulo - SP")
        @Size(max = 255) String location,

        @Schema(description = "Descricao completa da vaga")
        String description,

        @Schema(description = "URL original do anuncio", example = "https://example.com/jobs/123")
        @Size(max = 1000) String url,

        @Schema(description = "Fonte de origem", example = "gupy")
        @Size(max = 100) String source,

        @Schema(description = "Identificador da vaga na fonte de origem", example = "12345")
        @Size(max = 255) String externalId,

        @Schema(description = "Indica trabalho remoto", example = "true")
        Boolean remote,

        @Schema(description = "Faixa salarial informada", example = "R$ 8.000 - R$ 12.000")
        @Size(max = 255) String salary,

        @Schema(description = "Tipo de contratacao", example = "CLT")
        @Size(max = 100) String employmentType,

        @Schema(description = "Data de publicacao na fonte", example = "2026-11-05T12:00:00Z")
        Instant postedAt
) {
}
