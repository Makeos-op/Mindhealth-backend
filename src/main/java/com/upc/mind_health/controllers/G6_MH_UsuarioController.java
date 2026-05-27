package com.upc.mind_health.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.services.G6_MH_UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/verificar/{token}")
    @Operation(summary = "HU01 - Activar cuenta mediante token de verificación")
    public ResponseEntity<String> verificar(@PathVariable String token) {
        String resultado = usuarioService.verificarCuenta(token);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/login")
    @Operation(summary = "HU02 - Inicio de sesión de usuario registrado")
    public ResponseEntity<G6_MH_AuthResponseDTO> login(@RequestBody G6_MH_LoginDTO dto) {
        G6_MH_AuthResponseDTO response = usuarioService.login(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recuperar-password")
    @Operation(summary = "HU03 - Solicitar recuperación de contraseña")
    public ResponseEntity<String> recuperarPassword(@RequestBody G6_MH_RecuperarPasswordDTO dto) {
        String resultado = usuarioService.solicitarRecuperacion(dto.getCorreo());
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "HU03 - Restablecer contraseña con token")
    public ResponseEntity<String> resetPassword(@RequestBody G6_MH_ResetPasswordDTO dto) {
        String resultado = usuarioService.resetPassword(dto);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "HU04 - Ver perfil del usuario")
    public ResponseEntity<G6_MH_PerfilResponseDTO> obtenerPerfil(@PathVariable Long id) {
        G6_MH_PerfilResponseDTO perfil = usuarioService.obtenerPerfil(id);
        return ResponseEntity.ok(perfil);
    }

    @PutMapping("/{id}")
    @Operation(summary = "HU04 - Editar perfil del usuario")
    public ResponseEntity<G6_MH_PerfilResponseDTO> actualizarPerfil(
            @PathVariable Long id,
            @RequestBody G6_MH_PerfilUpdateDTO dto) {
        G6_MH_PerfilResponseDTO perfil = usuarioService.actualizarPerfil(id, dto);
        return ResponseEntity.ok(perfil);
    }
}
