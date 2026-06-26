package com.upc.mind_health.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.G6_MH_MetaBienestarRequestDTO;
import com.upc.mind_health.entities.G6_MH_ContenidoTerapeutico;
import com.upc.mind_health.entities.G6_MH_MetaBienestar;
import com.upc.mind_health.services.G6_MH_BienestarCatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bienestar-catalogo")
@RequiredArgsConstructor
@Tag(name = "Módulo de Catálogo Terapéutico y Hábitos Diarios", description = "Endpoints para la categorización de ejercicios de relajación y la administración completa del registro de metas de salud")
@CrossOrigin(origins = "*")
public class G6_MH_BienestarCatalogoController {

    private final G6_MH_BienestarCatalogoService bienestarService;

    // HU-42 Escenario 1 y 2
    @Operation(summary = "Filtrar el catálogo de ejercicios de relajación según su categoría emocional (Ansiedad, Insomnio, Estrés)")
    @GetMapping("/ejercicios/filtrar")
    public ResponseEntity<List<G6_MH_ContenidoTerapeutico>> filtrarEjercicios(@RequestParam String categoria) {
        return ResponseEntity.ok(bienestarService.filtrarEjerciciosPorCategoria(categoria));
    }

    // HU-43 Escenario 1
    @Operation(summary = "Registrar una nueva pequeña meta diaria de bienestar en estado pendiente")
    @PostMapping("/metas")
    public ResponseEntity<G6_MH_MetaBienestar> agregarMeta(@RequestBody G6_MH_MetaBienestarRequestDTO dto) {
        return ResponseEntity.ok(bienestarService.registrarMetaDiaria(dto));
    }

    // HU-43 Escenario 2
    @Operation(summary = "Actualizar el estado de una meta (Completada / Pendiente)")
    @PutMapping("/metas/cambiar-estado/{idMeta}")
    public ResponseEntity<String> cambiarEstadoMeta(@PathVariable Long idMeta, @RequestParam boolean completada) {
        return ResponseEntity.ok(bienestarService.conmutarEstadoMeta(idMeta, completada));
    }

    // HU-43 Escenario 3
    @Operation(summary = "Eliminar definitivamente un registro de meta del historial diario")
    @DeleteMapping("/metas/eliminar/{idMeta}")
    public ResponseEntity<String> eliminarMeta(@PathVariable Long idMeta) {
        return ResponseEntity.ok(bienestarService.eliminarMetaDiaria(idMeta));
    }

    // HU-43: Listar
    @Operation(summary = "Listar todas las metas del usuario ordenadas cronológicamente")
    @GetMapping("/metas/usuario/{idUsuario}")
    public ResponseEntity<List<G6_MH_MetaBienestar>> listarMetas(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(bienestarService.listarMetasUsuario(idUsuario));
    }
}