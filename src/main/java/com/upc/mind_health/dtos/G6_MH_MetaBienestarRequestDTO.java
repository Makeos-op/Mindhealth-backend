package com.upc.mind_health.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_MetaBienestarRequestDTO {
    private Long idUsuario;
    private String nombreMeta;
}