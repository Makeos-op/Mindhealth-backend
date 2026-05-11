package com.upc.mind_health.controllers;

import com.upc.mind_health.dtos.G6_MH_UsuarioRegistroDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.G6_MH_PrivacidadDTO;
import com.upc.mind_health.services.G6_MH_UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.upc.mind_health.dtos.G6_MH_UsuarioPerfilDTO;
import com.upc.mind_health.dtos.G6_MH_UsuarioActualizarDTO;
@RestController
@RequestMapping("/tp/mhg6/mhg6/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios - HU01, HU02, HU03, HU04")
public class G6_MH_UsuarioController {

    private final G6_MH_UsuarioService usuarioService;

    @PostMapping("/registro")
    @Operation(summary = "HU01 - Registrar nuevo usuario con verificación de cuenta")
    public ResponseEntity<String> registrar(@RequestBody G6_MH_UsuarioRegistroDTO dto) {
        String resultado = usuarioService.registrar(dto);
        return ResponseEntity.ok(resultado);
    }
    @GetMapping("/{id}")
    @Operation(summary = "HU04 - Ver perfil del usuario")
    public ResponseEntity<G6_MH_UsuarioPerfilDTO> obtenerPerfil(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "HU04 - Actualizar información del perfil del usuario")
    public ResponseEntity<G6_MH_UsuarioPerfilDTO> actualizarPerfil(
            @PathVariable Long id,
            @RequestBody G6_MH_UsuarioActualizarDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(id, dto));
    }

    @GetMapping("/verificar/{token}")
    @Operation(summary = "HU01 - Activar cuenta mediante token de verificación")
    public ResponseEntity<String> verificar(@PathVariable String token) {
        String resultado = usuarioService.verificarCuenta(token);
        return ResponseEntity.ok(resultado);
    }
}