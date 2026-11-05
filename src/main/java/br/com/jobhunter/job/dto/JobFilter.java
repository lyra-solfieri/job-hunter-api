package br.com.jobhunter.job.dto;

public record JobFilter(
        String q,
        String company,
        String location,
        String source,
        Boolean remote
) {
}
