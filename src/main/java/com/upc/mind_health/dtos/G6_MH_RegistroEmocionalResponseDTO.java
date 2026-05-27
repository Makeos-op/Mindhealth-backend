package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_RegistroEmocionalResponseDTO {
    private Long idRegistro;
    private G6_MH_UsuarioReducidoDTO usuario;
    private String emocion;
    private String descripcion;
    private LocalDate fecha;
}
