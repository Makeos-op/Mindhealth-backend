package com.upc.mind_health.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_MetodoPagoDTO {
    private Long idUsuario;
    private String tipoMetodo; // "Billetera digital"
    private String proveedor;  // "Yape"
}