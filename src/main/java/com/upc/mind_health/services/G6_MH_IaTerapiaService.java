package com.upc.mind_health.services;

import com.upc.mind_health.dtos.G6_MH_AlertaNotificacionDTO;
import com.upc.mind_health.dtos.G6_MH_CasoCriticoResponseDTO;
import com.upc.mind_health.dtos.G6_MH_ChatResponseDTO;
import com.upc.mind_health.entities.*;
import com.upc.mind_health.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class G6_MH_IaTerapiaService {
    private final G6_MH_UsuarioRepository usuarioRepository;
    private final G6_MH_SesionTerapiaRepository sesionRepository;
    private final G6_MH_MensajeChatRepository mensajeRepository;
    private final G6_MH_DerivacionRepository derivacionRepository;
    private final G6_MH_PsicologoRepository psicologoRepository;

    @Value("${mindhealth.gemini.api-key:SIN_LLAVE}")
    private String apiKey;

    private final String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    // LLAVE DE CIFRADO SIMÉTRICA (HU-10 - Requiere exactamente 16 caracteres para AES)
    private static final String SECRET_KEY = "MindHealthKey910";

    // Métodos utilitarios internos para encriptar y desencriptar
    private String cifrarAES(String texto) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] cifrado = cipher.doFinal(texto.getBytes());
            return Base64.getEncoder().encodeToString(cifrado);
        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar el mensaje para la base de datos", e);
        }
    }

    @Transactional
    public G6_MH_ChatResponseDTO procesarSesionRealConIA(String textoUsuario, Long idSesion) {
        if (textoUsuario == null || textoUsuario.trim().isEmpty()) {
            throw new RuntimeException("El texto de la sesión no puede estar vacío.");
        }

        // 1. PERSISTENCIA EXACTA
        G6_MH_SesionTerapia sesion;
        if (idSesion == null || !sesionRepository.existsById(idSesion)) {
            G6_MH_Usuario usuarioLogueado = usuarioRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("Usuario base no encontrado."));

            sesion = sesionRepository.save(G6_MH_SesionTerapia.builder()
                    .usuario(usuarioLogueado)
                    .fechaInicio(LocalDateTime.now())
                    .ultimaEmocionDetectada("Ninguna (Sesión Inicial)")
                    .nivelUrgenciaActual("BAJO")
                    .build());
        } else {
            sesion = sesionRepository.findById(idSesion).get();
            if ("FINALIZADA".equals(sesion.getEstado())) {
                throw new RuntimeException("No se pueden enviar mensajes a una sesión que ya ha sido FINALIZADA.");
            }
        }

        // 2. PERSISTENCIA PACIENTE CIFRADA (HU-10): Ciframos el contenido antes de ir a Postgres
        String textoCifradoPaciente = cifrarAES(textoUsuario);

        G6_MH_MensajeChat mensajePaciente = G6_MH_MensajeChat.builder()
                .sesion(sesion)
                .contenido(textoCifradoPaciente)
                .tipoRemitente("PACIENTE")
                .fechaEnvio(LocalDateTime.now())
                .build();
        mensajeRepository.save(mensajePaciente);

        try {
            RestTemplate restTemplate = new RestTemplate();

            // SE MANTIENE EL TEXTO EN CLARO PARA EL PROMPT: Asegura que el monitoreo sea real
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

            G6_MH_ChatResponseDTO responseDTO = parsearRespuestaIA(respuestaCrudaIA, textoUsuario);

            // 3. PERSISTENCIA IA CIFRADA (HU-10): Ciframos la respuesta empática antes de ir a Postgres
            String respuestaCifradaIA = cifrarAES(responseDTO.getRespuestaEmpatica());

            G6_MH_MensajeChat mensajeIA = G6_MH_MensajeChat.builder()
                    .sesion(sesion)
                    .contenido(respuestaCifradaIA)
                    .tipoRemitente("IA_ASISTENTE")
                    .fechaEnvio(LocalDateTime.now())
                    .build();

            mensajeRepository.save(mensajeIA);

            // 4. METADATOS EN CLARO PARA MONITOREO DE GRÁFICOS
            sesion.setUltimaEmocionDetectada(responseDTO.getEmocionDetectada());
            sesion.setNivelUrgenciaActual(responseDTO.getNivelUrgencia());
            sesionRepository.save(sesion);

            // HU-11 INTEGRACIÓN: Si el nivel es CRÍTICO, asignamos psicólogo en tiempo real
            if ("CRÍTICO".equalsIgnoreCase(responseDTO.getNivelUrgencia())) {
                ejecutarAsignacionAutomatica(sesion, responseDTO.getEmocionDetectada());
            }

            return responseDTO;

        } catch (Exception e) {
            System.err.println("Error crítico de integración con Gemini API: " + e.getMessage());
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

    @Transactional
    public Long obtenerOCrearSesionActiva(Long idUsuario) {
        return sesionRepository.findByUsuarioIdUsuarioAndEstado(idUsuario, "ACTIVA")
                .map(G6_MH_SesionTerapia::getIdSesion)
                .orElseGet(() -> {
                    G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                    G6_MH_SesionTerapia nuevaSesion = sesionRepository.save(G6_MH_SesionTerapia.builder()
                            .usuario(usuario)
                            .fechaInicio(LocalDateTime.now())
                            .estado("ACTIVA")
                            .ultimaEmocionDetectada("Ninguna (Inicial)")
                            .nivelUrgenciaActual("BAJO")
                            .build());
                    return nuevaSesion.getIdSesion();
                });
    }

    @Transactional
    public void finalizarSesionTerapia(Long idSesion) {
        G6_MH_SesionTerapia sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        sesion.setEstado("FINALIZADA");
        sesionRepository.save(sesion);
    }

    private void ejecutarAsignacionAutomatica(G6_MH_SesionTerapia sesion, String emocion) {
        List<G6_MH_Psicologo> disponibles = psicologoRepository.findByDisponibleTrue();

        if (!disponibles.isEmpty()) {
            G6_MH_Psicologo profesionalAsignado = disponibles.get(0);

            // 🌟 Construcción lineal: Asociamos la sesión directa en lugar del usuario
            G6_MH_Derivacion nuevaDerivacion = G6_MH_Derivacion.builder()
                    .motivo("Derivación Inmediata por Alerta IA: " + emocion)
                    .fecha(java.time.LocalDate.now())
                    .sesion(sesion)
                    .profesional(profesionalAsignado)
                    .build();

            derivacionRepository.save(nuevaDerivacion);

            profesionalAsignado.setDisponible(false);
            psicologoRepository.save(profesionalAsignado);

            //ESCENARIO 1: Simulación de Notificación Inteligente Push/Email
            dispararNotificacionInteligente(nuevaDerivacion, profesionalAsignado.getUsuario().getCorreo());

            System.out.println("Caso derivado linealmente al profesional ID: " + profesionalAsignado.getIdPsicologo());
        } else {
            System.out.println("Sin profesionales disponibles.");
        }
    }

    @Transactional(readOnly = true)
    public List<G6_MH_CasoCriticoResponseDTO> listarAlertasParaPsicologo(String correoPsicologo) {
        return derivacionRepository.findByProfesionalUsuarioCorreoOrderByFechaDesc(correoPsicologo).stream()
                .map(derivacion -> com.upc.mind_health.dtos.G6_MH_CasoCriticoResponseDTO.builder()
                        .idDerivacion(derivacion.getIdDerivacion())
                        .nombrePaciente(derivacion.getSesion().getUsuario().getNombre())
                        .fechaDerivacion(derivacion.getFecha())
                        .motivoAlerta(derivacion.getMotivo())
                        .mensajeConfirmacion("Asignación crítica automática por riesgo emocional.")
                        .ultimaEmocionHistorica(derivacion.getSesion().getUltimaEmocionDetectada())
                        .recomendacionesSeguimiento(Arrays.asList(
                                "Revisar los últimos mensajes cifrados de la sesión #" + derivacion.getSesion().getIdSesion(),
                                "Aplicar protocolo de contención cognitiva conductual para la emoción: " + derivacion.getSesion().getUltimaEmocionDetectada()
                        ))
                        .build())
                .collect(Collectors.toList());
    }

    private void dispararNotificacionInteligente(G6_MH_Derivacion derivacion, String correoDestino) {
        System.out.println("[NOTIFICACIÓN INTELIGENTE]");
        System.out.println("ENVIANDO ALERTA PUSH A: " + correoDestino);
        System.out.println("MENSAJE: ¡ALERTA DE CRISIS EMOCIONAL! Se te ha asignado el caso de la Sesión #"
                + derivacion.getSesion().getIdSesion() + ". Motivo: " + derivacion.getMotivo());
    }

    @Transactional
    public G6_MH_AlertaNotificacionDTO atenderYFecharCrisis(Long idDerivacion, String correoPsicologo, String notas) {
        G6_MH_Derivacion derivacion = derivacionRepository.findById(idDerivacion)
                .orElseThrow(() -> new RuntimeException("Registro de derivación no encontrado."));

        // Validación de seguridad: Que el psicólogo que atiende sea realmente el asignado
        if (!derivacion.getProfesional().getUsuario().getCorreo().equalsIgnoreCase(correoPsicologo)) {
            throw new RuntimeException("No tienes autorización para gestionar este caso crítico.");
        }

        // 1. Registrar el cierre clínico de la crisis
        derivacion.setEstadoAtencion("ATENDIDO");
        derivacion.setNotasSeguimiento(notas);
        derivacion.setFechaAtencion(LocalDateTime.now());
        derivacionRepository.save(derivacion);

        // 2. Liberar al psicólogo para que vuelva a figurar como DISPONIBLE para el bot
        G6_MH_Psicologo profesional = derivacion.getProfesional();
        profesional.setDisponible(true);
        psicologoRepository.save(profesional);

        return com.upc.mind_health.dtos.G6_MH_AlertaNotificacionDTO.builder()
                .idDerivacion(derivacion.getIdDerivacion())
                .correoProfesional(correoPsicologo)
                .mensajePush("Caso cerrado con éxito. El profesional vuelve a estar disponible en el sistema.")
                .severidad("RESOLVIDO")
                .fechaAlerta(LocalDate.now())
                .build();
    }
}