package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_HistorialAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface G6_MH_HistorialAccesoRepository extends JpaRepository<G6_MH_HistorialAcceso, Long> {

    // Escenario 2: Extrae los accesos del usuario ordenados desde el más reciente
    List<G6_MH_HistorialAcceso> findByUsuarioIdUsuarioOrderByFechaAccesoDesc(Long idUsuario);
}