package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_ContenidoTerapeutico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface G6_MH_ContenidoTerapeuticoRepository extends JpaRepository<G6_MH_ContenidoTerapeutico, Long> {
    // Filtra dinámicamente recursos que contengan la palabra clave de la emoción analizada por la IA
    List<G6_MH_ContenidoTerapeutico> findByEmocionAsociadaContainingIgnoreCase(String emocion);
}