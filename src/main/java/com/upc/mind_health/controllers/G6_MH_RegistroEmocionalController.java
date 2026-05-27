package com.upc.mind_health.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.G6_MH_RegistroEmocionalRequestDTO;
import com.upc.mind_health.dtos.G6_MH_RegistroEmocionalResponseDTO;
import com.upc.mind_health.services.G6_MH_RegistroEmocionalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tp/com.upc.mind_health/com.upc.mind_health/emociones")
@RequiredArgsConstructor
@Tag(name = "Emociones", description = "Registro de emociones diarias - HU15")
public class G6_MH_RegistroEmocionalController {

    private final G6_MH_RegistroEmocionalService registroEmocionalService;

    @PostMapping
    @Operation(summary = "HU15 - Registrar emoción diaria del usuario")
    public ResponseEntity<G6_MH_RegistroEmocionalResponseDTO> registrarEmocion(
            @RequestBody G6_MH_RegistroEmocionalRequestDTO dto) {
        G6_MH_RegistroEmocionalResponseDTO response = registroEmocionalService.registrarEmocion(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{idUsuario}")
    @Operation(summary = "HU15 - Obtener historial emocional del usuario")
    public ResponseEntity<List<G6_MH_RegistroEmocionalResponseDTO>> obtenerHistorial(
            @PathVariable Long idUsuario) {
        List<G6_MH_RegistroEmocionalResponseDTO> historial = registroEmocionalService.obtenerHistorial(idUsuario);
        return ResponseEntity.ok(historial);
    }
}
