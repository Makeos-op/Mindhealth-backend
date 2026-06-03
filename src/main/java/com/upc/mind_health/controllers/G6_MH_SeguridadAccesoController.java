package com.upc.mind_health.controllers;

import com.upc.mind_health.dtos.G6_MH_AccesoResponseDTO;
import com.upc.mind_health.dtos.G6_MH_SimulacionLoginDTO;
import com.upc.mind_health.services.G6_MH_SeguridadAccesoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/seguridad")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Seguridad de la Cuenta", description = "Endpoints automatizados para auditar inicios de sesión mediante JWT")
public class G6_MH_SeguridadAccesoController {

    private final G6_MH_SeguridadAccesoService seguridadService;

    //Simular un intento de acceso evaluando el contexto geográfico de la sesión activa
    @PostMapping("/simular-login")
    public ResponseEntity<G6_MH_AccesoResponseDTO> registrarLogin(
            @RequestBody G6_MH_SimulacionLoginDTO dto, Principal principal) { // 🌟 Inyección automática
        return ResponseEntity.ok(seguridadService.procesarIntentoAcceso(dto, principal.getName()));
    }

    //Escenario 2 - Visualizar el historial de accesos del usuario autenticado
    @GetMapping("/mis-accesos")
    public ResponseEntity<List<G6_MH_AccesoResponseDTO>> obtenerHistorial(Principal principal) {
        // Asumiendo un método homólogo en el servicio que busque por correo
        return ResponseEntity.ok(seguridadService.listarHistorialAccesosPorCorreo(principal.getName()));
    }
}