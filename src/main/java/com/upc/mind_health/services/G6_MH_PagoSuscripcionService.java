package com.upc.mind_health.services;

import lombok.RequiredArgsConstructor;
import com.upc.mind_health.dtos.*;
import com.upc.mind_health.entities.*;
import com.upc.mind_health.repositories.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class G6_MH_PagoSuscripcionService {

    private final G6_MH_MetodoPagoRepository metodoPagoRepository;
    private final G6_MH_SuscripcionRepository suscripcionRepository;
    private final G6_MH_FacturaRepository facturaRepository;
    private final G6_MH_UsuarioRepository usuarioRepository;

    // 🌟 HU-18 ESCENARIO 1: Seleccionar o cambiar el plan de suscripción
    @Transactional
    public G6_MH_SuscripcionResponseDTO seleccionarPlan(G6_MH_SuscripcionRequestDTO dto) {
        G6_MH_Usuario usuario = usuarioRepository.findById(dto.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        G6_MH_Suscripcion suscripcion = suscripcionRepository.findByUsuarioIdUsuario(dto.getIdUsuario())
                .orElseGet(() -> G6_MH_Suscripcion.builder()
                        .usuario(usuario)
                        .fechaInicio(LocalDate.now())
                        .build());

        suscripcion.setTipoPlan(dto.getTipoPlan());
        suscripcion.setEstado("ACTIVA");
        suscripcion.setFechaInicio(suscripcion.getFechaInicio() != null ? suscripcion.getFechaInicio() : LocalDate.now());
        suscripcion.setFechaFin(suscripcion.getFechaInicio().plusMonths(1));

        G6_MH_Suscripcion guardada = suscripcionRepository.save(suscripcion);
        return mapearSuscripcion(guardada);
    }

    // 🌟 HU-18 ESCENARIO 2: Consultar la suscripción activa del usuario (o ausencia de ella)
    @Transactional(readOnly = true)
    public Optional<G6_MH_SuscripcionResponseDTO> obtenerSuscripcionActual(Long idUsuario) {
        return suscripcionRepository.findByUsuarioIdUsuario(idUsuario).map(this::mapearSuscripcion);
    }

    private G6_MH_SuscripcionResponseDTO mapearSuscripcion(G6_MH_Suscripcion suscripcion) {
        return G6_MH_SuscripcionResponseDTO.builder()
                .idSuscripcion(suscripcion.getIdSuscripcion())
                .tipoPlan(suscripcion.getTipoPlan())
                .estado(suscripcion.getEstado())
                .fechaInicio(suscripcion.getFechaInicio())
                .fechaFin(suscripcion.getFechaFin())
                .build();
    }

    // 🌟 HU-19: Listar los métodos de pago registrados por el usuario
    @Transactional(readOnly = true)
    public List<G6_MH_MetodoPagoResponseDTO> listarMetodosPago(Long idUsuario) {
        return metodoPagoRepository.findByUsuarioIdUsuario(idUsuario).stream()
                .map(m -> G6_MH_MetodoPagoResponseDTO.builder()
                        .idMetodoPago(m.getIdMetodoPago())
                        .tipoMetodo(m.getTipoMetodo())
                        .proveedor(m.getProveedor())
                        .predeterminado(m.isPredeterminado())
                        .build())
                .collect(Collectors.toList());
    }

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

        List<String> lineas = List.of(
                "MIND HEALTH INC. - FACTURA #" + factura.getIdFactura(),
                "Fecha de Emisión: " + factura.getFecha(),
                "Monto Cobrado: S/. " + factura.getMonto(),
                "Estado del Pago: " + factura.getEstado(),
                "Gracias por confiar en Mind Health."
        );
        return generarPdf(lineas);
    }

    private byte[] generarPdf(List<String> lineas) {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage();
            documento.addPage(pagina);

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                PDType1Font fuente = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                float y = 720;
                contenido.beginText();
                contenido.setFont(fuente, 12);
                contenido.newLineAtOffset(50, y);
                for (String linea : lineas) {
                    contenido.showText(linea);
                    contenido.newLineAtOffset(0, -20);
                }
                contenido.endText();
            }

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            documento.save(salida);
            return salida.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el PDF", e);
        }
    }
}