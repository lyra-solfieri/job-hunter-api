package br.com.jobhunter.notification.telegram;

import br.com.jobhunter.config.TelegramProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class TelegramService {

    private final TelegramProperties properties;
    private final RestClient restClient;

    public TelegramService(TelegramProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl("https://api.telegram.org")
                .build();
    }

    public boolean isConfigured() {
        return properties.enabled()
                && StringUtils.hasText(properties.botToken())
                && StringUtils.hasText(properties.chatId());
    }

    public void sendMessage(String text) {
        if (!isConfigured()) {
            log.debug("Telegram desabilitado ou sem token/chat-id; mensagem ignorada");
            return;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chat_id", properties.chatId());
        payload.put("text", text);
        payload.put("disable_web_page_preview", true);

        try {
            restClient.post()
                    .uri("/bot{token}/sendMessage", properties.botToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Falha ao enviar mensagem para o Telegram: {}", ex.getMessage());
        }
    }

    public void notifyIngested(int created, int updated) {
        if (!properties.notifyOnIngest() || created == 0) {
            return;
        }
        sendMessage(String.format(
                "Job Hunter: %d nova(s) vaga(s) ingerida(s), %d atualizada(s).",
                created,
                updated
        ));
    }
}
