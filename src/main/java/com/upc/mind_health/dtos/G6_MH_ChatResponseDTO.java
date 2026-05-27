package com.upc.mind_health.dtos;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_ChatResponseDTO {
    private String textoUsuario;
    private String emocionDetectada;
    private String nivelUrgencia; // BAJO, MEDIO, ALTO, CRÍTICO
    private String respuestaEmpatica;
    private List<String> recomendaciones;
    private List<String> lineasDeAyuda; // Para el Escenario 2 (Casos críticos)
}