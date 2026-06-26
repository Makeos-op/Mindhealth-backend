package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.*;
import com.upc.mind_health.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class G6_MH_RutinaRecordatorioService {

    private final G6_MH_RutinaPreventivaRepository rutinaRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;
    private final G6_MH_RegistroEmocionalRepository registroRepository;

    // 🌟 HU-30 ESCENARIOS 1, 2 y 3: Motor algorítmico de generación de rutinas adaptativas
    @Transactional
    public G6_MH_RutinaPreventiva generarRutinaDiaria(Long idUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Evaluamos el historial reciente con tu consulta nativa (límite 3)
        List<G6_MH_RegistroEmocional> ultimos = registroRepository.findUltimosRegistros(idUsuario, 3);

        double promedioPuntaje = ultimos.stream()
                .mapToInt(G6_MH_RegistroEmocional::getPuntaje)
                .average()
                .orElse(3.0);

        String actividadesSugeridas = "Rutina Estándar: 10 min de respiración diafragmática por la mañana y lectura reflexiva nocturna.";
        boolean esRiesgoRecaida = false;

        // Criterio de Escenario 3: Si el promedio de bienestar cae por debajo de 2.5, se activa la alerta preventiva de crisis
        if (promedioPuntaje <= 2.5) {
            actividadesSugeridas = "PLAN PREVENTIVO ACTIVADO: Ejercicios intensivos de autorregulación, meditación guiada de emergencia (15 min) y recordatorio de contacto directo con tu red de soporte asignada.";
            esRiesgoRecaida = true;
        } else if (promedioPuntaje >= 4.0) {
            actividadesSugeridas = "Rutina de Consolidación: Afirmaciones positivas matutinas y diario de gratitud al finalizar el día.";
        }

        G6_MH_RutinaPreventiva nuevaRutina = G6_MH_RutinaPreventiva.builder()
                .usuario(usuario)
                .actividades(actividadesSugeridas)
                .fechaGeneracion(LocalDate.now())
                .esPreventivaRecaida(esRiesgoRecaida)
                .build();

        return rutinaRepository.save(nuevaRutina);
    }

    // 🌟 HU-30 ESCENARIO 5: Registrar la retroalimentación del usuario sobre la utilidad
    @Transactional
    public String evaluarUtilidadRutina(G6_MH_FeedbackRutinaDTO dto) {
        G6_MH_RutinaPreventiva rutina = rutinaRepository.findById(dto.getIdRutina())
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada."));

        rutina.setUtilidadEvaluada(dto.getUtilidad().toUpperCase());
        rutinaRepository.save(rutina);
        return "Gracias por tu retroalimentación. El sistema optimizará tus próximos planes de prevención.";
    }

    // 🌟 HU-31 ESCENARIO 1: Configuración flexible de rangos de recordatorios
    @Transactional
    public String actualizarHorarioPreferido(G6_MH_HorarioRecordatorioDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setHoraInicioRecordatorio(dto.getHoraInicio());
        usuario.setHoraFinRecordatorio(dto.getHoraFin());
        usuarioRepository.save(usuario);

        return "Horarios de preferencia actualizados con éxito. Los recordatorios automáticos se han adaptado a tu rango de comodidad.";
    }

    // 🌟 HU-30 ESCENARIO 4: Listar historial para la vista "Rutinas diarias"
    @Transactional(readOnly = true)
    public List<G6_MH_RutinaPreventiva> obtenerHistorialRutinas(Long idUsuario) {
        return rutinaRepository.findByUsuarioIdUsuarioOrderByFechaGeneracionDesc(idUsuario);
    }
}