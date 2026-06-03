package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_Psicologo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface G6_MH_PsicologoRepository extends JpaRepository<G6_MH_Psicologo, Long> {
    //Encontrar terapeutas listos para recibir alertas críticas
    List<G6_MH_Psicologo> findByDisponibleTrue();
}