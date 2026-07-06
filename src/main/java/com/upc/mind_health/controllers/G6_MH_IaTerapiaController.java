package com.upc.mind_health.controllers;

import com.upc.mind_health.dtos.G6_MH_ChatRequestDTO;
import com.upc.mind_health.dtos.G6_MH_ChatResponseDTO;
import com.upc.mind_health.dtos.G6_MH_HistorialSeguroResponseDTO;
import com.upc.mind_health.dtos.G6_MH_ResumenPostSesionDTO;
import com.upc.mind_health.entities.G6_MH_ContenidoTerapeutico;
import com.upc.mind_health.services.G6_MH_ContenidoTerapeuticoService;
import com.upc.mind_health.services.G6_MH_IaTerapiaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/terapia-ia/paciente")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Monitoreo Emocional con IA - Paciente", description = "Endpoints de interacción, análisis y soporte emocional en tiempo real para pacientes")
public class G6_MH_IaTerapiaController {

    private final G6_MH_IaTerapiaService iaTerapiaService;
    private final G6_MH_ContenidoTerapeuticoService contenidoService;

    // Inicializar o recuperar la sesión activa del paciente
    @PostMapping("/sesion/inicializar/{idUsuario}")
    public ResponseEntity<Long> inicializarChat(@PathVariable Long idUsuario) {
        Long idSesion = iaTerapiaService.obtenerOCrearSesionActiva(idUsuario);
        return ResponseEntity.ok(idSesion);
    }

    // El flujo continuo del chat (Cada burbuja de texto que envía el paciente)
    @PostMapping("/analizar/{idSesion}")
    public ResponseEntity<?> analizarTextoSesion(
            @PathVariable Long idSesion,
            @RequestBody G6_MH_ChatRequestDTO requestDTO) {
        try {
            G6_MH_ChatResponseDTO resultado = iaTerapiaService.procesarSesionRealConIA(requestDTO.getTextoUsuario(), idSesion);
            return new ResponseEntity<>(resultado, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    // Finalizar la sesión de chat y archivar de forma segura
    @PutMapping("/sesion/finalizar/{idSesion}")
    public ResponseEntity<?> finalizarChat(@PathVariable Long idSesion) {
        try {
            G6_MH_ResumenPostSesionDTO resumen = iaTerapiaService.finalizarSesionConResumen(idSesion);
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al finalizar sesión: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
    // HU-10 Escenario 2: Confirmación de seguridad e historial para el paciente
    @GetMapping("/historial-seguro/{idUsuario}")
    public ResponseEntity<List<G6_MH_HistorialSeguroResponseDTO>> obtenerHistorialSeguro(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(iaTerapiaService.obtenerHistorialSesionesSegurasReal(idUsuario));
    }

    // HU-38 Escenario 1 y 2
    @DeleteMapping("/sesiones/eliminar/{idSesion}")
    public ResponseEntity<String> eliminarSesion(@PathVariable Long idSesion) {
        return ResponseEntity.ok(iaTerapiaService.eliminarSesionHistorial(idSesion));
    }

    // HU-39 Escenario 2
    @GetMapping("/sugerencias/priorizadas")
    public ResponseEntity<List<G6_MH_ContenidoTerapeutico>> obtenerPriorizadas(
            @RequestParam Long idUsuario, @RequestParam String emocion) {
        return ResponseEntity.ok(contenidoService.obtenerRecomendacionesPriorizadas(idUsuario, emocion));
    }
}