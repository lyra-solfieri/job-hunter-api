package br.com.jobhunter.job.dto;

import br.com.jobhunter.job.Job;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "JobResponse", description = "Vaga retornada pela API")
public record JobResponse(
        @Schema(description = "Identificador interno da vaga", example = "1")
        Long id,

        @Schema(description = "Identificador da vaga na fonte", example = "12345")
        String externalId,

        @Schema(description = "Titulo da vaga", example = "Desenvolvedor Java Backend")
        String title,

        @Schema(description = "Empresa contratante", example = "Acme")
        String company,

        @Schema(description = "Localizacao", example = "Sao Paulo - SP")
        String location,

        @Schema(description = "Descricao completa")
        String description,

        @Schema(description = "URL original do anuncio")
        String url,

        @Schema(description = "Fonte de origem", example = "gupy")
        String source,

        @Schema(description = "Indica trabalho remoto")
        Boolean remote,

        @Schema(description = "Faixa salarial informada")
        String salary,

        @Schema(description = "Tipo de contratacao")
        String employmentType,

        @Schema(description = "Data de publicacao")
        Instant postedAt,

        @Schema(description = "Data de criacao do registro")
        Instant createdAt,

        @Schema(description = "Data da ultima atualizacao")
        Instant updatedAt
) {

    public static JobResponse from(Job job) {
        return new JobResponse(
                job.getId(),
                job.getExternalId(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getDescription(),
                job.getUrl(),
                job.getSource(),
                job.getRemote(),
                job.getSalary(),
                job.getEmploymentType(),
                job.getPostedAt(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
