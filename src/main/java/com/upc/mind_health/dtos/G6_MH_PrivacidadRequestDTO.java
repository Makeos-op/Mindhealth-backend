package com.upc.mind_health.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_PrivacidadRequestDTO {
    private Boolean permitirAnalisisIa;
    private Boolean compartirConTerapeuta;
    private Boolean anonimizacion;
    private Boolean usoIa;
    private Boolean visibilidadDatos;
}