package com.upc.mind_health.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.G6_MH_RutinaPreventiva;
import com.upc.mind_health.services.G6_MH_RutinaRecordatorioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prevencion-rutinas")
@RequiredArgsConstructor
@Tag(name = "Módulo de Prevención y Recordatorios Adaptativos", description = "Endpoints para la construcción de planes cotidianos preventivos y flexibilización de alertas horarias")
@CrossOrigin(origins = "*")
public class G6_MH_RutinaRecordatorioController {

    private final G6_MH_RutinaRecordatorioService service;

    // HU-30 Escenarios 1, 3 y 4
    @Operation(summary = "Generar u obtener una rutina personalizada adaptada según los últimos patrones anímicos")
    @PostMapping("/generar/{idUsuario}")
    public ResponseEntity<G6_MH_RutinaPreventiva> generarPlanDiario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.generarRutinaDiaria(idUsuario));
    }

    // HU-30 Escenario 4
    @Operation(summary = "Visualizar el listado histórico de rutinas diarias asignadas al paciente")
    @GetMapping("/historial/{idUsuario}")
    public ResponseEntity<List<G6_MH_RutinaPreventiva>> obtenerHistorial(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.obtenerHistorialRutinas(idUsuario));
    }

    // HU-30 Escenario 5
    @Operation(summary = "Enviar retroalimentación sobre la utilidad práctica de la rutina completada")
    @PutMapping("/evaluar")
    public ResponseEntity<String> evaluarRutina(@RequestBody G6_MH_FeedbackRutinaDTO dto) {
        return ResponseEntity.ok(service.evaluarUtilidadRutina(dto));
    }

    // HU-31 Escenario 1
    @Operation(summary = "Modificar el horario de preferencia y guardar la franja flexible de recordatorios")
    @PutMapping("/recordatorios/horario")
    public ResponseEntity<String> guardarHorarioPreferido(@RequestBody G6_MH_HorarioRecordatorioDTO dto) {
        return ResponseEntity.ok(service.actualizarHorarioPreferido(dto));
    }
}