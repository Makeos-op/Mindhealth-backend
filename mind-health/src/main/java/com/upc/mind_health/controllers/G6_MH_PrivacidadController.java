package com.upc.mind_health.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.G6_MH_PrivacidadDTO;
import com.upc.mind_health.services.G6_MH_PrivacidadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tp/mhg6/mhg6/privacidad")
@RequiredArgsConstructor
@Tag(name = "Privacidad", description = "Configuración de privacidad personalizada - HU07")
public class G6_MH_PrivacidadController {

    private final G6_MH_PrivacidadService privacidadService;

    @PostMapping("/{idUsuario}")
    @Operation(summary = "HU07 - Guardar configuración de privacidad del usuario")
    public ResponseEntity<G6_MH_PrivacidadDTO> guardarPrivacidad(
            @PathVariable Long idUsuario,
            @RequestBody G6_MH_PrivacidadDTO dto) {
        G6_MH_PrivacidadDTO resultado = privacidadService.guardarPrivacidad(idUsuario, dto);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{idUsuario}")
    @Operation(summary = "HU07 - Obtener configuración de privacidad del usuario")
    public ResponseEntity<G6_MH_PrivacidadDTO> obtenerPrivacidad(@PathVariable Long idUsuario) {
        G6_MH_PrivacidadDTO resultado = privacidadService.obtenerPrivacidad(idUsuario);
        return ResponseEntity.ok(resultado);
    }
}
