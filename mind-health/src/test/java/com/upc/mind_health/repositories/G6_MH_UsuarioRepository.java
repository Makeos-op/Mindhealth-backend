package mhg6.repositories;

import mhg6.entities.G6_MH_Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface G6_MH_UsuarioRepository extends JpaRepository<G6_MH_Usuario, Long> {
    Optional<G6_MH_Usuario> findByCorreo(String correo);
    Optional<G6_MH_Usuario> findByTokenVerificacion(String token);
    Optional<G6_MH_Usuario> findByTokenRecuperacion(String token);
    boolean existsByCorreo(String correo);
}