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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class G6_MH_RegistroEmocionalService {

    private final G6_MH_RegistroEmocionalRepository registroRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;

    public G6_MH_RegistroEmocionalResponseDTO registrarEmocion(G6_MH_RegistroEmocionalRequestDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        G6_MH_RegistroEmocional registro = G6_MH_RegistroEmocional.builder()
                .usuario(usuario)
                .emocion(dto.getEmocion())
                .descripcion(dto.getDescripcion())
                .fecha(LocalDate.now())
                .build();

        registro = registroRepository.save(registro);

        return toResponseDTO(registro);
    }

    public List<G6_MH_RegistroEmocionalResponseDTO> obtenerHistorial(Long idUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return registroRepository.findByUsuarioOrderByFechaDesc(usuario)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
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
                .build();
    }
}
