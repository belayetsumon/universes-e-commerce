package com.ecommerce.app.module.communication.sender;

import com.ecommerce.app.module.communication.dto.CommunicationSendResult;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.dto.RenderedMessage;
import com.ecommerce.app.module.communication.model.MessageProvider;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class ProviderHttpClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public CommunicationSendResult post(MessageDispatchRequest request, MessageProvider provider, RenderedMessage renderedMessage, String successCode) {
        if (provider == null) {
            return CommunicationSendResult.failed("PROVIDER_MISSING", "Message provider is required.");
        }
        if (isBlank(provider.getBaseUrl())) {
            return CommunicationSendResult.failed("PROVIDER_URL_MISSING", "Provider base URL is required.");
        }

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(provider.getBaseUrl().trim()))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(buildJson(request, provider, renderedMessage)));

            if (!isBlank(provider.getApiKey())) {
                builder.header("Authorization", "Bearer " + provider.getApiKey().trim());
                builder.header("X-API-Key", provider.getApiKey().trim());
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            String responseBody = truncate(response.body());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return CommunicationSendResult.sent(successCode, responseBody);
            }
            return CommunicationSendResult.failed("HTTP_" + response.statusCode(), responseBody);
        } catch (Exception ex) {
            return CommunicationSendResult.failed("HTTP_SEND_FAILED", ex.getMessage());
        }
    }

    private String buildJson(MessageDispatchRequest request, MessageProvider provider, RenderedMessage renderedMessage) {
        return "{"
                + "\"channel\":\"" + json(request.getChannel().name()) + "\","
                + "\"eventType\":\"" + json(request.getEventType().name()) + "\","
                + "\"recipient\":\"" + json(request.getRecipient()) + "\","
                + "\"subject\":\"" + json(renderedMessage.getSubject()) + "\","
                + "\"body\":\"" + json(renderedMessage.getBody()) + "\","
                + "\"senderId\":\"" + json(provider.getSenderId()) + "\","
                + "\"payloadJson\":" + payloadJson(request.getPayloadJson())
                + "}";
    }

    private String payloadJson(String payloadJson) {
        if (isBlank(payloadJson)) {
            return "null";
        }
        String trimmed = payloadJson.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            return trimmed;
        }
        return "\"" + json(trimmed) + "\"";
    }

    private String json(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String truncate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() > 1000 ? value.substring(0, 1000) : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
