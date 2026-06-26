package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_CalificacionResponseDTO {
    private Long idCalificacion;
    private String nombreUsuario;
    private Integer estrellas;
    private String comentario;
    private LocalDateTime fecha;
}