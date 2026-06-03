package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface G6_MH_MetodoPagoRepository extends JpaRepository<G6_MH_MetodoPago, Long> {
    List<G6_MH_MetodoPago> findByUsuarioIdUsuario(Long idUsuario);
}