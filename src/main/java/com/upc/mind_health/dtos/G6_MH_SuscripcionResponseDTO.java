package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_SuscripcionResponseDTO {
    private Long idSuscripcion;
    private String tipoPlan;
    private String estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
