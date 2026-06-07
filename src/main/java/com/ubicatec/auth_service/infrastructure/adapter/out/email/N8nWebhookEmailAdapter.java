package com.ubicatec.auth_service.infrastructure.adapter.out.email;

import com.ubicatec.auth_service.domain.port.out.EmailNotifierPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI; import java.net.http.*;
import java.time.Duration;

@Component
public class N8nWebhookEmailAdapter implements EmailNotifierPort {

    @Value("${ubicatec.email.n8n.webhook-url}")
    private String webhookUrl;

    @Value("${ubicatec.email.n8n.webhook-secret}")
    private String webhookSecret;

    @Value("${ubicatec.email.n8n.timeout-ms:10000}")
    private int timeoutMs;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    @Override
    public void sendOtp(String to, String code, int expiresInMinutes) {

        String body = String.format(
                "{\"to\":\"%s\",\"code\":\"%s\",\"expiresInMinutes\":%d}",
                to, code, expiresInMinutes);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .header("x-webhook-token", webhookSecret)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofMillis(timeoutMs))
                .build();

        try {
            var resp = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new RuntimeException("n8n error: " + resp.statusCode() + " - " + resp.body());
            }

        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el código: " + e.getMessage(), e);
        }
    }
}

