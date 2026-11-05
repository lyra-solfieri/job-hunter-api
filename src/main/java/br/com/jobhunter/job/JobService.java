package br.com.jobhunter.job;

import br.com.jobhunter.common.exception.DuplicateJobException;
import br.com.jobhunter.common.exception.NotFoundException;
import br.com.jobhunter.job.dto.IngestResult;
import br.com.jobhunter.job.dto.JobFilter;
import br.com.jobhunter.job.dto.JobRequest;
import br.com.jobhunter.notification.telegram.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final TelegramService telegramService;

    @Transactional(readOnly = true)
    public Page<Job> search(JobFilter filter, Pageable pageable) {
        return jobRepository.findAll(JobSpecifications.matching(filter), pageable);
    }

    @Transactional(readOnly = true)
    public Job get(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Vaga nao encontrada: " + id));
    }

    @Transactional
    public Job create(JobRequest request) {
        String source = normalizeSource(request.source());
        String fingerprint = JobFingerprint.of(request.title(), request.company(), request.location());

        findExisting(source, request.externalId(), fingerprint)
                .ifPresent(existing -> {
                    throw new DuplicateJobException("Vaga ja cadastrada com id " + existing.getId());
                });

        Job job = new Job();
        apply(job, request, source, fingerprint, true);
        return jobRepository.save(job);
    }

    @Transactional
    public Job update(Long id, JobRequest request) {
        Job job = get(id);
        String source = normalizeSource(request.source());
        String fingerprint = JobFingerprint.of(request.title(), request.company(), request.location());

        Optional<Job> sameFingerprint = jobRepository.findByFingerprint(fingerprint);
        if (sameFingerprint.isPresent() && !sameFingerprint.get().getId().equals(id)) {
            throw new DuplicateJobException("Outra vaga ja usa os mesmos dados (id "
                    + sameFingerprint.get().getId() + ")");
        }

        apply(job, request, source, fingerprint, false);
        return jobRepository.save(job);
    }

    @Transactional
    public void delete(Long id) {
        Job job = get(id);
        jobRepository.delete(job);
    }

    @Transactional
    public IngestResult ingest(List<JobRequest> requests) {
        int created = 0;
        int updated = 0;
        List<Long> jobIds = new ArrayList<>(requests.size());

        for (JobRequest request : requests) {
            String source = normalizeSource(request.source());
            String fingerprint = JobFingerprint.of(request.title(), request.company(), request.location());

            Optional<Job> existing = findExisting(source, request.externalId(), fingerprint);
            if (existing.isPresent()) {
                Job job = existing.get();
                apply(job, request, source, fingerprint, false);
                jobRepository.save(job);
                jobIds.add(job.getId());
                updated++;
            } else {
                Job job = new Job();
                apply(job, request, source, fingerprint, true);
                Job saved = jobRepository.saveAndFlush(job);
                jobIds.add(saved.getId());
                created++;
            }
        }

        telegramService.notifyIngested(created, updated);
        return new IngestResult(created, updated, requests.size(), jobIds);
    }

    @Transactional(readOnly = true)
    public List<JobRepository.SourceCount> countBySource() {
        return jobRepository.countGroupedBySource();
    }

    private Optional<Job> findExisting(String source, String externalId, String fingerprint) {
        if (StringUtils.hasText(externalId)) {
            Optional<Job> byExternalId = jobRepository.findBySourceAndExternalId(source, externalId);
            if (byExternalId.isPresent()) {
                return byExternalId;
            }
        }
        return jobRepository.findByFingerprint(fingerprint);
    }

    private void apply(Job job, JobRequest request, String source, String fingerprint, boolean creating) {
        job.setTitle(request.title().trim());
        job.setCompany(trimToNull(request.company()));
        job.setLocation(trimToNull(request.location()));
        job.setDescription(trimToNull(request.description()));
        job.setUrl(trimToNull(request.url()));
        job.setSource(source);
        job.setFingerprint(fingerprint);
        job.setRemote(request.remote());
        job.setSalary(trimToNull(request.salary()));
        job.setEmploymentType(trimToNull(request.employmentType()));
        job.setPostedAt(request.postedAt());

        String externalId = trimToNull(request.externalId());
        if (externalId != null || creating) {
            job.setExternalId(externalId);
        }
    }

    private String normalizeSource(String source) {
        if (!StringUtils.hasText(source)) {
            return "manual";
        }
        return source.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
