package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_MetaBienestar;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface G6_MH_MetaBienestarRepository extends JpaRepository<G6_MH_MetaBienestar, Long> {
    List<G6_MH_MetaBienestar> findByUsuarioIdUsuarioOrderByFechaRegistroDesc(Long idUsuario);
}