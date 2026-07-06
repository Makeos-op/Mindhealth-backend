package com.upc.mind_health.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.*;
import com.upc.mind_health.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
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
    private final G6_MH_ColaboracionRepository colaboracionRepository;
    private final G6_MH_EmailService emailService;

    @Value("${mindhealth.gemini.api-key:SIN_LLAVE}")
    private String apiKey;

    //(HU-10 - Requiere exactamente 16 caracteres para AES)
    @Value("${mindhealth.encryption.key}")
    private String secretKey;

    // Métodos utilitarios internos para encriptar y desencriptar (AES/CBC con IV aleatorio por mensaje)
    private String cifrarAES(String texto) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
            byte[] cifrado = cipher.doFinal(texto.getBytes());

            byte[] resultado = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, resultado, 0, iv.length);
            System.arraycopy(cifrado, 0, resultado, iv.length, cifrado.length);
            return Base64.getEncoder().encodeToString(resultado);
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
            sesion = sesionRepository.findById(idSesion)
                    .orElseThrow(() -> new RuntimeException("Sesión no encontrada."));
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
            G6_MH_Usuario usuario = sesion.getUsuario();
            String estiloComunicacion = usuario.getEstiloLenguajeIa() != null ? usuario.getEstiloLenguajeIa() : "INFORMAL";

            String instruccionEstilo = "INFORMAL".equalsIgnoreCase(estiloComunicacion)
                    ? "Usa un tono INFORMAL, sumamente cercano, empático, tratándolo de 'tú', amigable y juvenil."
                    : "Usa un tono FORMAL, profesional, clínico, respetuoso, tratándolo de 'usted' y manteniendo distancia terapéutica.";

            // SE MANTIENE EL TEXTO EN CLARO PARA EL PROMPT: Asegura que el monitoreo sea real
            String promptCompleto = construirPromptClinico(textoUsuario, instruccionEstilo);

            String respuestaCrudaIA = llamarApiGemini(promptCompleto);
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

        } catch (RuntimeException e) {
            System.err.println("Error crítico de integración con Gemini API: " + e.getMessage());
            throw e;
        }
    }

    private String construirPromptClinico(String textoUsuario, String instruccionEstilo) {
        return "Eres un asistente de inteligencia artificial experto en psicología y soporte " +
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
                "mental de Perú (Línea 113 Opción 5).\n"
                + "5. REGLA DE ADAPTACIÓN DE LENGUAJE (HU-28): " + instruccionEstilo + "\n\n"
                + "Debes responder estrictamente imitando esta estructura exacta separada por barras verticales " +
                "(||), sin usar negritas, saltos de línea ni asteriscos para que mi backend pueda procesarla:\n" +
                "EMOCION: [Emoción] || URGENCIA: [Nivel] || RESPUESTA: [Tu respuesta empática] || " +
                "RECOMENDACIONES: [Recomendación 1; Recomendación 2] || AYUDA: [Línea de ayuda si aplica]";
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

    private String descifrarAES(String textoCifrado) {
        try {
            byte[] datos = Base64.getDecoder().decode(textoCifrado);
            byte[] iv = Arrays.copyOfRange(datos, 0, 16);
            byte[] cifrado = Arrays.copyOfRange(datos, 16, datos.length);

            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            byte[] textoPlano = cipher.doFinal(cifrado);
            return new String(textoPlano);
        } catch (Exception e) {
            System.err.println("Error al descifrar el mensaje. Se devolverá encriptado por seguridad.");
            return textoCifrado; // Fallback
        }
    }

    @Transactional
    public G6_MH_ResumenPostSesionDTO finalizarSesionConResumen(Long idSesion) {
        G6_MH_SesionTerapia sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        if ("FINALIZADA".equals(sesion.getEstado())) {
            throw new RuntimeException("Esta sesión ya fue finalizada previamente.");
        }

        // 1. Recuperar el historial completo y descifrarlo
        List<G6_MH_MensajeChat> historial = mensajeRepository.findBySesionIdSesionOrderByFechaEnvioAsc(idSesion);
        StringBuilder transcripcion = new StringBuilder();

        for (G6_MH_MensajeChat msg : historial) {
            String textoPlano = descifrarAES(msg.getContenido());
            transcripcion.append(msg.getTipoRemitente()).append(": ").append(textoPlano).append("\n");
        }

        // 2. Prompt estricto para Gemini
        String promptAnalisis = "Actúa como un psicólogo clínico experto. Analiza la siguiente transcripción de una sesión de contención emocional. " +
                "Genera un resumen post-sesión personalizado. Devuelve ESTRICTAMENTE un JSON con esta estructura exacta, sin formato markdown, ni comillas invertidas, ni texto extra:\n" +
                "{\n" +
                "  \"insight\": \"Un párrafo breve y empático analizando el progreso y estado emocional del paciente en la sesión.\",\n" +
                "  \"sugerencias\": [\"Práctica de mindfulness específica basada en el chat\", \"Ejercicio práctico o reflexión\", \"Acción de bienestar\"]\n" +
                "}\n\n" +
                "Transcripción de la sesión:\n" + transcripcion;

        String insightGenerado;
        List<String> sugerenciasGeneradas;

        try {
            // 3. Conexión HTTP real a Gemini (Misma estructura que ya usamos para el chat)
            String respuestaGeminiBruta = llamarApiGemini(promptAnalisis);

            // 4. Limpiar y parsear el JSON devuelto por la IA usando Jackson
            String jsonLimpio = respuestaGeminiBruta.replace("```json", "").replace("```", "").trim();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonLimpio);

            insightGenerado = rootNode.get("insight").asText();
            sugerenciasGeneradas = mapper.convertValue(rootNode.get("sugerencias"), new com.fasterxml.jackson.core.type.TypeReference<>(){});

        } catch (Exception e) {
            // 5. Plan de contingencia si Google Gemini se cae o hay timeout
            insightGenerado = "Sesión concluida con éxito. Has dado un paso importante al expresar tus emociones el día de hoy.";
            sugerenciasGeneradas = Arrays.asList(
                    "Toma un vaso de agua y realiza respiraciones profundas.",
                    "Descansa la mente por los próximos 15 minutos."
            );
            System.err.println("Error procesando el resumen con la IA: " + e.getMessage());
        }

        // 6. Cerrar el caso en Base de Datos
        sesion.setEstado("FINALIZADA");
        sesionRepository.save(sesion);

        // 7. Entregar el DTO validado
        return com.upc.mind_health.dtos.G6_MH_ResumenPostSesionDTO.builder()
                .idSesion(sesion.getIdSesion())
                .fechaFinalizacion(LocalDateTime.now())
                .estadoSesion("FINALIZADA")
                .emocionPredominante(sesion.getUltimaEmocionDetectada() != null ? sesion.getUltimaEmocionDetectada() : "No evaluada")
                .insightClinicoIA(insightGenerado)
                .sugerenciasPracticas(sugerenciasGeneradas)
                .build();
    }

    private void ejecutarAsignacionAutomatica(G6_MH_SesionTerapia sesion, String emocion) {
        List<G6_MH_Psicologo> disponibles = psicologoRepository.findByDisponibleTrue();
        G6_MH_Psicologo profesionalAsignado;
        boolean marcarComoOcupado;

        if (!disponibles.isEmpty()) {
            profesionalAsignado = disponibles.get(0);
            marcarComoOcupado = true;
        } else {
            // Una alerta CRÍTICA (posible riesgo de vida) nunca debe perderse por falta de
            // profesionales "disponibles": si todos están ocupados, se reparte igual al que
            // tenga menos casos pendientes en vez de descartar el caso silenciosamente.
            List<G6_MH_Psicologo> todos = psicologoRepository.findAll();
            if (todos.isEmpty()) {
                System.err.println("No hay profesionales registrados para atender un caso CRÍTICO.");
                return;
            }
            profesionalAsignado = todos.stream()
                    .min(java.util.Comparator.comparingLong(p ->
                            derivacionRepository.countByProfesionalIdPsicologoAndEstadoAtencion(p.getIdPsicologo(), "PENDIENTE")))
                    .orElse(todos.get(0));
            marcarComoOcupado = false;
        }

        G6_MH_Derivacion nuevaDerivacion = G6_MH_Derivacion.builder()
                .motivo("Derivación Inmediata por Alerta IA: " + emocion)
                .fecha(java.time.LocalDate.now())
                .sesion(sesion)
                .profesional(profesionalAsignado)
                .build();

        derivacionRepository.save(nuevaDerivacion);

        if (marcarComoOcupado) {
            profesionalAsignado.setDisponible(false);
            psicologoRepository.save(profesionalAsignado);
        }

        //ESCENARIO 1: Notificación Inteligente Push/Email
        dispararNotificacionInteligente(nuevaDerivacion, profesionalAsignado.getUsuario().getCorreo());

        System.out.println("Caso derivado al profesional ID: " + profesionalAsignado.getIdPsicologo());
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
        emailService.enviarCorreo(
                correoDestino,
                "Alerta de crisis emocional — Mind Health",
                "Se te ha asignado un caso crítico (Sesión #" + derivacion.getSesion().getIdSesion() + ").\n"
                        + "Motivo: " + derivacion.getMotivo() + "\n\n"
                        + "Ingresa a tu panel de profesional para revisar el detalle y atender el caso."
        );
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

        return G6_MH_AlertaNotificacionDTO.builder()
                .idDerivacion(derivacion.getIdDerivacion())
                .correoProfesional(correoPsicologo)
                .mensajePush("Caso cerrado con éxito. El profesional vuelve a estar disponible en el sistema.")
                .severidad("RESOLVIDO")
                .fechaAlerta(LocalDate.now())
                .build();
    }

    @Transactional(readOnly = true)
    public List<G6_MH_HistorialSeguroResponseDTO> obtenerHistorialSesionesSegurasReal(Long idUsuario) {
        return sesionRepository.findAll().stream()
                .filter(sesion -> sesion.getUsuario().getIdUsuario().equals(idUsuario))
                .map(sesion -> G6_MH_HistorialSeguroResponseDTO.builder()
                        .idSesion(sesion.getIdSesion())
                        .fechaInicio(sesion.getFechaInicio())
                        .estado(sesion.getEstado())
                        .ultimaEmocion(sesion.getUltimaEmocionDetectada())
                        // Confirmación de cifrado explícita exigida por el criterio de aceptación
                        .confirmacionSeguridad("Sus datos emocionales compartidos en esta sesión están cifrados con el algoritmo AES y protegidos contra terceros de forma automática.")
                        .build())
                .collect(Collectors.toList());
    }

    //HU-13 ESCENARIO 1: Crear una solicitud de coordinación inter-psicólogos
    @Transactional
    public G6_MH_CoordinacionResponseDTO solicitarCoordinacionCaso(String correoEmisor, G6_MH_CoordinacionRequestDTO requestDTO) {
        G6_MH_Derivacion derivacion = derivacionRepository.findById(requestDTO.getIdDerivacion())
                .orElseThrow(() -> new RuntimeException("Caso de derivación no encontrado"));

        G6_MH_Psicologo emisor = psicologoRepository.findByUsuarioCorreo(correoEmisor)
                .orElseThrow(() -> new RuntimeException("No se encontró tu perfil de profesional emisor"));

        G6_MH_Psicologo receptor = psicologoRepository.findByUsuarioCorreo(requestDTO.getCorreoColegaInvitado())
                .orElseThrow(() -> new RuntimeException("El correo ingresado no pertenece a ningún psicólogo de la plataforma"));

        G6_MH_Colaboracion colaboracion = G6_MH_Colaboracion.builder()
                .derivacion(derivacion)
                .emisor(emisor)
                .receptor(receptor)
                .estadoSolicitud("PENDIENTE")
                .fechaSolicitud(LocalDateTime.now())
                .build();

        colaboracionRepository.save(colaboracion);

        emailService.enviarCorreo(
                receptor.getUsuario().getCorreo(),
                "Solicitud de coordinación de caso — Mind Health",
                "El Dr(a). " + emisor.getNombre() + " te ha invitado a coordinar en un caso complejo.\n"
                        + "Ingresa a tu panel de profesional, sección Coordinación, para aceptar o rechazar la solicitud."
        );

        return G6_MH_CoordinacionResponseDTO.builder()
                .idColaboracion(colaboracion.getIdColaboracion())
                .nombreEmisor(emisor.getNombre())
                .nombreReceptor(receptor.getNombre())
                .estadoActual("PENDIENTE")
                .mensajeNotificacion("📨 Solicitud enviada con éxito. Se ha notificado al Dr(a). " + receptor.getNombre())
                .fechaCambio(LocalDateTime.now())
                .build();
    }

    // HU-13 ESCENARIO 2 Y 3: Responder (Aceptar o Rechazar) la colaboración
    @Transactional
    public G6_MH_CoordinacionResponseDTO gestionarRespuestaColaboracion(Long idColaboracion, String correoReceptor, boolean acepta, String notasIniciales) {
        G6_MH_Colaboracion colaboracion = colaboracionRepository.findById(idColaboracion)
                .orElseThrow(() -> new RuntimeException("Registro de colaboración no encontrado"));

        if (!colaboracion.getReceptor().getUsuario().getCorreo().equalsIgnoreCase(correoReceptor)) {
            throw new RuntimeException("No tienes permiso para responder a esta invitación.");
        }

        if (acepta) {
            colaboracion.setEstadoSolicitud("ACEPTADA");
            colaboracion.setObservacionesCompartidas(notasIniciales != null ? notasIniciales : "Espacio de trabajo conjunto habilitado.");
        } else {
            colaboracion.setEstadoSolicitud("RECHAZADA");
        }
        colaboracionRepository.save(colaboracion);

        String msg = acepta
                ? "Colaboración aceptada. Espacio compartido habilitado para el caso."
                : "Colaboración rechazada. Mensaje enviado al emisor.";

        emailService.enviarCorreo(
                colaboracion.getEmisor().getUsuario().getCorreo(),
                acepta ? "Colaboración aceptada — Mind Health" : "Colaboración rechazada — Mind Health",
                "El Dr(a). " + colaboracion.getReceptor().getNombre() + " ha "
                        + (acepta ? "aceptado" : "rechazado") + " tu solicitud de coordinación de caso."
        );

        return G6_MH_CoordinacionResponseDTO.builder()
                .idColaboracion(colaboracion.getIdColaboracion())
                .nombreEmisor(colaboracion.getEmisor().getNombre())
                .nombreReceptor(colaboracion.getReceptor().getNombre())
                .estadoActual(colaboracion.getEstadoSolicitud())
                .notasDeCasoCompartidas(colaboracion.getObservacionesCompartidas())
                .mensajeNotificacion(msg)
                .fechaCambio(LocalDateTime.now())
                .build();
    }

    // HU-13: Bandeja de solicitudes de coordinación pendientes de responder
    @Transactional(readOnly = true)
    public List<G6_MH_CoordinacionResponseDTO> listarColaboracionesPendientes(String correoReceptor) {
        return colaboracionRepository.findByReceptorUsuarioCorreo(correoReceptor).stream()
                .filter(colaboracion -> "PENDIENTE".equals(colaboracion.getEstadoSolicitud()))
                .map(colaboracion -> G6_MH_CoordinacionResponseDTO.builder()
                        .idColaboracion(colaboracion.getIdColaboracion())
                        .nombreEmisor(colaboracion.getEmisor().getNombre())
                        .nombreReceptor(colaboracion.getReceptor().getNombre())
                        .estadoActual(colaboracion.getEstadoSolicitud())
                        .notasDeCasoCompartidas(colaboracion.getObservacionesCompartidas())
                        .fechaCambio(colaboracion.getFechaSolicitud())
                        .build())
                .collect(Collectors.toList());
    }

    // HU-13 ESCENARIO 2: Espacio de coordinación compartido — casos aceptados donde el profesional participa
    @Transactional(readOnly = true)
    public List<G6_MH_CoordinacionResponseDTO> listarColaboracionesAceptadas(String correoProfesional) {
        return colaboracionRepository
                .findByEstadoSolicitudAndEmisorUsuarioCorreoOrEstadoSolicitudAndReceptorUsuarioCorreo(
                        "ACEPTADA", correoProfesional, "ACEPTADA", correoProfesional)
                .stream()
                .map(colaboracion -> G6_MH_CoordinacionResponseDTO.builder()
                        .idColaboracion(colaboracion.getIdColaboracion())
                        .nombreEmisor(colaboracion.getEmisor().getNombre())
                        .nombreReceptor(colaboracion.getReceptor().getNombre())
                        .estadoActual(colaboracion.getEstadoSolicitud())
                        .notasDeCasoCompartidas(colaboracion.getObservacionesCompartidas())
                        .fechaCambio(colaboracion.getFechaSolicitud())
                        .build())
                .collect(Collectors.toList());
    }

    // HU-13 ESCENARIO 2: Agregar una observación nueva al espacio compartido (se concatena, no se sobreescribe)
    @Transactional
    public G6_MH_CoordinacionResponseDTO agregarObservacionColaboracion(Long idColaboracion, String correoAutor, String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new RuntimeException("La observación no puede estar vacía.");
        }

        G6_MH_Colaboracion colaboracion = colaboracionRepository.findById(idColaboracion)
                .orElseThrow(() -> new RuntimeException("Registro de colaboración no encontrado"));

        if (!"ACEPTADA".equals(colaboracion.getEstadoSolicitud())) {
            throw new RuntimeException("Solo se pueden agregar observaciones a colaboraciones aceptadas.");
        }

        boolean esEmisor = colaboracion.getEmisor().getUsuario().getCorreo().equalsIgnoreCase(correoAutor);
        boolean esReceptor = colaboracion.getReceptor().getUsuario().getCorreo().equalsIgnoreCase(correoAutor);
        if (!esEmisor && !esReceptor) {
            throw new RuntimeException("No tienes permiso para agregar observaciones a este caso.");
        }

        String nombreAutor = esEmisor ? colaboracion.getEmisor().getNombre() : colaboracion.getReceptor().getNombre();
        String correoDestino = esEmisor
                ? colaboracion.getReceptor().getUsuario().getCorreo()
                : colaboracion.getEmisor().getUsuario().getCorreo();

        String entradaNueva = "[" + LocalDateTime.now() + "] " + nombreAutor + ": " + texto.trim() + "\n---\n";
        String historialActual = colaboracion.getObservacionesCompartidas();
        colaboracion.setObservacionesCompartidas(
                historialActual == null || historialActual.isBlank() ? entradaNueva : historialActual + entradaNueva);
        colaboracionRepository.save(colaboracion);

        emailService.enviarCorreo(
                correoDestino,
                "Nueva observación en caso compartido — Mind Health",
                "El Dr(a). " + nombreAutor + " agregó una nueva observación al caso que coordinan:\n\n" + texto.trim()
                        + "\n\nIngresa a tu panel de profesional, sección Coordinación, para ver el historial completo."
        );

        return G6_MH_CoordinacionResponseDTO.builder()
                .idColaboracion(colaboracion.getIdColaboracion())
                .nombreEmisor(colaboracion.getEmisor().getNombre())
                .nombreReceptor(colaboracion.getReceptor().getNombre())
                .estadoActual(colaboracion.getEstadoSolicitud())
                .notasDeCasoCompartidas(colaboracion.getObservacionesCompartidas())
                .mensajeNotificacion("Observación agregada al espacio compartido.")
                .fechaCambio(LocalDateTime.now())
                .build();
    }

    // 🌟 HU-38 ESCENARIO 1 y 2: Eliminación física o lógica de una sesión individual
    @Transactional
    public String eliminarSesionHistorial(Long idSesion) {
        G6_MH_SesionTerapia sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new RuntimeException("La sesión que intenta eliminar no existe."));

        // Primero eliminamos los mensajes asociados a la sesión para evitar fallos de llaves foráneas
        mensajeRepository.deleteBySesionIdSesion(idSesion);

        // Eliminamos la sesión de manera definitiva
        sesionRepository.delete(sesion);

        // Retornamos el mensaje exigido por el Escenario 2
        return "La sesión ha sido eliminada del historial correctamente.";
    }

    private String llamarApiGemini(String prompt) {
        String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> partsWrapper = new HashMap<>();
        partsWrapper.put("parts", Collections.singletonList(textPart));
        requestBody.put("contents", Collections.singletonList(partsWrapper));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            org.springframework.http.ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    geminiApiUrl + apiKey,
                    org.springframework.http.HttpMethod.POST,
                    entity,
                    new org.springframework.core.ParameterizedTypeReference<>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            return obtenerTextoDeRespuestaGoogle(responseBody);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.err.println("Error de la API de Gemini [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 503) {
                throw new RuntimeException(
                        "Mind está recibiendo muchas conversaciones en este momento y no puede responder. " +
                        "Espera unos minutos y vuelve a intentarlo; si necesitas ayuda ahora mismo, comunícate a la Línea 113 Opción 5.");
            }
            throw new RuntimeException(
                    "Mind no está disponible en este momento. Por favor, inténtalo de nuevo en unos minutos.");
        } catch (org.springframework.web.client.RestClientException e) {
            System.err.println("Error de conexión con la API de Gemini: " + e.getMessage());
            throw new RuntimeException(
                    "No pudimos conectar con Mind en este momento. Revisa tu conexión e inténtalo nuevamente.");
        }
    }
}