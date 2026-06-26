package com.upc.mind_health.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_SugerenciaTerapiaResponseDTO {
    private String metodoActual;
    private String metodoSugerido;
    private String sustentacionIA; // Razón clínica del cambio
    private String mensajeConfirmacion;
}