package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.*;
import com.upc.mind_health.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class G6_MH_UsuarioSesionService {

    private final G6_MH_FavoritoRepository favoritoRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;
    private final G6_MH_ContenidoTerapeuticoRepository contenidoRepository;

    // 🌟 HU-37 ESCENARIO 1: Guardar un ejercicio o recurso en la lista de favoritos
    @Transactional
    public String guardarEjercicioFavorito(G6_MH_FavoritoRequestDTO dto) {
        // Validamos si ya existe para no duplicar el favorito
        if (favoritoRepository.findByUsuarioIdUsuarioAndContenidoIdContenido(dto.getIdUsuario(), dto.getIdContenido()).isPresent()) {
            return "Este ejercicio ya se encuentra en tu lista de favoritos.";
        }

        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        G6_MH_ContenidoTerapeutico contenido = contenidoRepository.findById(dto.getIdContenido())
                .orElseThrow(() -> new RuntimeException("Contenido terapéutico no encontrado."));

        G6_MH_Favorito favorito = G6_MH_Favorito.builder()
                .usuario(usuario)
                .contenido(contenido)
                .fechaGuardado(LocalDateTime.now())
                .build();

        favoritoRepository.save(favorito);
        return "El ejercicio terapéutico ha sido guardado correctamente en tus favoritos.";
    }

    // 🌟 HU-37 ESCENARIO 2: Obtener el listado de recursos favoritos del usuario
    @Transactional(readOnly = true)
    public List<G6_MH_ContenidoTerapeutico> obtenerFavoritosUsuario(Long idUsuario) {
        return favoritoRepository.findByUsuarioIdUsuarioOrderByFechaGuardadoDesc(idUsuario).stream()
                .map(G6_MH_Favorito::getContenido)
                .collect(Collectors.toList());
    }

    // 🌟 HU-36 ESCENARIO 1: Endpoint semántico para cierre de sesión en auditoría
    @Transactional(readOnly = true)
    public String finalizarSesionToken(Long idUsuario) {
        G6_MH_Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        System.out.println("[AUDITORÍA] Sesión finalizada correctamente para el usuario: " + usuario.getCorreo());
        return "Sesión finalizada exitosamente en el sistema.";
    }
}
