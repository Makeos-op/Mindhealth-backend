package com.upc.mind_health.controllers;

import com.upc.mind_health.dtos.G6_MH_UsuarioRegistroDTO;
import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.services.G6_MH_UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Usuarios y Autenticación", description = "Endpoints para el registro y activación de cuentas (HU-01)")
public class G6_MH_UsuarioController {

    @Autowired
    private G6_MH_UsuarioService usuarioService;

    // POST: /api/auth/registro (Escenario 1)
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody G6_MH_UsuarioRegistroDTO request) {
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
}