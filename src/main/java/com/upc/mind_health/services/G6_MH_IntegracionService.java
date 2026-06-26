package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.*;
import com.upc.mind_health.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class G6_MH_IntegracionService {

    private final G6_MH_UsuarioRepository usuarioRepository;

    // 🌟 HU-32 ESCENARIO 1: Vincular servicio externo de calendario
    @Transactional
    public String vincularCalendario(Long idUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        usuario.setCalendarioVinculado(true);
        usuarioRepository.save(usuario);
        return "La integración con Google Calendar se realizó correctamente de manera segura.";
    }

    // 🌟 HU-32 ESCENARIO 2: Sincronizar automáticamente un evento programado
    @Transactional(readOnly = true)
    public String agendarEventoEnCalendario(G6_MH_ActividadCalendarioDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (!usuario.isCalendarioVinculado()) {
            return "Actividad guardada localmente, pero no se pudo sincronizar porque no has vinculado tu calendario.";
        }

        // Simulación del log de integración externa con la API de Google Calendar
        System.out.println("[API EXTERNA] Sincronizando con Google Calendar para: " + usuario.getCorreo());
        System.out.println("[EVENTO CREADO] '" + dto.getNombreActividad() + "' agendado para el " + dto.getFechaHora());

        return "La actividad ha sido guardada en tu perfil y el evento '" + dto.getNombreActividad()
                + "' se creó automáticamente en tu calendario vinculado.";
    }

    // 🌟 HU-33 ESCENARIO 1: Autorizar un contacto de confianza para la red de apoyo
    @Transactional
    public String autorizarContactoApoyo(G6_MH_RedApoyoRequestDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        usuario.setCorreoRedApoyo(dto.getCorreoContacto());
        usuario.setNotificarRedApoyo(true);
        usuarioRepository.save(usuario);

        return "El contacto de confianza (" + dto.getCorreoContacto() + ") fue autorizado correctamente en tu Red de Apoyo.";
    }

    // 🌟 HU-33 ESCENARIO 2 (y HU-34 Escenario 2): Disparador del reporte automático semanal
    @Transactional(readOnly = true)
    public String enviarReporteSemanalRedApoyo(Long idUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (usuario.getCorreoRedApoyo() == null || usuario.getCorreoRedApoyo().isEmpty()) {
            throw new RuntimeException("No has autorizado ningún contacto en tu red de apoyo para recibir reportes.");
        }

        // Simulación de armado y envío de correo SMTP saliente
        System.out.println("[SMTP MAIL] Enviando reporte a: " + usuario.getCorreoRedApoyo());
        System.out.println("[CONTENIDO] Hola, compartimos el avance emocional de " + usuario.getNombre() + " de los últimos 7 días...");

        return "El sistema envió automáticamente el reporte semanal al correo electrónico del contacto autorizado ("
                + usuario.getCorreoRedApoyo() + ") con éxito.";
    }
}