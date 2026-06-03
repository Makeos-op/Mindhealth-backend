package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_HistorialSeguroResponseDTO {
    private Long idSesion;
    private LocalDateTime fechaInicio;
    private String estado;
    private String ultimaEmocion;
    private String confirmacionSeguridad;
}