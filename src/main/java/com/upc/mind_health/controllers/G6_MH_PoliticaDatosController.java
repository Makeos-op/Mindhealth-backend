package com.upc.mind_health.controllers;

import com.upc.mind_health.dtos.G6_MH_PoliticaResponseDTO;
import com.upc.mind_health.services.G6_MH_PoliticaDatosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/politicas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Políticas de Privacidad", description = "Endpoints para la consulta transparente de las políticas de protección de datos")
public class G6_MH_PoliticaDatosController {

    private final G6_MH_PoliticaDatosService politicaService;

    //HU09 - Consultar la política de privacidad vigente en lenguaje claro
    @GetMapping("/datos-emocionales")
    public ResponseEntity<G6_MH_PoliticaResponseDTO> obtenerPolitica() {
        return ResponseEntity.ok(politicaService.consultarPoliticaVigente());
    }
}