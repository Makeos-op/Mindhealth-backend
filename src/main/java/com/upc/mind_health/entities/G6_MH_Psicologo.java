package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profesional")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_Psicologo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profesional")
    private Long idPsicologo;

    @Column(name = "nombre", length = 255)
    private String nombre;

    @Column(name = "especialidad", length = 255)
    private String especialidad;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private G6_MH_Usuario usuario;

    @Builder.Default
    @Column(name = "disponible", nullable = false)
    private Boolean disponible = true;

    @Builder.Default
    @OneToMany(mappedBy = "profesional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<G6_MH_Derivacion> derivaciones = new ArrayList<>();
}