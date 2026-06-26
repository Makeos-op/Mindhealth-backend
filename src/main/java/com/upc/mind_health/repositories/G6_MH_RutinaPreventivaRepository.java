package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_RutinaPreventiva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface G6_MH_RutinaPreventivaRepository extends JpaRepository<G6_MH_RutinaPreventiva, Long> {
    List<G6_MH_RutinaPreventiva> findByUsuarioIdUsuarioOrderByFechaGeneracionDesc(Long idUsuario);
}