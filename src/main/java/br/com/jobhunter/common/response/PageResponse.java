package br.com.jobhunter.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(name = "PageResponse", description = "Pagina estavel de resultados")
public record PageResponse<T>(
        @Schema(description = "Itens da pagina")
        List<T> content,

        @Schema(description = "Numero da pagina, iniciando em zero", example = "0")
        int page,

        @Schema(description = "Tamanho da pagina", example = "20")
        int size,

        @Schema(description = "Total de elementos", example = "120")
        long totalElements,

        @Schema(description = "Total de paginas", example = "6")
        int totalPages,

        @Schema(description = "Indica a primeira pagina", example = "true")
        boolean first,

        @Schema(description = "Indica a ultima pagina", example = "false")
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> source) {
        return new PageResponse<>(
                source.getContent(),
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages(),
                source.isFirst(),
                source.isLast()
        );
    }
}
