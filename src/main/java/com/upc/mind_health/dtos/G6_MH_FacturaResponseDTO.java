package com.upc.mind_health.dtos;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_FacturaResponseDTO {
    private Long idFactura;
    private LocalDateTime fecha;
    private Double monto;
    private String estado;
}