package br.com.jobhunter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ingest")
public record IngestProperties(
        String token
) {
}
