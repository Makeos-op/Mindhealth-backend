package com.upc.mind_health.controllers;

import com.upc.mind_health.dtos.G6_MH_AccesoResponseDTO;
import com.upc.mind_health.dtos.G6_MH_SimulacionLoginDTO;
import com.upc.mind_health.services.G6_MH_SeguridadAccesoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seguridad")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Seguridad de la Cuenta", description = "Endpoints para auditar inicios de sesión y gestionar alertas por accesos sospechosos")
public class G6_MH_SeguridadAccesoController {

    private final G6_MH_SeguridadAccesoService seguridadService;

    //Simular un intento de inicio de sesión para evaluar comportamiento sospechoso
    // POST /api/seguridad/simular-login -> Endpoint de apoyo para gatillar el Escenario 1
    @PostMapping("/simular-login")
    public ResponseEntity<G6_MH_AccesoResponseDTO> registrarLogin(@RequestBody G6_MH_SimulacionLoginDTO dto) {
        return ResponseEntity.ok(seguridadService.procesarIntentoAcceso(dto));
    }

    //HU08 - Visualizar el historial completo de dispositivos y ubicaciones de acceso
    // GET /api/seguridad/historial/{idUsuario} -> Escenario 2
    @GetMapping("/historial/{idUsuario}")
    public ResponseEntity<List<G6_MH_AccesoResponseDTO>> obtenerHistorial(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(seguridadService.listarHistorialAccesos(idUsuario));
    }
}