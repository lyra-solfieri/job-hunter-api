package br.com.jobhunter.notification.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import br.com.jobhunter.config.TelegramProperties;
import br.com.jobhunter.job.JobService;
import br.com.jobhunter.job.dto.JobRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramWebhookController {

    private static final int MAX_TITLE_LENGTH = 500;

    private final JobService jobService;
    private final TelegramService telegramService;
    private final TelegramProperties properties;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secret,
            @RequestBody JsonNode update
    ) {
        if (!secretMatches(secret)) {
            return ResponseEntity.status(401).build();
        }

        JsonNode message = update.path("message");
        String text = message.path("text").asText(null);
        if (!StringUtils.hasText(text)) {
            return ResponseEntity.ok().build();
        }

        long chatId = message.path("chat").path("id").asLong();
        long messageId = message.path("message_id").asLong();
        Instant postedAt = message.has("date")
                ? Instant.ofEpochSecond(message.path("date").asLong())
                : Instant.now();

        String title = text.strip().lines().findFirst().orElse("Vaga do Telegram");
        if (title.length() > MAX_TITLE_LENGTH) {
            title = title.substring(0, MAX_TITLE_LENGTH);
        }

        JobRequest request = new JobRequest(
                title,
                null,
                null,
                text,
                null,
                "telegram",
                chatId + ":" + messageId,
                null,
                null,
                null,
                postedAt
        );

        try {
            jobService.ingest(List.of(request));
            telegramService.sendMessage("Vaga registrada: " + title);
        } catch (Exception ex) {
            log.warn("Falha ao processar mensagem do Telegram: {}", ex.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    private boolean secretMatches(String provided) {
        if (!StringUtils.hasText(properties.webhookSecret())) {
            return true;
        }
        if (provided == null) {
            return false;
        }
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                properties.webhookSecret().getBytes(StandardCharsets.UTF_8)
        );
    }
}
