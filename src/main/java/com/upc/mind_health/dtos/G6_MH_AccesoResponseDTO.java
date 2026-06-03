package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_AccesoResponseDTO {
    private Long idAcceso;
    private String dispositivo;
    private String ubicacion;
    private LocalDateTime fechaAcceso;
    private Boolean esSospechoso;
}