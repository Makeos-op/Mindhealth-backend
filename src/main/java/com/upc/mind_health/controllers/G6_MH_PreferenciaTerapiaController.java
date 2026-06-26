package com.upc.mind_health.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.services.G6_MH_PreferenciaTerapiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferencias-terapia")
@RequiredArgsConstructor
@Tag(name = "Módulo de Configuración y Enfoques Terapéuticos", description = "Endpoints para la gestión, actualización voluntaria y sugerencia algorítmica de corrientes psicológicas (TCC, Mindfulness) basadas en el progreso")
@CrossOrigin(origins = "*")
public class G6_MH_PreferenciaTerapiaController {

    private final G6_MH_PreferenciaTerapiaService preferenciaService;

    // HU-24 & HU-25: Guardar o modificar el método preferido (Soporta null para el registro libre)
    @Operation(summary = "Actualizar o definir el método de terapia preferido del usuario")
    @PutMapping("/actualizar")
    public ResponseEntity<String> actualizarMetodo(@RequestBody G6_MH_PreferenciaTerapiaRequestDTO dto) {
        return ResponseEntity.ok(preferenciaService.actualizarMetodoTerapia(dto));
    }

    // HU-26 Escenarios 1 y 2: Motor de sugerencias basado en promedios numéricos de ánimo
    @Operation(summary = "Obtener sugerencias automatizadas de enfoques terapéuticos según la tendencia de bienestar")
    @GetMapping("/sugerencia-ia/{idUsuario}")
    public ResponseEntity<G6_MH_SugerenciaTerapiaResponseDTO> obtenerSugerenciaIA(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(preferenciaService.evaluarYSugerirMetodo(idUsuario));
    }

    // HU-28 Escenario 1 y 2
    @Operation(summary = "Actualizar la preferencia de estilo de lenguaje de la IA (FORMAL/INFORMAL)")
    @PutMapping("/ajuste-lenguaje")
    public ResponseEntity<String> ajustarLenguajeIA(@RequestBody com.upc.mind_health.dtos.G6_MH_AjusteLenguajeRequestDTO dto) {
        return ResponseEntity.ok(preferenciaService.guardarPreferenciaLenguaje(dto));
    }

    // HU-29 Escenario 2
    @Operation(summary = "Modificar los objetivos personales del perfil del paciente")
    @PutMapping("/objetivos")
    public ResponseEntity<String> actualizarObjetivos(@RequestBody com.upc.mind_health.dtos.G6_MH_ObjetivosRequestDTO dto) {
        return ResponseEntity.ok(preferenciaService.actualizarObjetivosPersonales(dto));
    }
}