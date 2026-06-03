package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.*;
import com.upc.mind_health.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class G6_MH_PagoSuscripcionService {

    private final G6_MH_MetodoPagoRepository metodoPagoRepository;
    private final G6_MH_SuscripcionRepository suscripcionRepository;
    private final G6_MH_FacturaRepository facturaRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;

    // 🌟 HU-19 ESCENARIO 1: Seleccionar e inscribir método de pago
    @Transactional
    public String registrarMetodoPago(G6_MH_MetodoPagoDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<G6_MH_MetodoPago> existentes = metodoPagoRepository.findByUsuarioIdUsuario(dto.getIdUsuario());

        G6_MH_MetodoPago nuevoMetodo = G6_MH_MetodoPago.builder()
                .usuario(usuario)
                .tipoMetodo(dto.getTipoMetodo())
                .proveedor(dto.getProveedor())
                .predeterminado(existentes.isEmpty()) // Si es el primero, es el predeterminado
                .build();

        metodoPagoRepository.save(nuevoMetodo);
        return "Forma de pago registrada exitosamente.";
    }

    // 🌟 HU-19 ESCENARIO 2: Eliminar método de pago seguro
    @Transactional
    public String eliminarMetodoPago(Long idMetodoPago) {
        G6_MH_MetodoPago metodo = metodoPagoRepository.findById(idMetodoPago)
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado"));

        Long idUsuario = metodo.getUsuario().getIdUsuario();
        boolean eraPredeterminado = metodo.isPredeterminado();

        metodoPagoRepository.delete(metodo);

        if (eraPredeterminado) {
            List<G6_MH_MetodoPago> restantes = metodoPagoRepository.findByUsuarioIdUsuario(idUsuario);
            if (!restantes.isEmpty()) {
                G6_MH_MetodoPago nuevoPredeterminado = restantes.get(0);
                nuevoPredeterminado.setPredeterminado(true);
                metodoPagoRepository.save(nuevoPredeterminado);
                return "Método eliminado con éxito. Se asignó otro método como predeterminado de forma segura.";
            }
            return "Método de pago eliminado. Por favor, agregue otro método como predeterminado.";
        }
        return "Método de pago eliminado de manera segura.";
    }

    // 🌟 HU-20 ESCENARIO 1: Desactivar o congelar la facturación de la cuenta
    @Transactional
    public String congelarSuscripcion(Long idUsuario) {
        G6_MH_Suscripcion suscripcion = suscripcionRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("No cuentas con una suscripción activa para pausar."));

        suscripcion.setEstado("CONGELADA");
        suscripcionRepository.save(suscripcion);
        return "Se ha congelado la facturación en su cuenta de manera exitosa hasta su retorno.";
    }

    // 🌟 HU-20 ESCENARIO 2: Cancelación definitiva de suscripción Premium
    @Transactional
    public String cancelarSuscripcionDefinitiva(Long idUsuario) {
        G6_MH_Suscripcion suscripcion = suscripcionRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada."));

        suscripcionRepository.delete(suscripcion);
        return "Suscripción eliminada con éxito. Su cuenta ha retornado al estado base.";
    }

    // 🌟 HU-21 ESCENARIO 1: Visualización del historial de pagos realizados
    @Transactional(readOnly = true)
    public List<G6_MH_FacturaResponseDTO> obtenerHistorialPagos(Long idUsuario) {
        return facturaRepository.findByUsuarioIdUsuarioOrderByFechaDesc(idUsuario).stream()
                .map(f -> G6_MH_FacturaResponseDTO.builder()
                        .idFactura(f.getIdFactura())
                        .fecha(f.getFecha())
                        .monto(f.getMonto())
                        .estado(f.getEstado())
                        .build())
                .collect(Collectors.toList());
    }

    // 🌟 HU-21 ESCENARIO 2: Simular descarga de factura en PDF
    @Transactional(readOnly = true)
    public byte[] descargarFacturaPdf(Long idFactura) {
        G6_MH_Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        // Simulación de generación de bytes de un archivo PDF estructurado para el Frontend
        String plantillaFactura = "MIND HEALTH INC. - FACTURA #" + factura.getIdFactura() + "\n" +
                "Fecha de Emisión: " + factura.getFecha() + "\n" +
                "Monto Cobrado: S/. " + factura.getMonto() + "\n" +
                "Estado del Pago: " + factura.getEstado() + "\n" +
                "Gracias por confiar en Mind Health.";

        return plantillaFactura.getBytes();
    }
}