package br.com.jobhunter.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SourceCountResponse", description = "Total de vagas por fonte")
public record SourceCountResponse(
        @Schema(description = "Fonte de origem", example = "gupy")
        String source,

        @Schema(description = "Quantidade de vagas", example = "42")
        long total
) {
}
