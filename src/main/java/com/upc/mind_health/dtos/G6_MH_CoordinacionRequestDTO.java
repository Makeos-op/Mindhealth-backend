package com.upc.mind_health.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class G6_MH_CoordinacionRequestDTO {
    private Long idDerivacion;
    private String correoColegaInvitado;
}