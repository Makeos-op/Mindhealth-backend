package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_AlertaNotificacionDTO {
    private Long idDerivacion;
    private String correoProfesional;
    private String mensajePush;
    private String severidad;
    private LocalDate fechaAlerta;
}