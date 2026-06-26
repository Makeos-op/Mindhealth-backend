package com.upc.mind_health.dtos;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_ContenidoDetalleDTO {
    private Long idContenido;
    private String titulo;
    private String tipo;
    private String urlRecurso;
    private String emocionAsociada;
    private List<G6_MH_CalificacionResponseDTO> comentariosYCalificaciones; // HU-23 Escenario 2
}