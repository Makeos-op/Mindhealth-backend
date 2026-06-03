package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_CoordinacionResponseDTO {
    private Long idColaboracion;
    private String nombreEmisor;
    private String nombreReceptor;
    private String estadoActual;
    private String notasDeCasoCompartidas;
    private String mensajeNotificacion;
    private LocalDateTime fechaCambio;
}