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
    public G6_MH_AccesoResponseDTO procesarIntentoAcceso(G6_MH_SimulacionLoginDTO dto, String correoUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no autenticado o inexistente"));

        List<G6_MH_HistorialAcceso> historico = accesoRepository.findByUsuarioIdUsuarioOrderByFechaAccesoDesc(usuario.getIdUsuario());

        boolean sospechoso = false;

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
            System.out.println("ALERTA DE SEGURIDAD AUTOMÁTICA");
            System.out.println("Acceso inusual detectado para: " + usuario.getNombre());
            System.out.println("Notificación preventiva enviada a su casilla: " + usuario.getCorreo());
        }

        return entityToDto(nuevoAcceso);
    }

    //ESCENARIO 2: Obtener historial de accesos usando el correo seguro del Token JWT
    @Transactional(readOnly = true)
    public List<G6_MH_AccesoResponseDTO> listarHistorialAccesosPorCorreo(String correoUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no autenticado o inexistente"));

        return accesoRepository.findByUsuarioIdUsuarioOrderByFechaAccesoDesc(usuario.getIdUsuario()).stream()
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