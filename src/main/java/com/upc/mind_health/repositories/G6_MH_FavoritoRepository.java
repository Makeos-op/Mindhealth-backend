package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface G6_MH_FavoritoRepository extends JpaRepository<G6_MH_Favorito, Long> {
    List<G6_MH_Favorito> findByUsuarioIdUsuarioOrderByFechaGuardadoDesc(Long idUsuario);
    Optional<G6_MH_Favorito> findByUsuarioIdUsuarioAndContenidoIdContenido(Long idUsuario, Long idContenido);
}