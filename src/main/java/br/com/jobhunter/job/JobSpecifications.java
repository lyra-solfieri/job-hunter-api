package br.com.jobhunter.job;

import br.com.jobhunter.job.dto.JobFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class JobSpecifications {

    private JobSpecifications() {
    }

    public static Specification<Job> matching(JobFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.q())) {
                String like = "%" + filter.q().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("company")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (StringUtils.hasText(filter.company())) {
                predicates.add(cb.equal(
                        cb.lower(root.get("company")),
                        filter.company().trim().toLowerCase(Locale.ROOT)
                ));
            }
            if (StringUtils.hasText(filter.location())) {
                String like = "%" + filter.location().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.lower(root.get("location")), like));
            }
            if (StringUtils.hasText(filter.source())) {
                predicates.add(cb.equal(
                        cb.lower(root.get("source")),
                        filter.source().trim().toLowerCase(Locale.ROOT)
                ));
            }
            if (filter.remote() != null) {
                predicates.add(cb.equal(root.get("remote"), filter.remote()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
