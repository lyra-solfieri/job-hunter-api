package br.com.jobhunter.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "IngestResult", description = "Resultado de uma ingestao em lote")
public record IngestResult(
        @Schema(description = "Quantidade de vagas criadas", example = "10")
        int created,

        @Schema(description = "Quantidade de vagas atualizadas", example = "2")
        int updated,

        @Schema(description = "Quantidade total recebida", example = "12")
        int total,

        @Schema(description = "Identificadores das vagas processadas")
        List<Long> jobIds
) {
}
