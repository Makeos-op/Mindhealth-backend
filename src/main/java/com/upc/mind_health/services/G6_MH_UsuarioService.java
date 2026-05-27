package com.upc.mind_health.services;

import com.upc.mind_health.entities.G6_MH_Rol;
import com.upc.mind_health.entities.G6_MH_Token;
import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_RolRepository;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;
import com.upc.mind_health.repositories.G6_MH_TokenRepository;
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

    @Autowired
    private G6_MH_RolRepository rolRepository;

    @Autowired
    private G6_MH_TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================
    // HU-01: REGISTRO Y ACTIVACIÓN DE CUENTA
    // ==========================================
    // ESCENARIO 1: Registro de usuario y generación de Token en tabla independiente
    public G6_MH_Usuario registrarUsuario(G6_MH_Usuario nuevoUsuario) {
        Optional<G6_MH_Usuario> usuarioExistente = usuarioRepository.findByCorreo(nuevoUsuario.getCorreo());
        if (usuarioExistente.isPresent()) {
            throw new RuntimeException("El correo ya se encuentra registrado.");
        }

        nuevoUsuario.setContrasena(passwordEncoder.encode(nuevoUsuario.getContrasena()));
        nuevoUsuario.setActivo(false);
        nuevoUsuario.setFechaRegistro(LocalDateTime.now());

        G6_MH_Rol rolPaciente = rolRepository.findByName("ROLE_PACIENTE")
                .orElseThrow(() -> new RuntimeException("Error: El rol ROLE_PACIENTE no está inicializado en la base de datos."));
        nuevoUsuario.getRoles().add(rolPaciente);

        G6_MH_Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        String tokenUnico = UUID.randomUUID().toString();
        G6_MH_Token tokenActivacion = G6_MH_Token.builder()
                .token(tokenUnico)
                .tipoToken("ACTIVACION")
                .fechaExpiracion(LocalDateTime.now().plusHours(24)) // 24 horas de validez para activarse
                .usuario(usuarioGuardado)
                .build();

        tokenRepository.save(tokenActivacion);

        enviarCorreoVerificacion(usuarioGuardado.getCorreo(), tokenUnico);

        return usuarioGuardado;
    }

    // ESCENARIO 2: Verificación de correo electrónico (Corregido y Limpio)
    public boolean verificarCuenta(String token) {
        G6_MH_Token tokenDb = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("El enlace de activación no es válido."));

        if (!tokenDb.getTipoToken().equals("ACTIVACION")) {
            throw new RuntimeException("Este token no corresponde a una activación de cuenta.");
        }

        if (tokenDb.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El enlace de activación ha expirado.");
        }

        G6_MH_Usuario usuario = tokenDb.getUsuario();
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        tokenRepository.delete(tokenDb);
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

    // HU-03: RECUPERACIÓN DE CONTRASEÑA
    // Escenario 1: Solicitar recuperación
    public void solicitarRecuperacion(String correo) {
        G6_MH_Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("No existe ninguna cuenta registrada con este correo electrónico."));

        String tokenUUID = UUID.randomUUID().toString();

        G6_MH_Token tokenRecuperacion = G6_MH_Token.builder()
                .token(tokenUUID)
                .tipoToken("RECUPERACION")
                .fechaExpiracion(LocalDateTime.now().plusMinutes(15)) // 15 minutos por seguridad
                .usuario(usuario)
                .build();
        tokenRepository.save(tokenRecuperacion);

        System.out.println("=========================================================");
        System.out.println("MOCK EMAIL SERVICE: Recuperación de Contraseña");
        System.out.println("Enlace de recuperación: http://localhost:8080/api/auth/restablecer?token=" + tokenUUID);
        System.out.println("=========================================================");
    }

    // Escenario 2: Restablecer contraseña
    public void restablecerContrasena(String token, String nuevaContrasena) {
        G6_MH_Token tokenDb = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("El enlace de recuperación no es válido o ya fue utilizado."));

        if (!tokenDb.getTipoToken().equals("RECUPERACION")) {
            throw new RuntimeException("Este token no corresponde a una recuperación de contraseña.");
        }

        if (tokenDb.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El enlace de recuperación ha expirado.");
        }

        G6_MH_Usuario usuario = tokenDb.getUsuario();
        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);

        tokenRepository.delete(tokenDb); // Consumimos el token borrándolo de la BD
    }
}