package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_PoliticaDatos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface G6_MH_PoliticaDatosRepository extends JpaRepository<G6_MH_PoliticaDatos, Long> {
    // Retorna la política de privacidad más reciente ingresada en la base de datos
    Optional<G6_MH_PoliticaDatos> findFirstByOrderByUltimaActualizacionDesc();
}