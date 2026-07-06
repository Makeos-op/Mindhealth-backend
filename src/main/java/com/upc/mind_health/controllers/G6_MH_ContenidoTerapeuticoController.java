package com.upc.mind_health.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.G6_MH_ContenidoTerapeutico;
import com.upc.mind_health.services.G6_MH_ContenidoTerapeuticoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recursos-terapeuticos")
@RequiredArgsConstructor
@Tag(name = "Módulo de Contenido Terapéutico y Feedback", description = "Endpoints para la sugerencia automática de recursos multimedia basados en el estado emocional y gestión de calificaciones de la comunidad")
@CrossOrigin(origins = "*")
public class G6_MH_ContenidoTerapeuticoController {

    private final G6_MH_ContenidoTerapeuticoService contenidoService;

    // HU-22 Escenarios 1, 2 y 3
    @Operation(summary = "Obtener recomendaciones sugeridas (Videos/Artículos) basadas en el estado emocional de la sesión")
    @GetMapping("/sugerencias/sesion/{idSesion}")
    public ResponseEntity<List<G6_MH_ContenidoTerapeutico>> obtenerRecomendaciones(@PathVariable Long idSesion) {
        return ResponseEntity.ok(contenidoService.obtenerRecomendacionesPorSesion(idSesion));
    }

    // HU-23 Escenario 1
    @Operation(summary = "Calificar de 1 a 5 estrellas y añadir comentarios sobre un recurso visualizado")
    @PostMapping("/calificar")
    public ResponseEntity<String> calificarContenido(@RequestBody G6_MH_CalificacionRequestDTO dto) {
        return ResponseEntity.ok(contenidoService.calificarRecurso(dto));
    }

    // HU-23 Escenario 2
    @Operation(summary = "Visualizar el detalle de un recurso multimedia y el listado de comentarios de otros usuarios")
    @GetMapping("/detalle/{idContenido}")
    public ResponseEntity<G6_MH_ContenidoDetalleDTO> obtenerDetalleConComentarios(@PathVariable Long idContenido) {
        return ResponseEntity.ok(contenidoService.obtenerDetalleContenidoConComentarios(idContenido));
    }

    @Operation(summary = "Obtener sugerencias de contenido alineadas estrictamente a los objetivos personales del usuario")
    @GetMapping("/sugerencias/objetivos/{idUsuario}")
    public ResponseEntity<List<G6_MH_ContenidoTerapeutico>> obtenerPorObjetivos(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(contenidoService.obtenerContenidoPorObjetivosUsuario(idUsuario));
    }

    // HU-37 Escenario 1
    @Operation(summary = "Guardar un recurso terapéutico en la lista de favoritos del usuario")
    @PostMapping("/favoritos")
    public ResponseEntity<String> agregarFavorito(@RequestBody G6_MH_FavoritoRequestDTO dto) {
        return ResponseEntity.ok(contenidoService.agregarFavorito(dto));
    }

    @Operation(summary = "Quitar un recurso terapéutico de la lista de favoritos del usuario")
    @DeleteMapping("/favoritos/{idUsuario}/{idContenido}")
    public ResponseEntity<String> quitarFavorito(@PathVariable Long idUsuario, @PathVariable Long idContenido) {
        return ResponseEntity.ok(contenidoService.quitarFavorito(idUsuario, idContenido));
    }

    // HU-37 Escenario 2
    @Operation(summary = "Listar los recursos terapéuticos guardados como favoritos por el usuario")
    @GetMapping("/favoritos/{idUsuario}")
    public ResponseEntity<List<G6_MH_ContenidoTerapeutico>> listarFavoritos(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(contenidoService.listarFavoritos(idUsuario));
    }
}