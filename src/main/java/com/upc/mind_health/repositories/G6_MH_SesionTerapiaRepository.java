package com.upc.mind_health.repositories;
import com.upc.mind_health.entities.G6_MH_SesionTerapia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface G6_MH_SesionTerapiaRepository extends JpaRepository<G6_MH_SesionTerapia, Long> {
    Optional<G6_MH_SesionTerapia> findByUsuarioIdUsuarioAndEstado(Long idUsuario, String estado);
}