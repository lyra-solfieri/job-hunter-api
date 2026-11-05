package br.com.jobhunter.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Optional<Job> findByFingerprint(String fingerprint);

    Optional<Job> findBySourceAndExternalId(String source, String externalId);

    boolean existsByFingerprint(String fingerprint);

    boolean existsBySourceAndExternalId(String source, String externalId);

    long countBySource(String source);

    @Query("select j.source as source, count(j) as total from Job j group by j.source order by count(j) desc")
    List<SourceCount> countGroupedBySource();

    interface SourceCount {
        String getSource();

        long getTotal();
    }
}
