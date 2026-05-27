package com.upc.mind_health.security;

import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class G6_MH_UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private G6_MH_UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        G6_MH_Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + correo));

        if (!usuario.getActivo()) {
            throw new DisabledException("La cuenta no se encuentra activa. Verifique su correo.");
        }

        List<GrantedAuthority> autoridades = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getName())) // rol.getName() ya trae "ROLE_PACIENTE", etc.
                .collect(Collectors.toList());

        return new User(
                usuario.getCorreo(),
                usuario.getContrasena(),
                autoridades
        );
    }
}