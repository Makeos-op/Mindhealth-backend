package com.upc.mind_health.controllers;

import com.upc.mind_health.entities.G6_MH_Usuario;
import com.upc.mind_health.services.G6_MH_UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permite la conexión con tu Frontend de Angular/React
public class G6_MH_UsuarioController {

    @Autowired
    private G6_MH_UsuarioService usuarioService;

    // POST: /api/auth/registro (Escenario 1)
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody G6_MH_Usuario usuario) {
        try {
            G6_MH_Usuario registrado = usuarioService.registrarUsuario(usuario);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Registro exitoso. Se ha enviado un correo de confirmación."); // <-- SOLUCIÓN
            respuesta.put("usuarioId", registrado.getIdUsuario()); // <-- SOLUCIÓN
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
            if (verificado) {
                // Redirige automáticamente al usuario a la interfaz de login del Frontend
                // Cambia "http://localhost:4200/login" por la URL real de tu frontend
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("http://localhost:4200/login?verificado=true"))
                        .build();
            }
            return new ResponseEntity<>("No se pudo verificar la cuenta.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Si el token falló o expiró, redirige o muestra el error
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
