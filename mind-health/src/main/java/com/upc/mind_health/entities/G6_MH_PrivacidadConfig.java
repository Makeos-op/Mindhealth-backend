package mhg6.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "privacidad_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_PrivacidadConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_privacidad")
    private Long idPrivacidad;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private G6_MH_Usuario usuario;

    @Column(name = "visibilidad_datos", nullable = false)
    private Boolean visibilidadDatos = false;

    @Column(name = "uso_ia", nullable = false)
    private Boolean usoIa = false;

    @Column(name = "anonimizacion", nullable = false)
    private Boolean anonimizacion = false;
}
