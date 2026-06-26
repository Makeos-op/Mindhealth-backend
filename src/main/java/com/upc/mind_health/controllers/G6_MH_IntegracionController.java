package com.upc.mind_health.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.services.G6_MH_IntegracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integraciones")
@RequiredArgsConstructor
@Tag(name = "Módulo de Integraciones Externas y Red de Apoyo", description = "Endpoints para la sincronización con Google Calendar, autorizaciones y envío de reportes automatizados por correo")
@CrossOrigin(origins = "*")
public class G6_MH_IntegracionController {

    private final G6_MH_IntegracionService integracionService;

    // HU-32 Escenario 1
    @Operation(summary = "Vincular el servicio de Google Calendar del usuario")
    @PutMapping("/calendario/vincular/{idUsuario}")
    public ResponseEntity<String> vincularCalendario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(integracionService.vincularCalendario(idUsuario));
    }

    // HU-32 Escenario 2
    @Operation(summary = "Crear automáticamente un evento en el calendario vinculado tras guardar una actividad")
    @PostMapping("/calendario/crear-evento")
    public ResponseEntity<String> agendarEvento(@RequestBody G6_MH_ActividadCalendarioDTO dto) {
        return ResponseEntity.ok(integracionService.agendarEventoEnCalendario(dto));
    }

    // HU-33 Escenario 1
    @Operation(summary = "Autorizar un correo electrónico como contacto de confianza de la Red de Apoyo")
    @PostMapping("/red-apoyo/autorizar")
    public ResponseEntity<String> autorizarRedApoyo(@RequestBody G6_MH_RedApoyoRequestDTO dto) {
        return ResponseEntity.ok(integracionService.autorizarContactoApoyo(dto));
    }

    // HU-33 Escenario 2 & HU-34 Escenario 2
    @Operation(summary = "Disparar el envío automático del reporte de avances semanales al contacto de confianza")
    @PostMapping("/red-apoyo/enviar-reporte/{idUsuario}")
    public ResponseEntity<String> enviarReporte(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(integracionService.enviarReporteSemanalRedApoyo(idUsuario));
    }
}