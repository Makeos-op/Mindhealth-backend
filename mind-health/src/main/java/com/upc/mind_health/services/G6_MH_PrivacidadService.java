package mhg6.services;

import lombok.RequiredArgsConstructor;
import mhg6.dtos.G6_MH_PrivacidadDTO;
import mhg6.entities.G6_MH_PrivacidadConfig;
import mhg6.entities.G6_MH_Usuario;
import mhg6.repositories.G6_MH_PrivacidadConfigRepository;
import mhg6.repositories.G6_MH_UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class G6_MH_PrivacidadService {

    private final G6_MH_PrivacidadConfigRepository privacidadRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;

    // HU07 - Guardar o actualizar configuración de privacidad
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

    // HU07 - Obtener configuración de privacidad
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
