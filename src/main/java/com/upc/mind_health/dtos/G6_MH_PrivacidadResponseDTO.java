package com.upc.mind_health.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_PrivacidadResponseDTO {
    private Long idPrivacidad;
    private Long idUsuario;
    private Boolean permitirAnalisisIa;
    private Boolean compartirConTerapeuta;
    private Boolean anonimizacion;
    private Boolean usoIa;
    private Boolean visibilidadDatos;
    private String mensaje;
}