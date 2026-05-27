package com.upc.mind_health.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.G6_MH_DiarioRequestDTO;
import com.upc.mind_health.dtos.G6_MH_DiarioResponseDTO;
import com.upc.mind_health.services.G6_MH_DiarioPersonalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tp/mhg6/mhg6/diario")
@RequiredArgsConstructor
@Tag(name = "Diario Personal", description = "Gestión de notas personales - HU41")
public class G6_MH_DiarioPersonalController {

    private final G6_MH_DiarioPersonalService diarioPersonalService;

    @PostMapping
    @Operation(summary = "HU41 - Crear nueva nota personal")
    public ResponseEntity<G6_MH_DiarioResponseDTO> crearNota(@RequestBody G6_MH_DiarioRequestDTO dto) {
        G6_MH_DiarioResponseDTO response = diarioPersonalService.crearNota(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{idUsuario}")
    @Operation(summary = "HU41 - Obtener historial de notas personales del usuario")
    public ResponseEntity<List<G6_MH_DiarioResponseDTO>> obtenerNotas(@PathVariable Long idUsuario) {
        List<G6_MH_DiarioResponseDTO> notas = diarioPersonalService.obtenerNotas(idUsuario);
        return ResponseEntity.ok(notas);
    }
}
