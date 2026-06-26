package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.*;
import com.upc.mind_health.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class G6_MH_PreferenciaTerapiaService {

    private final G6_MH_UsuarioRepository usuarioRepository;
    private final G6_MH_RegistroEmocionalRepository registroEmocionalRepository;

    // 🌟 HU-25 ESCENARIO 1 y 2: Modificar método terapéutico posterior al registro
    @Transactional
    public String actualizarMetodoTerapia(G6_MH_PreferenciaTerapiaRequestDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setMetodoTerapiaPreferido(dto.getNuevoMetodo());
        usuarioRepository.save(usuario);

        // Retornamos la confirmación exigida por el Escenario 2
        return "Preferencia actualizada con éxito. La IA ajustará automáticamente tus próximas sesiones y recomendaciones al enfoque: "
                + (dto.getNuevoMetodo() != null ? dto.getNuevoMetodo() : "Sugerido automáticamente por IA");
    }

    // 🌟 HU-26 ESCENARIOS 1 y 2: Sugerencia de métodos basada en el progreso emocional histórico
    @Transactional(readOnly = true)
    public G6_MH_SugerenciaTerapiaResponseDTO evaluarYSugerirMetodo(Long idUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String metodoActual = usuario.getMetodoTerapiaPreferido() != null ? usuario.getMetodoTerapiaPreferido() : "Ninguno seleccionado";

        // Leemos los últimos registros usando tu query nativa con límite 5 para ver la tendencia de ánimo
        List<G6_MH_RegistroEmocional> ultimosRegistros = registroEmocionalRepository.findUltimosRegistros(idUsuario, 5);

        String metodoSugerido = "Terapia Cognitivo Conductual (TCC)";
        String sustentacion = "Detectamos picos de vulnerabilidad o inestabilidad en tus últimas autoevaluaciones. El enfoque estructurado de la TCC te ayudará a gestionar pensamientos críticos.";

        if (!ultimosRegistros.isEmpty()) {
            double promedioPuntaje = ultimosRegistros.stream()
                    .mapToInt(G6_MH_RegistroEmocional::getPuntaje)
                    .average()
                    .orElse(3.0);

            // Si el promedio es alto (>= 4.0), el usuario muestra señales claras de mejora constante
            if (promedioPuntaje >= 4.0) {
                metodoSugerido = "Mindfulness y Meditación Guiada";
                sustentacion = "Tu progreso emocional muestra una tendencia altamente positiva y estable. La IA sugiere transicionar hacia Mindfulness para consolidar tu estado de paz y autorregulación.";
            }
        }

        // Si ya está usando el método sugerido, le ofrecemos una alternativa complementaria (Escenario 2)
        if (metodoActual.equalsIgnoreCase(metodoSugerido) && "Mindfulness y Meditación Guiada".equals(metodoSugerido)) {
            metodoSugerido = "Psicología Positiva Integrada";
            sustentacion = "Mantienes un excelente estado de ánimo actual. Te recomendamos explorar este nuevo enfoque para potenciar tus fortalezas diarias.";
        }

        return G6_MH_SugerenciaTerapiaResponseDTO.builder()
                .metodoActual(metodoActual)
                .metodoSugerido(metodoSugerido)
                .sustentacionIA(sustentacion)
                .mensajeConfirmacion("Recomendación personalizada generada según tu bitácora de progreso.")
                .build();
    }

    // 🌟 HU-28 ESCENARIO 1 y 2: Guardar y cambiar dinámicamente el estilo de comunicación de la IA
    @Transactional
    public String guardarPreferenciaLenguaje(com.upc.mind_health.dtos.G6_MH_AjusteLenguajeRequestDTO dto) {
        if (!"FORMAL".equalsIgnoreCase(dto.getEstiloSeleccionado()) && !"INFORMAL".equalsIgnoreCase(dto.getEstiloSeleccionado())) {
            throw new RuntimeException("Estilo de lenguaje no válido. Seleccione FORMAL o INFORMAL.");
        }

        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        usuario.setEstiloLenguajeIa(dto.getEstiloSeleccionado().toUpperCase());
        usuarioRepository.save(usuario);

        // Mensaje requerido por el Escenario 2 para la notificación emergente del Frontend
        return "Preferencia de lenguaje guardada exitosamente. La IA adaptará su tono a modo "
                + usuario.getEstiloLenguajeIa() + " de forma automática a partir de la siguiente sesión.";
    }

    // 🌟 HU-29 ESCENARIO 2: Actualización de metas y reajuste automático de contenido
    @Transactional
    public String actualizarObjetivosPersonales(com.upc.mind_health.dtos.G6_MH_ObjetivosRequestDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        usuario.setObjetivosPersonales(dto.getNuevosObjetivos());
        usuarioRepository.save(usuario);

        // Confirmación exigida por el Escenario 2
        return "Tus objetivos personales han sido actualizados con éxito. El contenido sugerido en tu pantalla de inicio ha sido reajustado a tus nuevas metas.";
    }
}