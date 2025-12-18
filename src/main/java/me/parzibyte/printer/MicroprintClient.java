package me.parzibyte.printer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class MicroprintClient {
    private static final Logger log = LoggerFactory.getLogger(MicroprintClient.class);

    private final RestTemplate http;
    private final String baseUrl;
    private final String defaultPrinter;

    /**
     * Nota sobre configuración:
     * - application.properties:
     * microprint.base-url=http://host.docker.internal:3001
     * microprint.printer=ImpresoraTermica
     *
     * - O env vars (docker-compose):
     * MICROPRINT_BASE_URL=http://host.docker.internal:3001
     * MICROPRINT_PRINTER=ImpresoraTermica
     * (Spring relaxed binding hace el mapping automáticamente)
     */
    public MicroprintClient(
            RestTemplateBuilder builder,
            @Value("${microprint.base-url}") String baseUrl,
            @Value("${microprint.printer:ImpresoraTermica}") String defaultPrinter
    ) {
        this.http = builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.defaultPrinter = defaultPrinter;
        log.info("Microprint baseUrl={}, defaultPrinter={}", this.baseUrl, this.defaultPrinter);
    }

    /** Abre el cajón en el agente */
    public boolean openDrawer() {
        // 🟢 SOLUCIÓN: Cambiado a endpoint alternativo /open-drawer.
        // El endpoint /drawer/open estaba fallando o era incorrecto para tu versión de agente.
        String url = baseUrl + "/open-drawer"; 
        Map<String, Object> body = Map.of("printer", defaultPrinter);
        return post(url, body);
    }

    /** Imprime texto plano en el agente */
    public boolean printText(String text) {
        // CORREGIDO: Endpoint cambiado de /print a /print/text
        String url = baseUrl + "/print/text"; 
        Map<String, Object> body = new HashMap<>();
        body.put("printer", defaultPrinter);
        body.put("text", text);
        return post(url, body);
    }

    /** Envía bytes ESC/POS en base64 al agente */
    public boolean printRaw(byte[] bytes) {
        // CORREGIDO: Endpoint ajustado de /print a /print/raw
        String url = baseUrl + "/print/raw";
        Map<String, Object> body = new HashMap<>();
        body.put("printer", defaultPrinter);
        body.put("rawBase64", Base64.getEncoder().encodeToString(bytes));
        return post(url, body);
    }

    /* ========= Helper HTTP ========= */

    private boolean post(String url, Object body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<Map> resp =
                    http.postForEntity(url, new HttpEntity<>(body, headers), Map.class);

            log.info("POST {} -> status {}", url, resp.getStatusCodeValue());
            Object ok = (resp.getBody() != null ? resp.getBody().get("ok") : null);
            return resp.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(ok);
        } catch (RestClientException e) {
            log.warn("Error calling {}: {}", url, e.getMessage());
            return false;
        }
    }
}