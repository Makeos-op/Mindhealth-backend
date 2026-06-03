package com.upc.mind_health.repositories;

import com.upc.mind_health.entities.G6_MH_RegistroEmocional;
import com.upc.mind_health.entities.G6_MH_Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface G6_MH_RegistroEmocionalRepository extends JpaRepository<G6_MH_RegistroEmocional, Long> {
    // HU-15: Historial descendente clásico para la lista del perfil del usuario
    List<G6_MH_RegistroEmocional> findByUsuarioOrderByFechaDesc(G6_MH_Usuario usuario);

    // HU-06 Escenario 1: Historial cronológico ascendente indispensable para gráficos de línea
    List<G6_MH_RegistroEmocional> findByUsuarioIdUsuarioOrderByFechaAsc(Long idUsuario);

    // HU-06 Escenarios 2, 3 y 4: Native Query para extraer la muestra de los últimos registros
    @Query(value = "SELECT * FROM registro_emocional WHERE id_usuario = :idUsuario ORDER BY fecha DESC LIMIT :limite", nativeQuery = true)
    List<G6_MH_RegistroEmocional> findUltimosRegistros(@Param("idUsuario") Long idUsuario, @Param("limite") int limite);
}
