package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.G6_MH_RegistroEmocionalRequestDTO;
import com.upc.mind_health.dtos.G6_MH_RegistroEmocionalResponseDTO;
import com.upc.mind_health.dtos.G6_MH_UsuarioReducidoDTO;
import com.upc.mind_health.entities.G6_MH_RegistroEmocional;
import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_RegistroEmocionalRepository;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class G6_MH_RegistroEmocionalService {

    private final G6_MH_RegistroEmocionalRepository registroRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;

    @Transactional
    public G6_MH_RegistroEmocionalResponseDTO registrarEmocion(G6_MH_RegistroEmocionalRequestDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (registroRepository.findByUsuarioIdUsuarioAndFecha(dto.getIdUsuario(), LocalDate.now()).isPresent()) {
            throw new RuntimeException("Ya registraste tu estado de ánimo hoy. Vuelve mañana.");
        }

        G6_MH_RegistroEmocional registro = G6_MH_RegistroEmocional.builder()
                .usuario(usuario)
                .emocion(dto.getEmocion())
                .descripcion(dto.getDescripcion())
                .fecha(LocalDate.now()) // Se asigna automáticamente la fecha actual al guardar
                .puntaje(dto.getPuntaje())
                .build();

        registro = registroRepository.save(registro);

        return toResponseDTO(registro);
    }

    // HU-15: Consulta si ya existe un check-in registrado hoy, para bloquear el formulario en el frontend
    @Transactional(readOnly = true)
    public G6_MH_RegistroEmocionalResponseDTO obtenerRegistroDeHoy(Long idUsuario) {
        Optional<G6_MH_RegistroEmocional> registroDeHoy =
                registroRepository.findByUsuarioIdUsuarioAndFecha(idUsuario, LocalDate.now());
        return registroDeHoy.map(this::toResponseDTO).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<G6_MH_RegistroEmocionalResponseDTO> obtenerHistorial(Long idUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return registroRepository.findByUsuarioOrderByFechaDesc(usuario)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String analizarImpactoEvento(Long idUsuario) {
        // Usamos tu query nativa con límite 2 para comparar el día de hoy con el de ayer
        List<G6_MH_RegistroEmocional> ultimos = registroRepository.findUltimosRegistros(idUsuario, 2);

        if (ultimos.size() < 2) {
            return "Registra más días para que la IA pueda evaluar el impacto de tus eventos.";
        }

        G6_MH_RegistroEmocional hoy = ultimos.get(0);
        G6_MH_RegistroEmocional ayer = ultimos.get(1);

        // Si el puntaje bajó o subió, la IA genera la relación
        if (hoy.getPuntaje() < ayer.getPuntaje()) {
            return "La IA detectó un cambio de ánimo a raíz de tu evento registrado: '" + hoy.getEmocion() +
                    "'. El evento '" + hoy.getDescripcion() + "' impactó negativamente en comparación con ayer.";
        } else if (hoy.getPuntaje() > ayer.getPuntaje()) {
            return "¡Gran cambio positivo! Tu evento de hoy: '" + hoy.getDescripcion() +
                    "' elevó tu estado de ánimo a '" + hoy.getEmocion() + "' en comparación con el día anterior.";
        }

        return "Tu estado de ánimo se mantiene estable con respecto a los eventos de ayer.";
    }

    @Transactional(readOnly = true)
    public String obtenerRecompensaMotivacional(Long idUsuario) {
        List<G6_MH_RegistroEmocional> historialSemanal = registroRepository.findByUsuarioIdUsuarioOrderByFechaAsc(idUsuario);

        // Tomamos como máximo los últimos 7 registros de la lista
        List<G6_MH_RegistroEmocional> ultimos7Dias = historialSemanal.stream()
                .skip(Math.max(0, historialSemanal.size() - 7))
                .toList();

        if (ultimos7Dias.isEmpty()) {
            return "Comienza tu registro diario para recibir mensajes de progreso.";
        }

        // Calculamos matemáticamente el promedio de bienestar semanal
        double promedioPuntaje = ultimos7Dias.stream()
                .mapToInt(G6_MH_RegistroEmocional::getPuntaje)
                .average()
                .orElse(0.0);

        // Si el promedio es alto (mayor o igual a 3.5), se activa la recompensa motivacional
        if (promedioPuntaje >= 3.5) {
            return "¡Estás avanzando! Esta semana tuviste más días felices. Sigue así.";
        }

        return "Has completado tus registros de la semana. Continuar expresándote es el primer paso para tu bienestar.";
    }

    private G6_MH_RegistroEmocionalResponseDTO toResponseDTO(G6_MH_RegistroEmocional registro) {
        return G6_MH_RegistroEmocionalResponseDTO.builder()
                .idRegistro(registro.getIdRegistro())
                .usuario(G6_MH_UsuarioReducidoDTO.builder()
                        .idUsuario(registro.getUsuario().getIdUsuario())
                        .nombre(registro.getUsuario().getNombre())
                        .build())
                .emocion(registro.getEmocion())
                .descripcion(registro.getDescripcion())
                .fecha(registro.getFecha())
                .puntaje(registro.getPuntaje())
                .build();
    }
}