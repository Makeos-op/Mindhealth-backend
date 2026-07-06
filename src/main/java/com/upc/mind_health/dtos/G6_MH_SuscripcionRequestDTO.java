package com.upc.mind_health.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_SuscripcionRequestDTO {
    private Long idUsuario;
    private String tipoPlan; // "Standard", "VIP", "Gold", "Platinum"
}
