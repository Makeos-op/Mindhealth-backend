package com.upc.mind_health.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.G6_MH_ContenidoTerapeutico;
import com.upc.mind_health.services.G6_MH_UsuarioSesionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sesiones-usuario")
@RequiredArgsConstructor
@Tag(name = "Módulo de Sesiones, Accesos y Favoritos", description = "Endpoints para el control del ciclo de vida de la autenticación y personalización de la biblioteca de recursos del paciente")
@CrossOrigin(origins = "*")
public class G6_MH_UsuarioSesionController {

    private final G6_MH_UsuarioSesionService usuarioSesionService;

    // HU-37 Escenario 1
    @Operation(summary = "Almacenar un ejercicio terapéutico en la lista de favoritos del usuario")
    @PostMapping("/favoritos/agregar")
    public ResponseEntity<String> agregarFavorito(@RequestBody G6_MH_FavoritoRequestDTO dto) {
        return ResponseEntity.ok(usuarioSesionService.guardarEjercicioFavorito(dto));
    }

    // HU-37 Escenario 2
    @Operation(summary = "Visualizar el listado completo de ejercicios previamente guardados por el usuario")
    @GetMapping("/favoritos/listar/{idUsuario}")
    public ResponseEntity<List<G6_MH_ContenidoTerapeutico>> listarFavoritos(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(usuarioSesionService.obtenerFavoritosUsuario(idUsuario));
    }

    // HU-36 Escenario 1
    @Operation(summary = "Finalizar la sesión del usuario de forma segura")
    @PostMapping("/cerrar-sesion/{idUsuario}")
    public ResponseEntity<String> cerrarSesion(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(usuarioSesionService.finalizarSesionToken(idUsuario));
    }
}