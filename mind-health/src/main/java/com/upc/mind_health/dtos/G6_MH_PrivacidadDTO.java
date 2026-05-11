package mhg6.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_PrivacidadDTO {
    private Boolean visibilidadDatos;
    private Boolean usoIa;
    private Boolean anonimizacion;
}
