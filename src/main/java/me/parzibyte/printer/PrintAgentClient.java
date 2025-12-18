package me.parzibyte.printer;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

@Component
public class PrintAgentClient {
    private final RestTemplate rt = new RestTemplate();

    // ✅ CORREGIDO: ahora apunta al puerto y endpoint correctos
    private final String baseUrl = "http://host.docker.internal:3001";

    /**
     * Envía texto simple al agente de impresión.
     */
    public void printText(String text) {
        rt.postForEntity(baseUrl + "/print/text", text, String.class);
    }

    /**
     * Envía bytes en base64 al agente.
     */
    public void printBytes(byte[] bytes) {
        String b64 = Base64Utils.encodeToString(bytes);
        rt.postForEntity(baseUrl + "/print/bytes", b64, String.class);
    }

    /**
     * 🟢 Abre el cajón de dinero mediante el endpoint correcto.
     */
    public void openDrawer() {
        try {
            String payload = "{}"; // el agente requiere cuerpo vacío tipo JSON
            ResponseEntity<String> res = rt.postForEntity(baseUrl + "/drawer/open", payload, String.class);
            System.out.println("✅ Cajón abierto: " + res.getBody());
        } catch (Exception e) {
            System.err.println("❌ Error al abrir cajón: " + e.getMessage());
        }
    }
}
