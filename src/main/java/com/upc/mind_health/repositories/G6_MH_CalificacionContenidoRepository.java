package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_CalificacionContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface G6_MH_CalificacionContenidoRepository extends JpaRepository<G6_MH_CalificacionContenido, Long> {
    List<G6_MH_CalificacionContenido> findByContenidoIdContenidoOrderByFechaRegistroDesc(Long idContenido);
    List<G6_MH_CalificacionContenido> findByUsuarioIdUsuarioOrderByFechaRegistroDesc(Long idUsuario);
}