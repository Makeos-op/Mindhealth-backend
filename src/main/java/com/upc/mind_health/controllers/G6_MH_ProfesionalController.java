package com.upc.mind_health.controllers;

import com.upc.mind_health.dtos.*;
import com.upc.mind_health.services.G6_MH_IaTerapiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/terapia-ia/profesional")
@RequiredArgsConstructor
public class G6_MH_ProfesionalController {

    private final G6_MH_IaTerapiaService iaTerapiaService;

    // HU-11 & HU-12 Escenario 1: Bandeja de alertas críticas asignadas al psicólogo logueado
    @GetMapping("/mis-alertas")
    public ResponseEntity<List<G6_MH_CasoCriticoResponseDTO>> listarAlertas(Principal principal) {
        return ResponseEntity.ok(iaTerapiaService.listarAlertasParaPsicologo(principal.getName()));
    }

    // HU-12 Escenario 2: Registrar atención de crisis y cerrar caso
    @PutMapping("/atender-alerta/{idDerivacion}")
    public ResponseEntity<?> registrarAtencionCrisis(
            @PathVariable Long idDerivacion,
            @RequestBody G6_MH_AtencionCrisisRequestDTO requestDTO,
            Principal principal) {
        try {
            var resultado = iaTerapiaService.atenderYFecharCrisis(idDerivacion, principal.getName(), requestDTO.getNotasSeguimiento());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al procesar la atención: " + e.getMessage()));
        }
    }

    // HU-13 Escenario 1: Solicitar la colaboración/coordinación de otro terapeuta
    @PostMapping("/coordinar/solicitar")
    public ResponseEntity<G6_MH_CoordinacionResponseDTO> crearSolicitudCoordinacion(
            @RequestBody G6_MH_CoordinacionRequestDTO requestDTO, Principal principal) {
        return ResponseEntity.ok(iaTerapiaService.solicitarCoordinacionCaso(principal.getName(), requestDTO));
    }

    // HU-13 Escenarios 2 y 3: Aceptar o rechazar una solicitud de colaboración recibida
    @PutMapping("/coordinar/responder/{idColaboracion}")
    public ResponseEntity<G6_MH_CoordinacionResponseDTO> responderSolicitudCoordinacion(
            @PathVariable Long idColaboracion,
            @RequestParam boolean acepta,
            @RequestParam(required = false) String notas,
            Principal principal) {
        return ResponseEntity.ok(iaTerapiaService.gestionarRespuestaColaboracion(idColaboracion, principal.getName(), acepta, notas));
    }
}