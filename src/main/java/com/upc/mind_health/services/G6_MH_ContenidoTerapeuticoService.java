package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.*;
import com.upc.mind_health.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class G6_MH_ContenidoTerapeuticoService {

    private final G6_MH_ContenidoTerapeuticoRepository contenidoRepository;
    private final G6_MH_CalificacionContenidoRepository calificacionRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;
    private final G6_MH_SesionTerapiaRepository sesionRepository;

    // 🌟 HU-22 ESCENARIOS 1, 2 y 3: Recomendar contenido dinámicamente basado en el estado emocional
    @Transactional(readOnly = true)
    public List<G6_MH_ContenidoTerapeutico> obtenerRecomendacionesPorSesion(Long idSesion) {
        G6_MH_SesionTerapia sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new RuntimeException("Sesión clínica no encontrada."));

        String emocionActual = sesion.getUltimaEmocionDetectada();
        if (emocionActual == null || emocionActual.isEmpty()) {
            emocionActual = "Calma"; // Fallback general para el menú inicio (Escenario 2)
        }

        // Retorna la lista de videos, artículos o cuestionarios mapeados a esa emoción por la IA
        return contenidoRepository.findByEmocionAsociadaContainingIgnoreCase(emocionActual);
    }

    // 🌟 HU-23 ESCENARIO 1: Calificar contenido y dejar comentarios
    @Transactional
    public String calificarRecurso(G6_MH_CalificacionRequestDTO dto) {
        if (dto.getEstrellas() < 1 || dto.getEstrellas() > 5) {
            throw new RuntimeException("La calificación debe estar estrictamente en el rango de 1 a 5 estrellas.");
        }

        G6_MH_ContenidoTerapeutico contenido = contenidoRepository.findById(dto.getIdContenido())
                .orElseThrow(() -> new RuntimeException("Contenido terapéutico no encontrado."));

        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        G6_MH_CalificacionContenido calificacion = G6_MH_CalificacionContenido.builder()
                .contenido(contenido)
                .usuario(usuario)
                .estrellas(dto.getEstrellas())
                .comentario(dto.getComentario())
                .fechaRegistro(LocalDateTime.now())
                .build();

        calificacionRepository.save(calificacion);
        return "Calificación y comentario registrados con éxito.";
    }

    // 🌟 HU-23 ESCENARIO 2: Obtener detalle del contenido junto a los comentarios de otros usuarios
    @Transactional(readOnly = true)
    public G6_MH_ContenidoDetalleDTO obtenerDetalleContenidoConComentarios(Long idContenido) {
        G6_MH_ContenidoTerapeutico contenido = contenidoRepository.findById(idContenido)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado."));

        List<G6_MH_CalificacionResponseDTO> feedbackUsuarios = calificacionRepository
                .findByContenidoIdContenidoOrderByFechaRegistroDesc(idContenido).stream()
                .map(cal -> G6_MH_CalificacionResponseDTO.builder()
                        .idCalificacion(cal.getIdCalificacion())
                        .nombreUsuario(cal.getUsuario().getNombre())
                        .estrellas(cal.getEstrellas())
                        .comentario(cal.getComentario())
                        .fecha(cal.getFechaRegistro())
                        .build())
                .collect(Collectors.toList());

        return G6_MH_ContenidoDetalleDTO.builder()
                .idContenido(contenido.getIdContenido())
                .titulo(contenido.getTitulo())
                .tipo(contenido.getTipo())
                .urlRecurso(contenido.getUrlRecurso())
                .emocionAsociada(contenido.getEmocionAsociada())
                .comentariosYCalificaciones(feedbackUsuarios)
                .build();
    }

    // 🌟 HU-29 ESCENARIO 1: Filtrar recursos según los objetivos personales guardados en el perfil
    @Transactional(readOnly = true)
    public List<G6_MH_ContenidoTerapeutico> obtenerContenidoPorObjetivosUsuario(Long idUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        String metas = usuario.getObjetivosPersonales();
        if (metas == null || metas.isEmpty()) {
            return contenidoRepository.findAll(); // Si no tiene metas, muestra catálogo general
        }

        // Tomamos el primer objetivo de su lista para realizar el filtro en la base de datos
        String objetivoPrincipal = metas.split(",")[0].trim();
        return contenidoRepository.findByEmocionAsociadaContainingIgnoreCase(objetivoPrincipal);
    }

    // 🌟 HU-39 ESCENARIO 2: Ajuste y priorización de recomendaciones según contenidos mejor valorados
    @Transactional(readOnly = true)
    public List<G6_MH_ContenidoTerapeutico> obtenerRecomendacionesPriorizadas(Long idUsuario, String emocionActual) {
        // 1. Obtener catálogo base según la emoción detectada
        List<G6_MH_ContenidoTerapeutico> catalogoBase = contenidoRepository.findByEmocionAsociadaContainingIgnoreCase(emocionActual);

        // 2. Buscar si el usuario tiene calificaciones previas altas (4 o 5 estrellas)
        List<G6_MH_CalificacionContenido> altasCalificaciones = calificacionRepository.findByUsuarioIdUsuarioOrderByFechaRegistroDesc(idUsuario)
                .stream()
                .filter(c -> c.getEstrellas() >= 4)
                .collect(Collectors.toList());

        if (!altasCalificaciones.isEmpty()) {
            // Extraemos el tipo de recurso que le gusta (Ej: "VIDEO")
            String tipoPreferido = altasCalificaciones.get(0).getContenido().getTipo();

            // Ordenamos la lista colocando primero los contenidos que coincidan con su formato favorito
            catalogoBase.sort((c1, c2) -> {
                if (c1.getTipo().equals(tipoPreferido) && !c2.getTipo().equals(tipoPreferido)) return -1;
                if (!c1.getTipo().equals(tipoPreferido) && c2.getTipo().equals(tipoPreferido)) return 1;
                return 0;
            });
        }

        return catalogoBase;
    }
}