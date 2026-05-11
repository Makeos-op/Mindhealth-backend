package com.upc.mind_health.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_UsuarioActualizarDTO {
    private String nombre;
    private Integer edad;
    private String genero;
}