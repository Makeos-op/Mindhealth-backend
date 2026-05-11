package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_RegistroEmocionalRequestDTO {
    private Long idUsuario;
    private String emocion;
    private String descripcion;
}
