package mhg6.repositories;

import mhg6.entities.G6_MH_PrivacidadConfig;
import mhg6.entities.G6_MH_Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface G6_MH_PrivacidadConfigRepository extends JpaRepository<G6_MH_PrivacidadConfig, Long> {
    Optional<G6_MH_PrivacidadConfig> findByUsuario(G6_MH_Usuario usuario);
}
