package com.upc.mind_health.services;

import com.upc.mind_health.dtos.G6_MH_ChatResponseDTO;
import com.upc.mind_health.entities.G6_MH_MensajeChat;
import com.upc.mind_health.entities.G6_MH_SesionTerapia;
import com.upc.mind_health.repositories.G6_MH_MensajeChatRepository;
import com.upc.mind_health.repositories.G6_MH_SesionTerapiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class G6_MH_IaTerapiaService {

    private final G6_MH_SesionTerapiaRepository sesionRepository;
    private final G6_MH_MensajeChatRepository mensajeRepository;

    @Value("${mindhealth.gemini.api-key:SIN_LLAVE}")
    private String apiKey;

    private final String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    @Transactional // Todo este flujo se ejecuta junto: o se guarda todo o nada
    public G6_MH_ChatResponseDTO procesarSesionRealConIA(String textoUsuario, Long idSesion) {
        if (textoUsuario == null || textoUsuario.trim().isEmpty()) {
            throw new RuntimeException("El texto de la sesión no puede estar vacío.");
        }

        // 1. PERSISTENCIA EXACTA: Buscar la sesión usando el idSesion que viene del controlador
        G6_MH_SesionTerapia sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new RuntimeException("La sesión de terapia con ID " + idSesion + " no existe en el sistema."));

        // 2. PERSISTENCIA PACIENTE: Registrar lo que el usuario escribió
        G6_MH_MensajeChat mensajePaciente = G6_MH_MensajeChat.builder()
                .sesion(sesion)
                .contenido(textoUsuario)
                .tipoRemitente("PACIENTE")
                .fechaEnvio(LocalDateTime.now())
                .build();
        mensajeRepository.save(mensajePaciente);

        try {
            RestTemplate restTemplate = new RestTemplate();

            String promptCompleto = "Eres un asistente de inteligencia artificial experto en psicología y soporte " +
                    "emocional para la plataforma Mind Health. "
                    + "Tu trabajo es analizar el texto que escribe un paciente en crisis o desahogo. "
                    + "Analiza el siguiente texto del paciente: \"" + textoUsuario + "\". "
                    + "REGLAS CRÍTICAS:\n"
                    + "1. Determina el nivel de urgencia únicamente como uno de estos valores: " +
                    "[BAJO, MEDIO, ALTO, CRÍTICO]. Si el usuario expresa ideas de hacerse daño o morir, " +
                    "pon siempre CRÍTICO.\n"
                    + "2. Da una respuesta altamente empática y humana, validando sus emociones.\n"
                    + "3. Brinda 2 o 3 recomendaciones o técnicas de relajación claras.\n"
                    + "4. Si la urgencia es ALTO o CRÍTICO, incluye la línea de ayuda telefónica oficial de salud " +
                    "mental de Perú (Línea 113 Opción 5).\n\n"
                    + "Debes responder estrictamente imitando esta estructura exacta separada por barras verticales " +
                    "(||), sin usar negritas, saltos de línea ni asteriscos para que mi backend pueda procesarla:\n" +
                    "EMOCION: [Emoción] || URGENCIA: [Nivel] || RESPUESTA: [Tu respuesta empática] || " +
                    "RECOMENDACIONES: [Recomendación 1; Recomendación 2] || AYUDA: [Línea de ayuda si aplica]";

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", promptCompleto);

            Map<String, Object> partsWrapper = new HashMap<>();
            partsWrapper.put("parts", Collections.singletonList(textPart));
            requestBody.put("contents", Collections.singletonList(partsWrapper));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(geminiApiUrl + apiKey, entity, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            String respuestaCrudaIA = obtenerTextoDeRespuestaGoogle(responseBody);

            // Parseamos la respuesta para construir el DTO final
            G6_MH_ChatResponseDTO responseDTO = parsearRespuestaIA(respuestaCrudaIA, textoUsuario);

            // 3. PERSISTENCIA IA: Guardar la respuesta empática en la tabla de mensajes
            G6_MH_MensajeChat mensajeIA = G6_MH_MensajeChat.builder()
                    .sesion(sesion)
                    .contenido(responseDTO.getRespuestaEmpatica())
                    .tipoRemitente("IA_ASISTENTE")
                    .fechaEnvio(LocalDateTime.now())
                    .build();
            mensajeRepository.save(mensajeIA);

            // 4. METADATOS: Actualizar el estado de la cabecera de la sesión
            sesion.setUltimaEmocionDetectada(responseDTO.getEmocionDetectada());
            sesion.setNivelUrgenciaActual(responseDTO.getNivelUrgencia());
            sesionRepository.save(sesion);

            return responseDTO;

        } catch (Exception e) {
            System.err.println("Error crítico de integración con Gemini API: " + e.getMessage());
            // 🚨 Al eliminar el fallback, lanzamos el error para que el controlador lo atrape
            throw new RuntimeException("No se pudo procesar el análisis cognitivo con la IA: " + e.getMessage());
        }
    }

    private G6_MH_ChatResponseDTO parsearRespuestaIA(String cruda, String textoOriginal) {
        try {
            if (cruda != null && cruda.contains("||")) {
                String[] partes = cruda.split("\\|\\|");
                String emocion = partes[0].replace("EMOCION:", "").trim();
                String urgencia = partes[1].replace("URGENCIA:", "").trim();
                String respuestaEmpatica = partes[2].replace("RESPUESTA:", "").trim();
                String recs = partes[3].replace("RECOMENDACIONES:", "").trim();
                String ayuda = partes.length > 4 ? partes[4].replace("AYUDA:", "").trim() : "";

                List<String> recomendacionesLimpisimas = Arrays.stream(recs.split(";"))
                        .map(String::trim)
                        .filter(item -> !item.isEmpty() && !item.equals(","))
                        .collect(Collectors.toList());

                List<String> ayudaLimpia = (ayuda.isEmpty() || ayuda.toLowerCase().contains("no aplica"))
                        ? new java.util.ArrayList<>()
                        : Arrays.stream(ayuda.split(";")).map(String::trim).collect(Collectors.toList());

                return G6_MH_ChatResponseDTO.builder()
                        .textoUsuario(textoOriginal)
                        .emocionDetectada(emocion)
                        .nivelUrgencia(urgencia)
                        .respuestaEmpatica(respuestaEmpatica)
                        .recomendaciones(recomendacionesLimpisimas)
                        .lineasDeAyuda(ayudaLimpia)
                        .build();
            }

            String urgenciaDetectada = "MEDIO";
            if (textoOriginal != null && (textoOriginal.toLowerCase().contains("daño") || textoOriginal.toLowerCase().contains("morir"))) {
                urgenciaDetectada = "CRÍTICO";
            }

            return G6_MH_ChatResponseDTO.builder()
                    .textoUsuario(textoOriginal)
                    .emocionDetectada("Análisis Emocional Inteligente")
                    .nivelUrgencia(urgenciaDetectada)
                    .respuestaEmpatica(cruda)
                    .recomendaciones(Arrays.asList("Realiza ejercicios de respiración diafragmática.", "Busca un espacio seguro de conversación."))
                    .lineasDeAyuda(urgenciaDetectada.equals("CRÍTICO") ? Collections.singletonList("Línea 113 Opción 5 (Perú)") : new java.util.ArrayList<>())
                    .build();

        } catch (Exception e) {
            return G6_MH_ChatResponseDTO.builder()
                    .textoUsuario(textoOriginal)
                    .emocionDetectada("Detección Emocional")
                    .nivelUrgencia("MEDIO")
                    .respuestaEmpatica(cruda == null || cruda.isEmpty() ? "Estamos aquí para procesar tu desahogo de forma segura." : cruda)
                    .recomendaciones(Arrays.asList("Respira profundo.", "Conversa con un especialista."))
                    .lineasDeAyuda(new java.util.ArrayList<>())
                    .build();
        }
    }

    private String obtenerTextoDeRespuestaGoogle(Map<String, Object> responseBody) {
        if (responseBody != null && responseBody.containsKey("candidates")) {
            List<?> candidates = (List<?>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
                Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
                if (content != null) {
                    List<?> parts = (List<?>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
                        return (String) firstPart.get("text");
                    }
                }
            }
        }
        return "";
    }
}