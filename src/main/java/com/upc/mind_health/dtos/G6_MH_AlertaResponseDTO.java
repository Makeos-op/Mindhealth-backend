package com.upc.mind_health.dtos;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_AlertaResponseDTO {
    private String tipo; // PROGRESO_POSITIVO, ALERTA_PREVENTIVA, CRITICA_RECURRENTE
    private String mensaje;
    private List<String> recomendaciones;
    private Boolean requiereProfesional;
}