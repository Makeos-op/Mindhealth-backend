package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface G6_MH_RolRepository extends JpaRepository<G6_MH_Rol, Long> {
    Optional<G6_MH_Rol> findByName(String name);
}