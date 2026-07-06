package com.upc.mind_health.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.services.G6_MH_PagoSuscripcionService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pagos-suscripciones")
@RequiredArgsConstructor
@Tag(name = "Módulo de Finanzas, Planes y Suscripciones", description = "Endpoints para el manejo de billeteras digitales, control de ciclos de facturación premium e historial de recibos")
@CrossOrigin(origins = "*")
public class G6_MH_PagoSuscripcionController {

    private final G6_MH_PagoSuscripcionService pagoService;

    // HU-18 Escenario 1 - Seleccionar o cambiar el plan de suscripción (Standard, VIP, Gold, Platinum)
    @PostMapping("/suscripcion")
    public ResponseEntity<G6_MH_SuscripcionResponseDTO> seleccionarPlan(@RequestBody G6_MH_SuscripcionRequestDTO dto) {
        return ResponseEntity.ok(pagoService.seleccionarPlan(dto));
    }

    // HU-18 Escenario 2 - Consultar la suscripción activa del usuario (vacío si continuó sin plan)
    @GetMapping("/suscripcion/{idUsuario}")
    public ResponseEntity<G6_MH_SuscripcionResponseDTO> obtenerSuscripcionActual(@PathVariable Long idUsuario) {
        Optional<G6_MH_SuscripcionResponseDTO> suscripcion = pagoService.obtenerSuscripcionActual(idUsuario);
        return suscripcion.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    // HU-19 - Listar los métodos de pago ya registrados por el usuario
    @GetMapping("/metodo/{idUsuario}")
    public ResponseEntity<List<G6_MH_MetodoPagoResponseDTO>> listarMetodos(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(pagoService.listarMetodosPago(idUsuario));
    }

    // HU-19 Escenario 1 - Registrar de forma exitosa un método de pago (Billetera digital / Tarjeta)
    @PostMapping("/metodo")
    public ResponseEntity<String> registrarMetodo(@RequestBody G6_MH_MetodoPagoDTO dto) {
        return ResponseEntity.ok(pagoService.registrarMetodoPago(dto));
    }

    // HU-19 Escenario 2 - Eliminar un método de pago de manera segura y actualizar el predeterminado
    @DeleteMapping("/metodo/{idMetodoPago}")
    public ResponseEntity<String> eliminarMetodo(@PathVariable Long idMetodoPago) {
        return ResponseEntity.ok(pagoService.eliminarMetodoPago(idMetodoPago));
    }

    // HU-20 Escenario 1 - Pausar o congelar temporalmente la facturación recurrente de la cuenta
    @PutMapping("/suscripcion/congelar/{idUsuario}")
    public ResponseEntity<String> congelarSuscripcion(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(pagoService.congelarSuscripcion(idUsuario));
    }

    // HU-20 Escenario 2 - Cancelación definitiva de la suscripción Premium
    @DeleteMapping("/suscripcion/cancelar/{idUsuario}")
    public ResponseEntity<String> cancelarSuscripcion(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(pagoService.cancelarSuscripcionDefinitiva(idUsuario));
    }

    // HU-21 Escenario 1 - Visualizar el listado histórico de cobros con fechas, montos y estados
    @GetMapping("/historial/{idUsuario}")
    public ResponseEntity<List<G6_MH_FacturaResponseDTO>> obtenerHistorial(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(pagoService.obtenerHistorialPagos(idUsuario));
    }

    // HU-21 Escenario 2 - Generar y descargar el comprobante de pago electrónico en formato PDF
    @GetMapping("/factura/descargar/{idFactura}")
    public ResponseEntity<byte[]> descargarFactura(@PathVariable Long idFactura) {
        byte[] pdfBytes = pagoService.descargarFacturaPdf(idFactura);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("factura-" + idFactura + ".pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}