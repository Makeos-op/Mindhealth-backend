package com.upc.mind_health.services;

import com.upc.mind_health.dtos.G6_MH_PrivacidadRequestDTO;
import com.upc.mind_health.dtos.G6_MH_PrivacidadResponseDTO;
import com.upc.mind_health.entities.G6_MH_PrivacidadConfig;
import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_PrivacidadConfigRepository;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class G6_MH_PrivacidadService {

    private final G6_MH_PrivacidadConfigRepository privacidadRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;

    // Obtener la configuración actual del usuario
    @Transactional(readOnly = true)
    public G6_MH_PrivacidadResponseDTO obtenerConfiguracionPorCorreo(String correoUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no autenticado o no encontrado"));

        G6_MH_PrivacidadConfig config = privacidadRepository.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseGet(() -> inicializarPrivacidadPorDefecto(usuario.getIdUsuario()));

        return entityToDto(config, "Configuración cargada correctamente.");
    }

    // ESCENARIO 1: Guardar preferencias de forma automática y segura usando la sesión del Token
    @Transactional
    public G6_MH_PrivacidadResponseDTO guardarPreferencias(G6_MH_PrivacidadRequestDTO dto, String correoUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no autenticado o no encontrado"));

        // Buscamos la configuración o forzamos la creación limpia con valores predeterminados para evitar campos NULL
        G6_MH_PrivacidadConfig config = privacidadRepository.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseGet(() -> {
                    G6_MH_PrivacidadConfig nueva = G6_MH_PrivacidadConfig.builder()
                            .usuario(usuario)
                            .anonimizacion(false)
                            .usoIa(true)
                            .visibilidadDatos(true)
                            .compartirConTerapeuta(true)
                            .permitirAnalisisIa(true)
                            .build();
                    return privacidadRepository.save(nueva);
                });

        // Seteamos los valores actualizados provenientes de la interfaz
        config.setPermitirAnalisisIa(dto.getPermitirAnalisisIa());
        config.setCompartirConTerapeuta(dto.getCompartirConTerapeuta());
        config.setAnonimizacion(dto.getAnonimizacion());
        config.setUsoIa(dto.getUsoIa());
        config.setVisibilidadDatos(dto.getVisibilidadDatos());

        config = privacidadRepository.save(config);

        return entityToDto(config, "Preferencias de privacidad actualizadas con éxito de forma segura.");
    }

    @Transactional
    public G6_MH_PrivacidadConfig inicializarPrivacidadPorDefecto(Long idUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        G6_MH_PrivacidadConfig nuevaConfig = G6_MH_PrivacidadConfig.builder()
                .usuario(usuario)
                .anonimizacion(false)
                .usoIa(true)
                .visibilidadDatos(true)
                .compartirConTerapeuta(true)
                .permitirAnalisisIa(true)
                .build();

        return privacidadRepository.save(nuevaConfig);
    }

    private G6_MH_PrivacidadResponseDTO entityToDto(G6_MH_PrivacidadConfig config, String mensaje) {
        return G6_MH_PrivacidadResponseDTO.builder()
                .idPrivacidad(config.getIdPrivacidad())
                .idUsuario(config.getUsuario().getIdUsuario())
                .permitirAnalisisIa(config.getPermitirAnalisisIa())
                .compartirConTerapeuta(config.getCompartirConTerapeuta())
                .anonimizacion(config.getAnonimizacion())
                .usoIa(config.getUsoIa())
                .visibilidadDatos(config.getVisibilidadDatos())
                .mensaje(mensaje)
                .build();
    }
}