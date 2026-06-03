package com.upc.mind_health.controllers;

import com.upc.mind_health.dtos.G6_MH_PrivacidadRequestDTO;
import com.upc.mind_health.dtos.G6_MH_PrivacidadResponseDTO;
import com.upc.mind_health.services.G6_MH_PrivacidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/privacidad")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Privacidad Personalizada", description = "Endpoints seguros para la gestión del consentimiento")
public class G6_MH_PrivacidadController {

    private final G6_MH_PrivacidadService privacidadService;

    //Obtener los ajustes de privacidad del usuario autenticado de forma automática
    // GET /api/privacidad/mi-configuracion ->
    @GetMapping("/mi-configuracion")
    public ResponseEntity<G6_MH_PrivacidadResponseDTO> obtenerAjustes(Principal principal) {
        // principal.getName() nos da el correo de quien inició sesión de forma 100% segura
        String correo = principal.getName();
        // Asumiendo que adaptas obtenerConfiguracion en el servicio para buscar por correo igual que el guardar
        return ResponseEntity.ok(privacidadService.obtenerConfiguracionPorCorreo(correo));
    }

    //Actualizar preferencias de privacidad basadas estrictamente en la sesión activa
    // PUT /api/privacidad -> Escenario 1
    @PutMapping
    public ResponseEntity<G6_MH_PrivacidadResponseDTO> actualizarPrivacidad(
            @RequestBody G6_MH_PrivacidadRequestDTO dto,
            Principal principal) { // 🌟 Inyección automática de la sesión de Spring Security

        String correoUsuario = principal.getName(); // Extrae el correo del JWT
        return ResponseEntity.ok(privacidadService.guardarPreferencias(dto, correoUsuario));
    }
}