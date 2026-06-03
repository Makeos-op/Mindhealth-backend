package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_PoliticaResponseDTO {
    private String titulo;
    private String contenidoHtml;
    private String version;
    private LocalDateTime ultimaActualizacion;
}