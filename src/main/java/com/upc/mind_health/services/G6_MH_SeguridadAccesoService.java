package com.upc.mind_health.services;

import com.upc.mind_health.dtos.G6_MH_AccesoResponseDTO;
import com.upc.mind_health.dtos.G6_MH_SimulacionLoginDTO;
import com.upc.mind_health.entities.G6_MH_HistorialAcceso;
import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_HistorialAccesoRepository;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class G6_MH_SeguridadAccesoService {

    private final G6_MH_HistorialAccesoRepository accesoRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;

    // ESCENARIO 1: Evaluar si el Login es sospechoso y registrarlo
    @Transactional
    public G6_MH_AccesoResponseDTO procesarIntentoAcceso(G6_MH_SimulacionLoginDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtenemos el historial previo para comparar ubicación
        List<G6_MH_HistorialAcceso> historico = accesoRepository.findByUsuarioIdUsuarioOrderByFechaAccesoDesc(dto.getIdUsuario());

        boolean sospechoso = false;

        // Si ya tiene accesos anteriores y la nueva ubicación es radicalmente distinta, alertamos
        if (!historico.isEmpty()) {
            String ultimaUbicacionHabitual = historico.get(0).getUbicacion();
            if (!ultimaUbicacionHabitual.equalsIgnoreCase(dto.getUbicacion())) {
                sospechoso = true;
            }
        }

        G6_MH_HistorialAcceso nuevoAcceso = G6_MH_HistorialAcceso.builder()
                .dispositivo(dto.getDispositivo())
                .ubicacion(dto.getUbicacion())
                .fechaAcceso(LocalDateTime.now())
                .esSospechoso(sospechoso)
                .usuario(usuario)
                .build();

        nuevoAcceso = accesoRepository.save(nuevoAcceso);

        if (sospechoso) {
            // 📧 ALERTA SIMULADA POR CORREO (Escenario 1)
            System.out.println("ALERTA DE SEGURIDAD MIND HEALTH");
            System.out.println("Estimado/a " + usuario.getNombre() + ", detectamos un acceso inusual.");
            System.out.println("Ubicación: " + dto.getUbicacion() + " | Dispositivo: " + dto.getDispositivo());
            System.out.println("Si no fuiste tú, protege tu cuenta inmediatamente cambiando tu contraseña aquí:");
            System.out.println("http://localhost:8080/api/auth/recuperar-contrasena");
        }

        return entityToDto(nuevoAcceso);
    }

    // ESCENARIO 2: Obtener historial de accesos para la sección "Seguridad"
    @Transactional(readOnly = true)
    public List<G6_MH_AccesoResponseDTO> listarHistorialAccesos(Long idUsuario) {
        return accesoRepository.findByUsuarioIdUsuarioOrderByFechaAccesoDesc(idUsuario).stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    private G6_MH_AccesoResponseDTO entityToDto(G6_MH_HistorialAcceso acceso) {
        return G6_MH_AccesoResponseDTO.builder()
                .idAcceso(acceso.getIdAcceso())
                .dispositivo(acceso.getDispositivo())
                .ubicacion(acceso.getUbicacion())
                .fechaAcceso(acceso.getFechaAcceso())
                .esSospechoso(acceso.getEsSospechoso())
                .build();
    }
}