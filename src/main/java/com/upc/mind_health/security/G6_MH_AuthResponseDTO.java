package com.upc.mind_health.security;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_AuthResponseDTO {
    private String token;
    private String mensaje;
    private Long idUsuario;
    private String nombre;
    private String correo;
}