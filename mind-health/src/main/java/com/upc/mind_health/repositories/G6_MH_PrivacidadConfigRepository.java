package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_PrivacidadConfig;
import com.upc.mind_health.entities.G6_MH_Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface G6_MH_PrivacidadConfigRepository extends JpaRepository<G6_MH_PrivacidadConfig, Long> {
    Optional<G6_MH_PrivacidadConfig> findByUsuario(G6_MH_Usuario usuario);
}
