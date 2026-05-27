package com.upc.mind_health.controllers;

import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.repositories.G6_MH_UsuarioRepository;
import com.upc.mind_health.security.G6_MH_AuthResponseDTO;
import com.upc.mind_health.services.G6_MH_UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import com.upc.mind_health.security.G6_MH_JwtUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Usuarios y Autenticación", description = "Endpoints para el registro y activación de cuentas")
public class G6_MH_UsuarioController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private G6_MH_UsuarioRepository usuarioRepository;

    @Autowired
    private G6_MH_JwtUtil jwtUtil;

    @Autowired
    private G6_MH_UsuarioService usuarioService;

    // POST: /api/auth/registro (Escenario 1)
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody G6_MH_UsuarioRegistroDTO request) {
        try {
            G6_MH_Usuario nuevoUsuario = new G6_MH_Usuario();
            nuevoUsuario.setNombre(request.getNombre());
            nuevoUsuario.setCorreo(request.getCorreo());
            nuevoUsuario.setContrasena(request.getContrasena());
            nuevoUsuario.setEdad(request.getEdad());
            nuevoUsuario.setGenero(request.getGenero());

            G6_MH_Usuario registrado = usuarioService.registrarUsuario(nuevoUsuario);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Registro exitoso. Se ha enviado un correo de confirmación.");
            respuesta.put("usuarioId", registrado.getIdUsuario());
            respuesta.put("correo", registrado.getCorreo());

            return new ResponseEntity<>(respuesta, HttpStatus.CREATED);

        } catch (Exception e) {
            Map<String, String> errorRespuesta = new HashMap<>();
            errorRespuesta.put("error", e.getMessage());
            return new ResponseEntity<>(errorRespuesta, HttpStatus.BAD_REQUEST);
        }
    }

    // GET: /api/auth/verificar?token=xyz (Escenario 2)
    @GetMapping("/verificar")
    public ResponseEntity<?> verificarCuenta(@RequestParam("token") String token) {
        try {
            boolean verificado = usuarioService.verificarCuenta(token);

            Map<String, String> respuesta = new HashMap<>();
            if (verificado) {
                respuesta.put("mensaje", "Cuenta activada con éxito. Ya puede iniciar sesión en la plataforma.");
                respuesta.put("estado", "ACTIVO");
                return new ResponseEntity<>(respuesta, HttpStatus.OK); // <--- Cambiado a 200 OK con JSON
            }

            respuesta.put("error", "No se pudo verificar la cuenta.");
            return new ResponseEntity<>(respuesta, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            Map<String, String> errorRespuesta = new HashMap<>();
            errorRespuesta.put("error", e.getMessage());
            return new ResponseEntity<>(errorRespuesta, HttpStatus.BAD_REQUEST);
        }
    }

    // POST: /api/auth/login (HU-02 - Escenarios 1 y 2)
    @PostMapping("/login")
    public ResponseEntity<?> login(@jakarta.validation.Valid @RequestBody G6_MH_LoginDTO loginDTO) {
        try {
            // ESCENARIO 1: Spring Security valida automáticamente correo y contraseña (hash BCrypt)
            org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            loginDTO.getCorreo(),
                            loginDTO.getContrasena()
                    )
            );

            // Si pasa la línea anterior, las credenciales son válidas. Obtenemos los datos del usuario de la sesión.
            org.springframework.security.core.userdetails.User userDetails =
                    (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

            // Buscamos el usuario completo en la BD para sacar su ID y Nombre real para tu AuthResponseDTO
            G6_MH_Usuario usuario = usuarioRepository.findByCorreo(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado post-autenticación."));

            // Generamos el token JWT usando tu JwtUtil
            String tokenJwt = jwtUtil.generateToken(usuario.getCorreo());

            // Construimos tu G6_MH_AuthResponseDTO usando el Builder de Lombok
            G6_MH_AuthResponseDTO respuesta = G6_MH_AuthResponseDTO.builder()
                    .token(tokenJwt)
                    .mensaje("Inicio de sesión exitoso. ¡Bienvenido(a) a Mind Health!")
                    .idUsuario(usuario.getIdUsuario())
                    .nombre(usuario.getNombre())
                    .correo(usuario.getCorreo())
                    .build();

            return new ResponseEntity<>(respuesta, HttpStatus.OK);

        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // ESCENARIO 2: Si las credenciales no coinciden, salta esta excepción automática
            Map<String, String> errorRespuesta = new HashMap<>();
            errorRespuesta.put("error", "Las credenciales ingresadas no son válidas. Verifique su correo o contraseña.");
            return new ResponseEntity<>(errorRespuesta, HttpStatus.UNAUTHORIZED); // 401 Unauthorized

        } catch (org.springframework.security.authentication.DisabledException e) {
            // Extra por seguridad (HU-01): Cuenta registrada pero que no ha sido verificada en el correo
            Map<String, String> errorRespuesta = new HashMap<>();
            errorRespuesta.put("error", "Esta cuenta no se encuentra activa. Revise su correo para verificarla.");
            return new ResponseEntity<>(errorRespuesta, HttpStatus.FORBIDDEN); // 403 Forbidden

        } catch (Exception e) {
            Map<String, String> errorRespuesta = new HashMap<>();
            errorRespuesta.put("error", e.getMessage());
            return new ResponseEntity<>(errorRespuesta, HttpStatus.BAD_REQUEST);
        }
    }

    // POST: /api/auth/solicitar-recuperacion (HU-03 - Escenario 1)
    @PostMapping("/solicitar-recuperacion")
    public ResponseEntity<?> solicitarRecuperacion(@RequestBody G6_MH_RecuperarPasswordDTO dto) {
        try {
            // Usamos el .getCorreo() de tu DTO
            usuarioService.solicitarRecuperacion(dto.getCorreo());

            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Se han enviado las instrucciones de recuperación al correo proporcionado.");
            return new ResponseEntity<>(respuesta, HttpStatus.OK);

        } catch (Exception e) {
            Map<String, String> errorRespuesta = new HashMap<>();
            errorRespuesta.put("error", e.getMessage());
            return new ResponseEntity<>(errorRespuesta, HttpStatus.BAD_REQUEST);
        }
    }

    // POST: /api/auth/restablecer (HU-03 - Escenario 2)
    @PostMapping("/restablecer")
    public ResponseEntity<?> restablecerContrasena(@RequestBody G6_MH_ResetPasswordDTO dto) {
        try {
            // Usamos el .getToken() y .getNuevaContrasena() de tu DTO
            usuarioService.restablecerContrasena(dto.getToken(), dto.getNuevaContrasena());

            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Contraseña actualizada con éxito. Ya puede iniciar sesión con su nueva clave.");
            return new ResponseEntity<>(respuesta, HttpStatus.OK);

        } catch (Exception e) {
            Map<String, String> errorRespuesta = new HashMap<>();
            errorRespuesta.put("error", e.getMessage());
            return new ResponseEntity<>(errorRespuesta, HttpStatus.BAD_REQUEST);
        }
    }
}