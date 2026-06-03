package com.upc.mind_health.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_SimulacionLoginDTO {
    private String dispositivo;
    private String ubicacion;
}