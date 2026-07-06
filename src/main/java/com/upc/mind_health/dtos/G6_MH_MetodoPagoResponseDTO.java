package com.upc.mind_health.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_MetodoPagoResponseDTO {
    private Long idMetodoPago;
    private String tipoMetodo;
    private String proveedor;
    private boolean predeterminado;
}
