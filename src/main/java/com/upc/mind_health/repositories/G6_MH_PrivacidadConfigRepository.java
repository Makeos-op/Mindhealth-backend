package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_PrivacidadConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface G6_MH_PrivacidadConfigRepository extends JpaRepository<G6_MH_PrivacidadConfig, Long> {
    // Busca las preferencias de privacidad al ID del usuario
    Optional<G6_MH_PrivacidadConfig> findByUsuarioIdUsuario(Long idUsuario);
}