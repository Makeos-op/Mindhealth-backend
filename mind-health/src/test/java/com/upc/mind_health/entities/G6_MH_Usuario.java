package mhg6.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String contrasena;

    private Integer edad;

    private String genero;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @Column(nullable = false)
    private String rol;

    @Column(name = "token_verificacion")
    private String tokenVerificacion;

    @Column(name = "cuenta_activa", nullable = false)
    private Boolean cuentaActiva = false;

    @Column(name = "token_recuperacion")
    private String tokenRecuperacion;
}