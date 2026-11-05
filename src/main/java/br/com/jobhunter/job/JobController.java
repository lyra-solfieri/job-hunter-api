package br.com.jobhunter.job;

import br.com.jobhunter.common.response.ApiResponse;
import br.com.jobhunter.common.response.PageResponse;
import br.com.jobhunter.job.dto.IngestResult;
import br.com.jobhunter.job.dto.JobFilter;
import br.com.jobhunter.job.dto.JobIngestRequest;
import br.com.jobhunter.job.dto.JobRequest;
import br.com.jobhunter.job.dto.JobResponse;
import br.com.jobhunter.job.dto.SourceCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/jobs", "/api/jobs"})
@RequiredArgsConstructor
@Tag(name = "Vagas", description = "Cadastro, pesquisa e ingestao de vagas")
public class JobController {

    private final JobService jobService;

    @GetMapping
    @Operation(summary = "Pesquisa vagas com filtros e paginacao")
    public ApiResponse<PageResponse<JobResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Boolean remote,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        JobFilter filter = new JobFilter(q, company, location, source, remote);
        PageResponse<JobResponse> page = PageResponse.from(
                jobService.search(filter, pageable).map(JobResponse::from)
        );
        return ApiResponse.success("Vagas consultadas com sucesso", page);
    }

    @GetMapping("/stats")
    @Operation(summary = "Conta vagas agrupadas por fonte")
    public ApiResponse<List<SourceCountResponse>> stats() {
        List<SourceCountResponse> counts = jobService.countBySource().stream()
                .map(count -> new SourceCountResponse(count.getSource(), count.getTotal()))
                .toList();
        return ApiResponse.success("Estatisticas consultadas com sucesso", counts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta os detalhes de uma vaga")
    public ApiResponse<JobResponse> get(@PathVariable Long id) {
        return ApiResponse.success("Vaga encontrada", JobResponse.from(jobService.get(id)));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma vaga manualmente")
    public ResponseEntity<ApiResponse<JobResponse>> create(@Valid @RequestBody JobRequest request) {
        Job job = jobService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/jobs/" + job.getId()))
                .body(ApiResponse.success("Vaga criada com sucesso", JobResponse.from(job)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma vaga existente")
    public ApiResponse<JobResponse> update(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return ApiResponse.success("Vaga atualizada com sucesso", JobResponse.from(jobService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma vaga")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ingest")
    @Operation(summary = "Ingere um lote de vagas normalizadas pelos coletores")
    public ResponseEntity<ApiResponse<IngestResult>> ingest(@Valid @RequestBody JobIngestRequest request) {
        IngestResult result = jobService.ingest(request.jobs());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Ingestao concluida", result));
    }
}
