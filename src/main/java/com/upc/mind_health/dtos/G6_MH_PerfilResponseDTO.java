package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_PerfilResponseDTO {
    private Long idUsuario;
    private String nombre;
    private String correo;
    private Integer edad;
    private String genero;
    private LocalDate fechaRegistro;
    private String rol;
    private Boolean cuentaActiva;
}