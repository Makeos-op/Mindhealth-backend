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

    //1 Al pulsar el botón "Hablar con MindBot" en el Frontend.
    @PostMapping("/sesion/inicializar/{idUsuario}")
    public ResponseEntity<Long> inicializarChat(@PathVariable Long idUsuario) {
        Long idSesion = iaTerapiaService.obtenerOCrearSesionActiva(idUsuario);
        return ResponseEntity.ok(idSesion);
    }

    //2 El flujo continuo del chat (Cada burbuja de texto que envía el paciente)
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

    //3 Al pulsar el botón "Finalizar Sesión" en el Frontend
    @PutMapping("/sesion/finalizar/{idSesion}")
    public ResponseEntity<String> finalizarChat(@PathVariable Long idSesion) {
        try {
            iaTerapiaService.finalizarSesionTerapia(idSesion);
            return ResponseEntity.ok("Sesión finalizada con éxito e historial archivado de forma segura.");
        } catch (Exception e) {
            return new ResponseEntity<>("Error al finalizar sesión: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/historial-seguro")
    public ResponseEntity<?> consultarHistorialSeguro(java.security.Principal principal) {
        try {
            var historial = iaTerapiaService.obtenerHistorialSesionesSeguras(principal.getName());
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al recuperar el historial seguro: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}