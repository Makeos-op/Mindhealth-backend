package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notas_personales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_NotaPersonal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nota")
    private Long idNota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private G6_MH_Usuario usuario;

    @Column(nullable = false, length = 50) // Validación de max 50 caracteres (Escenario 1)
    private String titulo;

    @Column(nullable = false, length = 500) // Validación de max 500 caracteres (Escenario 1)
    private String cuerpo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}