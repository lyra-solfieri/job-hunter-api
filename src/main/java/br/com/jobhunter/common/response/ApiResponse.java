package br.com.jobhunter.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "ApiResponse", description = "Resposta padrao de sucesso da API")
public record ApiResponse<T>(
        @Schema(description = "Indica sucesso da operacao", example = "true")
        boolean success,

        @Schema(description = "Mensagem descritiva", example = "Operacao realizada com sucesso")
        String message,

        @Schema(description = "Dados da resposta")
        T data,

        @Schema(description = "Momento da resposta em UTC", example = "2026-11-05T12:00:00Z")
        Instant timestamp
) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, Instant.now());
    }
}
