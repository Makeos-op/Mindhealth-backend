package com.upc.mind_health.controllers;

import com.upc.mind_health.dtos.G6_MH_ChatRequestDTO;
import com.upc.mind_health.dtos.G6_MH_ChatResponseDTO;
import com.upc.mind_health.services.G6_MH_IaTerapiaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/terapia-ia")
@CrossOrigin(origins = "*")
@Tag(name = "Monitoreo Emocional con IA Real", description = "Endpoints de análisis cognitivo y soporte en tiempo real potenciados por Google Gemini")
public class G6_MH_IaTerapiaController {

    @Autowired
    private G6_MH_IaTerapiaService iaTerapiaService;

    // POST: /api/terapia-ia/analizar (HU-05 - Escenarios 1, 2 y 3)
    @PostMapping("/analizar/{idSesion}")
    public ResponseEntity<?> analizarTextoSesion(
            @PathVariable Long idSesion,
            @RequestBody G6_MH_ChatRequestDTO requestDTO) {
        try {
            // Le pasamos el texto y el ID de la sesión al servicio
            G6_MH_ChatResponseDTO resultado = iaTerapiaService.procesarSesionRealConIA(requestDTO.getTextoUsuario(), idSesion);
            return new ResponseEntity<>(resultado, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}