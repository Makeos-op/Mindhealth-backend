package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_ResumenPostSesionDTO {
    private Long idSesion;
    private LocalDateTime fechaFinalizacion;
    private String estadoSesion;
    private String emocionPredominante;
    private String insightClinicoIA;
    private List<String> sugerenciasPracticas; // Ejercicios, mindfulness, journaling
}