package com.upc.mind_health.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_DiarioRequestDTO {
    private Long idUsuario;
    private String titulo;
    private String contenido;
}

