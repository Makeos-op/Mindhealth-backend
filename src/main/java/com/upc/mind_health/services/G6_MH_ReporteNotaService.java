package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.*;
import com.upc.mind_health.repositories.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

        List<String> lineas = new ArrayList<>();
        lineas.add("MIND HEALTH - REPORTE EVOLUTIVO DE: " + usuario.getNombre().toUpperCase());
        lineas.add("Historial de actividad de los últimos 7 días:");
        lineas.add("");

        if (ultimos.isEmpty()) {
            lineas.add("No se registraron datos emocionales esta semana.");
        } else {
            for (G6_MH_RegistroEmocional reg : ultimos) {
                lineas.add("[" + reg.getFecha() + "] Ánimo: " + reg.getPuntaje() + "/5 | Emoción: " + reg.getEmocion());
                lineas.add("Detalle: " + reg.getDescripcion());
                lineas.add("");
            }
        }

        return generarPdf(lineas);
    }

    private byte[] generarPdf(List<String> lineas) {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage();
            documento.addPage(pagina);

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                PDType1Font fuente = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                float y = 720;
                contenido.beginText();
                contenido.setFont(fuente, 11);
                contenido.newLineAtOffset(50, y);
                for (String linea : lineas) {
                    contenido.showText(linea);
                    contenido.newLineAtOffset(0, -16);
                }
                contenido.endText();
            }

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            documento.save(salida);
            return salida.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el PDF", e);
        }
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