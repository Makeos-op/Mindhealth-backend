package com.upc.mind_health.controllers;

import com.upc.mind_health.dtos.G6_MH_ChatRequestDTO;
import com.upc.mind_health.dtos.G6_MH_ChatResponseDTO;
import com.upc.mind_health.dtos.G6_MH_CasoCriticoResponseDTO;
import com.upc.mind_health.dtos.G6_MH_HistorialSeguroResponseDTO;
import com.upc.mind_health.services.G6_MH_IaTerapiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/terapia-ia")
@CrossOrigin(origins = "*")
@Tag(name = "Monitoreo Emocional con IA Real", description = "Endpoints de análisis cognitivo y soporte en tiempo real potenciados por Google Gemini")
public class G6_MH_IaTerapiaController {

    @Autowired
    private G6_MH_IaTerapiaService iaTerapiaService;

    //Inicializar o recuperar la sesión activa del paciente
    // 1. Al pulsar el botón "Hablar con MindBot" en el Frontend.
    @PostMapping("/sesion/inicializar/{idUsuario}")
    public ResponseEntity<Long> inicializarChat(@PathVariable Long idUsuario) {
        Long idSesion = iaTerapiaService.obtenerOCrearSesionActiva(idUsuario);
        return ResponseEntity.ok(idSesion);
    }

    // 2. El flujo continuo del chat (Cada burbuja de texto que envía el paciente)
    //Si el resultado de este análisis es CRÍTICO, el servicio dispara la derivación automática
    @PostMapping("/analizar/{idSesion}")
    public ResponseEntity<?> analizarTextoSesion(
            @PathVariable Long idSesion,
            @RequestBody G6_MH_ChatRequestDTO requestDTO) {
        try {
            G6_MH_ChatResponseDTO resultado = iaTerapiaService.procesarSesionRealConIA(requestDTO.getTextoUsuario(), idSesion);
            return new ResponseEntity<>(resultado, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    //Finalizar la sesión de chat actual
    // 3. Al pulsar el botón "Finalizar Sesión" en el Frontend
    @PutMapping("/sesion/finalizar/{idSesion}")
    public ResponseEntity<String> finalizarChat(@PathVariable Long idSesion) {
        try {
            iaTerapiaService.finalizarSesionTerapia(idSesion);
            return ResponseEntity.ok("Sesión finalizada con éxito e historial archivado de forma segura.");
        } catch (Exception e) {
            return new ResponseEntity<>("Error al finalizar sesión: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //HU-10: Escenario 2: Confirmación de seguridad al usuario
    @GetMapping("/paciente/historial-seguro/{idUsuario}")
    public ResponseEntity<List<G6_MH_HistorialSeguroResponseDTO>> obtenerHistorialSeguro(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(iaTerapiaService.obtenerHistorialSesionesSegurasReal(idUsuario));
    }

    //HU-11: Para que el Profesional consulte sus alertas asignadas de forma automática
    //Escenario 2 - Listar los casos críticos derivados automáticamente al profesional activo
    @GetMapping("/profesional/mis-alertas")
    public ResponseEntity<?> obtenerAlertasAsignadas(Principal principal) {
        try {
            List<G6_MH_CasoCriticoResponseDTO> alertas = iaTerapiaService.listarAlertasParaPsicologo(principal.getName());
            return ResponseEntity.ok(alertas);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al recuperar alertas del profesional: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    // HU-12 Escenario 2: Procesar atención y cierre de caso por parte del Profesional
    @PutMapping("/profesional/atender-alerta/{idDerivacion}")
    public ResponseEntity<?> registrarAtencionCrisis(
            @PathVariable Long idDerivacion,
            @RequestBody com.upc.mind_health.dtos.G6_MH_AtencionCrisisRequestDTO requestDTO,
            Principal principal) {
        try {
            var resultado = iaTerapiaService.atenderYFecharCrisis(idDerivacion, principal.getName(), requestDTO.getNotasSeguimiento());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al procesar la atención: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}