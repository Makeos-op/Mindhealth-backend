package com.upc.mind_health.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_CalificacionRequestDTO {
    private Long idContenido;
    private Long idUsuario;
    private Integer estrellas;
    private String comentario;
}