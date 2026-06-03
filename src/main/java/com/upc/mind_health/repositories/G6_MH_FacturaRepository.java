package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface G6_MH_FacturaRepository extends JpaRepository<G6_MH_Factura, Long> {
    List<G6_MH_Factura> findByUsuarioIdUsuarioOrderByFechaDesc(Long idUsuario);
}