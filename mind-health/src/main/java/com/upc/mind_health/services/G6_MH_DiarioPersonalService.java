package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;

import com.upc.mind_health.dtos.G6_MH_DiarioRequestDTO;
import com.upc.mind_health.dtos.G6_MH_DiarioResponseDTO;
import com.upc.mind_health.dtos.G6_MH_UsuarioReducidoDTO;


import com.upc.mind_health.entities.G6_MH_DiarioPersonal;
import com.upc.mind_health.repositories.G6_MH_DiarioPersonalRepository;


import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class G6_MH_DiarioPersonalService {

    private final G6_MH_DiarioPersonalRepository diarioRepository;
    private final G6_MH_UsuarioRepository usuarioRepository; // Limpiado el nombre largo


    public G6_MH_DiarioResponseDTO crearNota(G6_MH_DiarioRequestDTO dto) {

        if (dto.getTitulo() == null || dto.getTitulo().isBlank()) {
            throw new RuntimeException("El título no puede estar vacío");
        }
        if (dto.getContenido() == null || dto.getContenido().isBlank()) {
            throw new RuntimeException("El contenido no puede estar vacío");
        }

        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        G6_MH_DiarioPersonal nota = G6_MH_DiarioPersonal.builder()
                .usuario(usuario)
                .titulo(dto.getTitulo())
                .contenido(dto.getContenido())
                .fecha(LocalDate.now()) // Cumple con registrar la fecha actual
                .build();

        nota = diarioRepository.save(nota);

        return toResponseDTO(nota);
    }

    public List<G6_MH_DiarioResponseDTO> obtenerNotas(Long idUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


        return diarioRepository.findByUsuarioOrderByFechaDesc(usuario)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private G6_MH_DiarioResponseDTO toResponseDTO(G6_MH_DiarioPersonal nota) {
        return G6_MH_DiarioResponseDTO.builder()
                .idDiario(nota.getIdDiario())
                .usuario(G6_MH_UsuarioReducidoDTO.builder()
                        .idUsuario(nota.getUsuario().getIdUsuario())
                        .nombre(nota.getUsuario().getNombre())
                        .build())
                .titulo(nota.getTitulo())
                .contenido(nota.getContenido())
                .fecha(nota.getFecha())
                .build();
    }
}