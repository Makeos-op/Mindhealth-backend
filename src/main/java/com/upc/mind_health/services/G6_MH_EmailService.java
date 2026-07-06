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

// Envía correo real vía la API HTTP de Resend (https://resend.com), no por SMTP directo:
// muchas plataformas de hosting (Render, Railway, etc.) bloquean el tráfico saliente por
// los puertos SMTP en sus planes gratuitos, así que la API sobre HTTPS es la única opción viable.
@Service
public class G6_MH_EmailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Value("${mindhealth.resend.api-key:}")
    private String apiKey;

    @Value("${mindhealth.mail.remitente:onboarding@resend.dev}")
    private String remitente;

    public void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("No se pudo enviar el correo a " + destinatario + ": falta configurar RESEND_API_KEY.");
            return;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> body = new HashMap<>();
            body.put("from", remitente);
            body.put("to", new String[]{destinatario});
            body.put("subject", asunto);
            body.put("text", cuerpo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(RESEND_API_URL, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            System.err.println("No se pudo enviar el correo a " + destinatario + ": " + e.getMessage());
        }
    }
}
