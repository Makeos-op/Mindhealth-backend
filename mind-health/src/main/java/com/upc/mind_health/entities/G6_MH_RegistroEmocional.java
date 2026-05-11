package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "registro_emocional")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_RegistroEmocional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Long idRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private G6_MH_Usuario usuario;

    @Column(nullable = false)
    private String emocion;

    private String descripcion;

    @Column(nullable = false)
    private LocalDate fecha;
}
