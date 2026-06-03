package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_ProgresoEmocionalDTO {
    private LocalDate fecha;
    private String emocionRegistrada;
    private String descripcion;
    private Integer puntajeAnimo;
}