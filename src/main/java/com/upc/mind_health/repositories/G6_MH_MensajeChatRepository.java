package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_MensajeChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface G6_MH_MensajeChatRepository extends JpaRepository<G6_MH_MensajeChat, Long> {

    List<G6_MH_MensajeChat> findBySesionIdSesionOrderByFechaEnvioAsc(Long idSesion);
}