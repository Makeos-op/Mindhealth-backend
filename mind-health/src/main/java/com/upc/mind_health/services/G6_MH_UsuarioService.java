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

    // HU01 - Registro con generación de token de verificación
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

        // En producción se enviaría correo; aquí se retorna el token para pruebas
        return "Registro exitoso. Token de verificación (para pruebas): " + token;
    }

    // HU01 - Activación de cuenta mediante token
    public String verificarCuenta(String token) {
        G6_MH_Usuario usuario = usuarioRepository.findByTokenVerificacion(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o cuenta ya activada"));

        usuario.setCuentaActiva(true);
        usuario.setTokenVerificacion(null);
        usuarioRepository.save(usuario);

        return "Cuenta activada correctamente";
    }

    // HU02 - Login con validación de cuenta activa
    public G6_MH_AuthResponseDTO login(G6_MH_LoginDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findByCorreo(dto.getCorreo())
                .orElseThrow(() -> new RuntimeException("Credenciales no válidas"));

        if (!passwordEncoder.matches(dto.getContrasena(), usuario.getContrasena())) {
            throw new RuntimeException("Credenciales no válidas");
        }

        if (!usuario.getCuentaActiva()) {
            throw new RuntimeException("Cuenta no verificada. Revisa tu correo");
        }

        String token = jwtUtil.generateToken(usuario.getCorreo());

        return G6_MH_AuthResponseDTO.builder()
                .token(token)
                .mensaje("Inicio de sesión exitoso")
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .build();
    }

    // HU04 - Ver perfil
    public G6_MH_PerfilResponseDTO obtenerPerfil(Long id) {
        G6_MH_Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return G6_MH_PerfilResponseDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .edad(usuario.getEdad())
                .genero(usuario.getGenero())
                .fechaRegistro(usuario.getFechaRegistro())
                .rol(usuario.getRol())
                .cuentaActiva(usuario.getCuentaActiva())
                .build();
    }

    public G6_MH_PerfilResponseDTO actualizarPerfil(Long id, G6_MH_PerfilUpdateDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (dto.getNombre() != null) usuario.setNombre(dto.getNombre());
        if (dto.getEdad() != null) usuario.setEdad(dto.getEdad());
        if (dto.getGenero() != null) usuario.setGenero(dto.getGenero());

        usuarioRepository.save(usuario);

        return G6_MH_PerfilResponseDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .edad(usuario.getEdad())
                .genero(usuario.getGenero())
                .fechaRegistro(usuario.getFechaRegistro())
                .rol(usuario.getRol())
                .cuentaActiva(usuario.getCuentaActiva())
                .build();
    }

    // HU03 - Solicitar recuperación de contraseña
    public String solicitarRecuperacion(String correo) {
        G6_MH_Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Correo no registrado"));

        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacion(token);
        usuarioRepository.save(usuario);

        // En producción se enviaría correo; aquí se retorna el token para pruebas
        return "Correo de recuperación enviado. Token (para pruebas): " + token;
    }

    // HU03 - Restablecer contraseña con token
    public String resetPassword(G6_MH_ResetPasswordDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findByTokenRecuperacion(dto.getToken())
                .orElseThrow(() -> new RuntimeException("Token inválido o expirado"));

        usuario.setContrasena(passwordEncoder.encode(dto.getNuevaContrasena()));
        usuario.setTokenRecuperacion(null);
        usuarioRepository.save(usuario);

        return "Contraseña actualizada correctamente";
    }
}
