package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_CasoCriticoResponseDTO {
    private Long idDerivacion;
    private String nombrePaciente;
    private LocalDate fechaDerivacion;
    private String motivoAlerta;
    private String mensajeConfirmacion;
    private String ultimaEmocionHistorica;
    private List<String> recomendacionesSeguimiento;
}