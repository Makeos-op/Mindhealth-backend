package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.G6_MH_PrivacidadDTO;
import com.upc.mind_health.entities.G6_MH_PrivacidadConfig;
import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_PrivacidadConfigRepository;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class G6_MH_PrivacidadService {

    private final G6_MH_PrivacidadConfigRepository privacidadRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;

    public G6_MH_PrivacidadDTO guardarPrivacidad(Long idUsuario, G6_MH_PrivacidadDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        G6_MH_PrivacidadConfig config = privacidadRepository.findByUsuario(usuario)
                .orElse(G6_MH_PrivacidadConfig.builder().usuario(usuario).build());

        config.setVisibilidadDatos(dto.getVisibilidadDatos());
        config.setUsoIa(dto.getUsoIa());
        config.setAnonimizacion(dto.getAnonimizacion());

        privacidadRepository.save(config);

        return G6_MH_PrivacidadDTO.builder()
                .visibilidadDatos(config.getVisibilidadDatos())
                .usoIa(config.getUsoIa())
                .anonimizacion(config.getAnonimizacion())
                .build();
    }

    public G6_MH_PrivacidadDTO obtenerPrivacidad(Long idUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        G6_MH_PrivacidadConfig config = privacidadRepository.findByUsuario(usuario)
                .orElse(G6_MH_PrivacidadConfig.builder()
                        .visibilidadDatos(false)
                        .usoIa(false)
                        .anonimizacion(false)
                        .build());

        return G6_MH_PrivacidadDTO.builder()
                .visibilidadDatos(config.getVisibilidadDatos())
                .usoIa(config.getUsoIa())
                .anonimizacion(config.getAnonimizacion())
                .build();
    }
}
