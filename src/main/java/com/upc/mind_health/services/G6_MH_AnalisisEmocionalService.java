package com.upc.mind_health.services;

import com.upc.mind_health.dtos.G6_MH_ProgresoEmocionalDTO;
import com.upc.mind_health.dtos.G6_MH_AlertaResponseDTO;
import com.upc.mind_health.entities.G6_MH_AlertaEmocional;
import com.upc.mind_health.entities.G6_MH_RegistroEmocional;
import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_AlertaEmocionalRepository;
import com.upc.mind_health.repositories.G6_MH_RegistroEmocionalRepository;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class G6_MH_AnalisisEmocionalService {

    private final G6_MH_RegistroEmocionalRepository registroRepository;
    private final G6_MH_AlertaEmocionalRepository alertaRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;

    // ESCENARIO 1: Evolución para componentes gráficos
    @Transactional(readOnly = true)
    public List<G6_MH_ProgresoEmocionalDTO> obtenerEvolucionEmocional(Long idUsuario) {
        return registroRepository.findByUsuarioIdUsuarioOrderByFechaAsc(idUsuario).stream()
                .map(reg -> G6_MH_ProgresoEmocionalDTO.builder()
                        .fecha(reg.getFecha())
                        .emocionRegistrada(reg.getEmocion())
                        .descripcion(reg.getDescripcion())
                        .puntajeAnimo(reg.getPuntaje())
                        .build())
                .collect(Collectors.toList());
    }

    // ESCENARIOS 2, 3, 4 y 5: Motor Predictivo Inteligente
    @Transactional
    public G6_MH_AlertaResponseDTO evaluarPatronesYGenerarAlerta(Long idUsuario) {
        List<G6_MH_RegistroEmocional> ultimos = registroRepository.findUltimosRegistros(idUsuario, 4);

        if (ultimos.isEmpty()) {
            return null;
        }

        // ESCENARIO 4: Detección de patrones negativos recurrentes (4 días consecutivos con puntaje <= 2)
        long diasConsecutivosMal = ultimos.stream().filter(r -> r.getPuntaje() <= 2).count();

        if (ultimos.size() == 4 && diasConsecutivosMal == 4) {
            G6_MH_AlertaResponseDTO alertaCritica = G6_MH_AlertaResponseDTO.builder()
                    .tipo("CRITICA_RECURRENTE")
                    .mensaje("Alerta de Bienestar: Hemos detectado un patrón prolongado de malestar emocional en tus últimos 4 registros.")
                    .recomendaciones(Arrays.asList(
                            "Te sugerimos encarecidamente agendar una cita prioritaria con uno de nuestros psicólogos colegiados.",
                            "Utiliza nuestro chat MindBot de soporte inmediato si necesitas desahogarte en este momento.",
                            "Te recomendamos realizar la guía de ejercicios de respiración diafragmática 4-7-8."
                    ))
                    .requiereProfesional(true)
                    .build();

            guardarAlertaEnBD(idUsuario, alertaCritica);
            return alertaCritica;
        }

        G6_MH_RegistroEmocional ultimoRegistro = ultimos.get(0);

        // ESCENARIO 3 y 5: Alerta preventiva aislada por puntaje bajo (1 o 2)
        if (ultimoRegistro.getPuntaje() <= 2) {
            G6_MH_AlertaResponseDTO alertaPreventiva = G6_MH_AlertaResponseDTO.builder()
                    .tipo("ALERTA_PREVENTIVA")
                    .mensaje("Hemos detectado una baja en tu estado de ánimo el día de hoy.")
                    .recomendaciones(Arrays.asList(
                            "Escribe los desencadenantes de esta emoción en tu Diario Personal para procesarlo mejor.",
                            "Prueba escuchar las pistas de meditación sugeridas en nuestra biblioteca de apoyo."
                    ))
                    .requiereProfesional(false)
                    .build();

            guardarAlertaEnBD(idUsuario, alertaPreventiva);
            return alertaPreventiva;
        }

        // ESCENARIO 2: Notificaciones de progreso positivo (Puntaje 4 o 5)
        if (ultimoRegistro.getPuntaje() >= 4) {
            return G6_MH_AlertaResponseDTO.builder()
                    .tipo("PROGRESO_POSITIVO")
                    .mensaje("¡Excelente avance! El sistema reconoce tu progreso y estabilidad emocional el día de hoy. ¡Sigue cuidando de ti!")
                    .recomendaciones(Arrays.asList("Registrar de forma constante las razones de tu bienestar te ayudará a sostener este gran patrón."))
                    .requiereProfesional(false)
                    .build();
        }

        return null;
    }

    private void guardarAlertaEnBD(Long idUsuario, G6_MH_AlertaResponseDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        G6_MH_AlertaEmocional entidadAlerta = G6_MH_AlertaEmocional.builder()
                .tipoAlerta(dto.getTipo())
                .mensaje(dto.getMensaje())
                .sugerencias(String.join("; ", dto.getRecomendaciones()))
                .fechaGeneracion(LocalDateTime.now())
                .leido(false)
                .usuario(usuario)
                .build();

        alertaRepository.save(entidadAlerta);
    }
}