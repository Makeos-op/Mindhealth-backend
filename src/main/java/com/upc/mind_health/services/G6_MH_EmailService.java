package com.upc.mind_health.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

// Envía correo real vía la API HTTP de Brevo (https://brevo.com), no por SMTP directo:
// muchas plataformas de hosting (Render, Railway, etc.) bloquean el tráfico saliente por
// los puertos SMTP en sus planes gratuitos, así que la API sobre HTTPS es la única opción viable.
// A diferencia de Resend, Brevo permite verificar un solo correo remitente (sin dominio propio)
// y enviar desde ahí a cualquier destinatario.
@Service
public class G6_MH_EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${mindhealth.brevo.api-key:}")
    private String apiKey;

    @Value("${mindhealth.mail.remitente:}")
    private String remitente;

    public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("No se pudo enviar el correo a " + destinatario + ": falta configurar BREVO_API_KEY.");
            return;
        }
        if (remitente == null || remitente.isBlank()) {
            System.err.println("No se pudo enviar el correo a " + destinatario + ": falta configurar MAIL_REMITENTE.");
            return;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> sender = new HashMap<>();
            sender.put("email", remitente);
            sender.put("name", "Mind Health");

            Map<String, Object> destinatarioMap = new HashMap<>();
            destinatarioMap.put("email", destinatario);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", sender);
            body.put("to", new Object[]{destinatarioMap});
            body.put("subject", asunto);
            body.put("textContent", cuerpo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(BREVO_API_URL, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            System.err.println("No se pudo enviar el correo a " + destinatario + ": " + e.getMessage());
        }
    }
}
