package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_DiarioResponseDTO {
    private Long idDiario;
    private G6_MH_UsuarioReducidoDTO usuario;
    private String titulo;
    private String contenido;
    private LocalDate fecha;
}