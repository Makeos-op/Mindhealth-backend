package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_Colaboracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface G6_MH_ColaboracionRepository extends JpaRepository<G6_MH_Colaboracion, Long> {
    // Para que el psicólogo invitado vea qué solicitudes tiene pendientes
    List<G6_MH_Colaboracion> findByReceptorUsuarioCorreo(String correoReceptor);
}