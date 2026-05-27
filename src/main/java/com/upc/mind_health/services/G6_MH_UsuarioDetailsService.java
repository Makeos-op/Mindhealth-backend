package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class G6_MH_UsuarioDetailsService implements UserDetailsService {

    private final G6_MH_UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        G6_MH_Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        return new User(
                usuario.getCorreo(),
                usuario.getContrasena(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().toUpperCase()))
        );
    }
}
