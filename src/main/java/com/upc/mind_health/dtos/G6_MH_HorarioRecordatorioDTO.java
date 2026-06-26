package com.upc.mind_health.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_HorarioRecordatorioDTO {
    private Long idUsuario;
    private String horaInicio; // "HH:mm"
    private String horaFin;    // "HH:mm"
}