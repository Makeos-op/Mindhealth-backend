package com.upc.mind_health.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.G6_MH_NotaPersonalRequestDTO;
import com.upc.mind_health.entities.G6_MH_NotaPersonal;
import com.upc.mind_health.services.G6_MH_ReporteNotaService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bienestar-analitica")
@RequiredArgsConstructor
@Tag(name = "Módulo de Bitácora Personal y Exportación Analítica", description = "Endpoints para la descarga de consolidados evolutivos semanales en PDF y administración de notas íntimas en el Muro de Bienestar")
@CrossOrigin(origins = "*")
public class G6_MH_ReporteNotaController {

    private final G6_MH_ReporteNotaService bienestarService;

    // HU-40 Escenarios 1 y 2
    @Operation(summary = "Descargar un resumen analítico estructurado de la actividad emocional de la semana en formato PDF")
    @GetMapping("/resumen-semanal/descargar/{idUsuario}")
    public ResponseEntity<byte[]> descargarResumen(@PathVariable Long idUsuario) {
        byte[] pdfBytes = bienestarService.exportarResumenSemanalPdf(idUsuario);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("resumen-semanal-" + idUsuario + ".pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // HU-41 Escenario 1
    @Operation(summary = "Persistir una nueva nota personal de pensamientos en el Muro de Bienestar")
    @PostMapping("/notas")
    public ResponseEntity<String> guardarNota(@RequestBody G6_MH_NotaPersonalRequestDTO dto) {
        return ResponseEntity.ok(bienestarService.crearNotaPersonal(dto));
    }

    // HU-41 Escenario 2
    @Operation(summary = "Recuperar el historial completo de notas personales ordenadas de forma cronológica descendente")
    @GetMapping("/notas/historial/{idUsuario}")
    public ResponseEntity<List<G6_MH_NotaPersonal>> listarNotas(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(bienestarService.obtenerHistorialNotas(idUsuario));
    }
}