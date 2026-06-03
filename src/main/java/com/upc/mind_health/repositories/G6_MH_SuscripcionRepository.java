package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface G6_MH_SuscripcionRepository extends JpaRepository<G6_MH_Suscripcion, Long> {
    Optional<G6_MH_Suscripcion> findByUsuarioIdUsuario(Long idUsuario);
}