package br.com.jobhunter.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiError", description = "Erro associado a um campo da requisicao")
public record ApiError(
        @Schema(description = "Nome do campo", example = "email")
        String field,

        @Schema(description = "Mensagem do erro", example = "E-mail invalido")
        String message
) {
}
