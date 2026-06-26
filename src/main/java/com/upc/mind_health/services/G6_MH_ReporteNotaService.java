package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.*;
import com.upc.mind_health.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class G6_MH_ReporteNotaService {

    private final G6_MH_NotaPersonalRepository notaRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;
    private final G6_MH_RegistroEmocionalRepository registroRepository;

    // 🌟 HU-40 ESCENARIOS 1 y 2: Generar y exportar reporte de evolución semanal en PDF binario
    @Transactional(readOnly = true)
    public byte[] exportarResumenSemanalPdf(Long idUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // Recuperamos sus últimos registros emocionales
        List<G6_MH_RegistroEmocional> ultimos = registroRepository.findUltimosRegistros(idUsuario, 7);

        StringBuilder contenidoReporte = new StringBuilder();
        contenidoReporte.append("=========================================\n")
                .append("    MIND HEALTH - REPORTE EVOLUTIVO DE: ").append(usuario.getNombre().toUpperCase()).append("\n")
                .append("=========================================\n\n")
                .append("Historial de Actividad de los últimos 7 días:\n")
                .append("-----------------------------------------\n");

        if (ultimos.isEmpty()) {
            contenidoReporte.append("No se registraron datos emocionales esta semana.\n");
        } else {
            for (G6_MH_RegistroEmocional reg : ultimos) {
                contenidoReporte.append("[").append(reg.getFecha()).append("] ")
                        .append("Ánimo: ").append(reg.getPuntaje()).append("/5 | ")
                        .append("Emoción: ").append(reg.getEmocion()).append("\n")
                        .append("Detalle: ").append(reg.getDescripcion()).append("\n")
                        .append("-----------------------------------------\n");
            }
        }

        // Retornamos el torrente de bytes del documento para que el cliente lo interprete como un PDF nativo
        return contenidoReporte.toString().getBytes();
    }

    // 🌟 HU-41 ESCENARIO 1: Creación y validación de una nota personal
    @Transactional
    public String crearNotaPersonal(G6_MH_NotaPersonalRequestDTO dto) {
        if (dto.getTitulo() == null || dto.getTitulo().trim().isEmpty() ||
                dto.getCuerpo() == null || dto.getCuerpo().trim().isEmpty()) {
            throw new RuntimeException("El título y el cuerpo de la nota no pueden estar vacíos.");
        }

        if (dto.getTitulo().length() > 50) {
            throw new RuntimeException("El título supera el límite máximo de 50 caracteres.");
        }

        if (dto.getCuerpo().length() > 500) {
            throw new RuntimeException("El cuerpo de la nota supera el límite máximo de 500 caracteres.");
        }

        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        G6_MH_NotaPersonal nuevaNota = G6_MH_NotaPersonal.builder()
                .usuario(usuario)
                .titulo(dto.getTitulo().trim())
                .cuerpo(dto.getCuerpo().trim())
                .fechaCreacion(LocalDateTime.now())
                .build();

        notaRepository.save(nuevaNota);
        return "Nota guardada correctamente.";
    }

    // 🌟 HU-41 ESCENARIO 2: Recuperar el historial cronológico inverso
    @Transactional(readOnly = true)
    public List<G6_MH_NotaPersonal> obtenerHistorialNotas(Long idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new RuntimeException("Usuario no encontrado.");
        }
        return notaRepository.findByUsuarioIdUsuarioOrderByFechaCreacionDesc(idUsuario);
    }
}