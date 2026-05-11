package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_DiarioPersonal;
import com.upc.mind_health.entities.G6_MH_Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface G6_MH_DiarioPersonalRepository extends JpaRepository<G6_MH_DiarioPersonal, Long> {

    List<G6_MH_DiarioPersonal> findByUsuarioOrderByFechaDesc(G6_MH_Usuario usuario);
}