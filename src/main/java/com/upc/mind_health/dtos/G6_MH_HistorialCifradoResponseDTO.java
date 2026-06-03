package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_HistorialCifradoResponseDTO {
    private String codigoSesion;
    private LocalDateTime fechaInteraccion;
    private String confirmacionSeguridad;
    private Boolean certificadoProteccion;
}