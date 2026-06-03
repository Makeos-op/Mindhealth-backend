package com.upc.mind_health.controllers;

import com.upc.mind_health.dtos.G6_MH_ProgresoEmocionalDTO;
import com.upc.mind_health.dtos.G6_MH_AlertaResponseDTO;
import com.upc.mind_health.services.G6_MH_AnalisisEmocionalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analisis-emocional")
@CrossOrigin(origins = "*")
@Tag(name = "Análisis Emocional e Inteligencia Predictiva", description = "Endpoints analíticos para el monitoreo de patrones crónicos y alertas automáticas")
public class G6_MH_AnalisisEmocionalController {

    @Autowired
    private G6_MH_AnalisisEmocionalService analisisService;

    // GET: /api/analisis-emocional/evolucion/{idUsuario} -> Escenario 1
    @GetMapping("/evolucion/{idUsuario}")
    public ResponseEntity<List<G6_MH_ProgresoEmocionalDTO>> obtenerEvolucion(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(analisisService.obtenerEvolucionEmocional(idUsuario));
    }

    // GET: /api/analisis-emocional/alertas/{idUsuario} -> Escenarios 2, 3, 4 y 5
    @GetMapping("/alertas/{idUsuario}")
    public ResponseEntity<G6_MH_AlertaResponseDTO> revisarAlertasYPatrones(@PathVariable Long idUsuario) {
        G6_MH_AlertaResponseDTO alerta = analisisService.evaluarPatronesYGenerarAlerta(idUsuario);
        if (alerta == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(alerta);
    }
}