package br.com.jobhunter.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(name = "ErrorResponse", description = "Resposta padrao de erro da API")
public record ErrorResponse(
        @Schema(description = "Indica falha da operacao", example = "false")
        boolean success,

        @Schema(description = "Mensagem descritiva do erro", example = "Erro de validacao")
        String message,

        @Schema(description = "Detalhes por campo, quando aplicavel")
        List<ApiError> errors,

        @Schema(description = "Momento do erro em UTC", example = "2026-11-05T12:00:00Z")
        Instant timestamp
) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(false, message, List.of(), Instant.now());
    }

    public static ErrorResponse of(String message, List<ApiError> errors) {
        return new ErrorResponse(false, message, errors, Instant.now());
    }
}
