package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_SesionCifrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface G6_MH_SesionCifradaRepository extends JpaRepository<G6_MH_SesionCifrada, Long> {
    List<G6_MH_SesionCifrada> findByUsuarioCorreoOrderByFechaInteraccionDesc(String correo);
    Optional<G6_MH_SesionCifrada> findByCodigoSesion(String codigoSesion);
}