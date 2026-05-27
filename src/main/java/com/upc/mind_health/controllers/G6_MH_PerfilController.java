package com.upc.mind_health.controllers;

import com.upc.mind_health.dtos.G6_MH_PerfilDTO;
import com.upc.mind_health.services.G6_MH_UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuario")
@CrossOrigin(origins = "*")
@Tag(name = "Perfil de Usuario", description = "Endpoints protegidos para la gestión y edición del perfil del usuario autenticado")
public class G6_MH_PerfilController {

    @Autowired
    private G6_MH_UsuarioService usuarioService;

    // GET: /api/usuario/perfil (HU-04 - Escenario 1)
    @GetMapping("/perfil")
    public ResponseEntity<?> verPerfil(Authentication authentication) {
        try {
            // Extrae la identidad (correo) de forma segura desde el Token JWT
            String correo = authentication.getName();
            G6_MH_PerfilDTO perfil = usuarioService.obtenerPerfil(correo);
            return new ResponseEntity<>(perfil, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    // PATCH: /api/usuario/perfil (HU-04 - Escenario 2)
    @PatchMapping("/editar-perfil")
    public ResponseEntity<?> editarPerfil(Authentication authentication, @RequestBody G6_MH_PerfilDTO perfilDTO) {
        try {
            String correo = authentication.getName();

            // Llama a la lógica centralizada en el Service (donde se evalúan los nulos, vacíos y rangos de edad)
            usuarioService.actualizarPerfil(correo, perfilDTO);

            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Información de perfil actualizada correctamente.");
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}