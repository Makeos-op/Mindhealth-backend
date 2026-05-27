package com.upc.mind_health.services;

import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class G6_MH_UsuarioService {

    @Autowired
    private G6_MH_UsuarioRepository usuarioRepository;

    // ESCENARIO 1: Registro de usuario y generación de Token
    @Autowired
    private PasswordEncoder passwordEncoder; // <--- Inyectamos el encriptador de Spring Security

    // ESCENARIO 1: Registro de usuario y generación de Token
    public G6_MH_Usuario registrarUsuario(G6_MH_Usuario nuevoUsuario) {
        Optional<G6_MH_Usuario> usuarioExistente = usuarioRepository.findByCorreo(nuevoUsuario.getCorreo());
        if (usuarioExistente.isPresent()) {
            throw new RuntimeException("El correo ya se encuentra registrado.");
        }

        // ¡Aquí se activa la magia de la encriptación!
        String contrasenaEncriptada = passwordEncoder.encode(nuevoUsuario.getContrasena());
        nuevoUsuario.setContrasena(contrasenaEncriptada);

        // Configurar metadatos y fechas
        nuevoUsuario.setActivo(false);
        nuevoUsuario.setFechaRegistro(LocalDateTime.now()); // O LocalDateTime según lo que hayas elegido

        String tokenUnico = UUID.randomUUID().toString();
        nuevoUsuario.setTokenActivacion(tokenUnico);
        nuevoUsuario.setFechaExpiracionToken(LocalDateTime.now().plusHours(24));

        G6_MH_Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        enviarCorreoVerificacion(usuarioGuardado.getCorreo(), tokenUnico);

        return usuarioGuardado;
    }

    // ESCENARIO 2: Verificación de correo electrónico
    public boolean verificarCuenta(String token) {
        Optional<G6_MH_Usuario> usuarioOpt = usuarioRepository.findByTokenActivacion(token);

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Token de activación no válido.");
        }

        G6_MH_Usuario usuario = usuarioOpt.get();

        // Validar si el token ya expiró en el tiempo
        if (usuario.getFechaExpiracionToken().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token de activación ha expiración. Regístrese nuevamente.");
        }

        // Activar la cuenta del usuario
        usuario.setActivo(true);
        usuario.setTokenActivacion(null); // Limpiar el token usado
        usuario.setFechaExpiracionToken(null);

        usuarioRepository.save(usuario);
        return true;
    }

    private void enviarCorreoVerificacion(String correoDestino, String token) {
        String enlaceActivacion = "http://localhost:8080/api/auth/verificar?token=" + token;
        System.out.println("=========================================================");
        System.out.println("MOCK EMAIL SERVICE: Enviando correo a " + correoDestino);
        System.out.println("Haga clic en el siguiente enlace para activar su cuenta de Mind Health:");
        System.out.println(enlaceActivacion);
        System.out.println("=========================================================");
    }
}