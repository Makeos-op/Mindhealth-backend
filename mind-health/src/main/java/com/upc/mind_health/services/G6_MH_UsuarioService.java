package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;
import com.upc.mind_health.security.G6_MH_JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class G6_MH_UsuarioService {

    private final G6_MH_UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final G6_MH_JwtUtil jwtUtil;

    public String registrar(G6_MH_UsuarioRegistroDTO dto) {
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        String token = UUID.randomUUID().toString();

        G6_MH_Usuario usuario = G6_MH_Usuario.builder()
                .nombre(dto.getNombre())
                .correo(dto.getCorreo())
                .contrasena(passwordEncoder.encode(dto.getContrasena()))
                .edad(dto.getEdad())
                .genero(dto.getGenero())
                .fechaRegistro(LocalDate.now())
                .rol("USER")
                .cuentaActiva(false)
                .tokenVerificacion(token)
                .build();

        usuarioRepository.save(usuario);

        return "Registro exitoso. Token de verificación (para pruebas): " + token;
    }

    public String verificarCuenta(String token) {
        G6_MH_Usuario usuario = usuarioRepository.findByTokenVerificacion(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o cuenta ya activada"));

        usuario.setCuentaActiva(true);
        usuario.setTokenVerificacion(null);
        usuarioRepository.save(usuario);

        return "Cuenta activada correctamente";
    }
}

