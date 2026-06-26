package com.upc.mind_health.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_ObjetivosRequestDTO {
    private Long idUsuario;
    private String nuevosObjetivos; // Ej: "Manejo del estrés, Conciliar sueño"
}