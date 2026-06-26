package com.upc.mind_health.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_ActividadCalendarioDTO {
    private Long idUsuario;
    private String nombreActividad; // Ej: "Sesión de Contención" o "Ejercicio de Respiración"
    private String fechaHora;       // "YYYY-MM-DD HH:mm"
}