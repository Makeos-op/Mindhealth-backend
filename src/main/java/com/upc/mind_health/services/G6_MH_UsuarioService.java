package com.upc.mind_health.services;

import com.upc.mind_health.dtos.G6_MH_PerfilDTO;
import com.upc.mind_health.entities.G6_MH_Rol;
import com.upc.mind_health.entities.G6_MH_Token;
import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_RolRepository;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;
import com.upc.mind_health.repositories.G6_MH_TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private G6_MH_EmailService emailService;

    @Value("${mindhealth.app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Value("${mindhealth.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    // ==========================================
    // HU-01: REGISTRO Y ACTIVACIÓN DE CUENTA
    // ==========================================
    // ESCENARIO 1: Registro de usuario y generación de Token en tabla independiente
// HU-01: Registro de usuario con validación completa en el Service
    public G6_MH_Usuario registrarUsuario(G6_MH_Usuario nuevoUsuario) {
        // 1. PASAR LOS "NOT BLANK" AL SERVICE (Validación Estricta)
        if (nuevoUsuario.getNombre() == null || nuevoUsuario.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre es un campo obligatorio.");
        }
        if (nuevoUsuario.getCorreo() == null || nuevoUsuario.getCorreo().trim().isEmpty()) {
            throw new RuntimeException("El correo electrónico es un campo obligatorio.");
        }
        if (nuevoUsuario.getContrasena() == null || nuevoUsuario.getContrasena().trim().isEmpty()) {
            throw new RuntimeException("La contraseña es un campo obligatorio.");
        }
        if (nuevoUsuario.getGenero() == null || nuevoUsuario.getGenero().trim().isEmpty()) {
            throw new RuntimeException("El género es un campo obligatorio.");
        }
        if (nuevoUsuario.getEdad() == null) {
            throw new RuntimeException("La edad es un campo obligatorio.");
        }
        // 2. REGLAS DE NEGOCIO (Formatos y Rangos)
        // Validar formato básico de correo electrónico
        if (!nuevoUsuario.getCorreo().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Por favor, ingrese un formato de correo electrónico válido.");
        }

        // Validar rango de Edad
        if (nuevoUsuario.getEdad() < 18 || nuevoUsuario.getEdad() > 120) {
            throw new RuntimeException("Por favor, ingrese una edad válida.");
        }

        // Validar longitud de contraseña
        if (nuevoUsuario.getContrasena().length() < 6) {
            throw new RuntimeException("La contraseña debe tener una longitud mínima de 6 caracteres.");
        }

        // Validar duplicados en la Base de Datos
        Optional<G6_MH_Usuario> usuarioExistente = usuarioRepository.findByCorreo(nuevoUsuario.getCorreo());
        if (usuarioExistente.isPresent()) {
            throw new RuntimeException("El correo ya se encuentra registrado.");
        }
        // 3. PROCESAMIENTO Y PERSISTENCIA (Core del Negocio)
        nuevoUsuario.setContrasena(passwordEncoder.encode(nuevoUsuario.getContrasena()));
        nuevoUsuario.setActivo(false);
        nuevoUsuario.setFechaRegistro(LocalDateTime.now());
        // Valor predeterminado para pacientes; se ajusta luego vía preferencias de terapia (HU-28)
        nuevoUsuario.setEstiloLenguajeIa("INFORMAL");

        G6_MH_Rol rolPaciente = rolRepository.findByName("ROLE_PACIENTE")
                .orElseThrow(() -> new RuntimeException("Error: El rol ROLE_PACIENTE no está inicializado."));
        nuevoUsuario.getRoles().add(rolPaciente);

        G6_MH_Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        // Generar Token de Activación Independiente
        String tokenUnico = UUID.randomUUID().toString();
        G6_MH_Token tokenActivacion = G6_MH_Token.builder()
                .token(tokenUnico)
                .tipoToken("ACTIVACION")
                .fechaExpiracion(LocalDateTime.now().plusHours(24))
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
        String enlaceActivacion = appBaseUrl + "/api/auth/verificar?token=" + token;
        emailService.enviarCorreo(
                correoDestino,
                "Confirma tu cuenta de Mind Health",
                "Haz clic en el siguiente enlace para activar tu cuenta de Mind Health:\n" + enlaceActivacion
        );
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

        String enlaceRecuperacion = frontendBaseUrl + "/restablecer-password?token=" + tokenUUID;
        emailService.enviarCorreo(
                usuario.getCorreo(),
                "Recuperación de contraseña — Mind Health",
                "Recibimos una solicitud para restablecer tu contraseña. Usa el siguiente enlace (válido por 15 minutos):\n"
                        + enlaceRecuperacion
        );
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

    // HU-04: EDICIÓN DEL PERFIL DE USUARIO
    // Escenario 1: Obtener la información del perfil actual
    public G6_MH_PerfilDTO obtenerPerfil(String correo) {
        G6_MH_Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        return G6_MH_PerfilDTO.builder()
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .edad(usuario.getEdad())
                .genero(usuario.getGenero())
                .build();
    }

    // Escenario 2: Modificar y guardar los datos personales
    public void actualizarPerfil(String correo, G6_MH_PerfilDTO perfilDTO) {
        // 1. Buscamos el usuario REAL y COMPLETO que está guardado en la BD
        G6_MH_Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        // 2. Evaluamos campo por campo. Si el DTO no lo trae, NO TOCAMOS el valor que ya tenía la BD
        if (perfilDTO.getNombre() != null) {
            if (perfilDTO.getNombre().trim().isEmpty()) {
                throw new RuntimeException("El nombre no puede estar vacío.");
            }
            usuario.setNombre(perfilDTO.getNombre()); // Solo se altera si vino en el JSON
        }

        if (perfilDTO.getEdad() != null) {
            if (perfilDTO.getEdad() < 12 || perfilDTO.getEdad() > 120) {
                throw new RuntimeException("Por favor, ingrese una edad válida (entre 12 y 120 años).");
            }
            usuario.setEdad(perfilDTO.getEdad()); // Solo se altera si vino en el JSON
        }

        if (perfilDTO.getGenero() != null) {
            if (perfilDTO.getGenero().trim().isEmpty()) {
                throw new RuntimeException("El género no puede estar vacío.");
            }
            usuario.setGenero(perfilDTO.getGenero()); // Solo se altera si vino en el JSON
        }

        // 3. Al guardar esta misma instancia, Hibernate mantendrá intactos el nombre, contraseña, fecha, etc.
        usuarioRepository.save(usuario);
    }
}