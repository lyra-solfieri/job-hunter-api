package br.com.jobhunter.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(name = "JobIngestRequest", description = "Lote de vagas normalizadas enviado por um coletor")
public record JobIngestRequest(
        @Schema(description = "Vagas a serem ingeridas")
        @NotEmpty @Valid List<JobRequest> jobs
) {
}
