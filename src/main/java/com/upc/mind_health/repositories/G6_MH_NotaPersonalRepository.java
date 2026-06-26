package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_NotaPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface G6_MH_NotaPersonalRepository extends JpaRepository<G6_MH_NotaPersonal, Long> {
    // HU-41 Escenario 2: Historial ordenado de la más reciente a la más antigua
    List<G6_MH_NotaPersonal> findByUsuarioIdUsuarioOrderByFechaCreacionDesc(Long idUsuario);
}