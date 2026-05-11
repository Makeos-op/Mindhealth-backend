package mhg6.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_UsuarioRegistroDTO {
    private String nombre;
    private String correo;
    private String contrasena;
    private Integer edad;
    private String genero;
}