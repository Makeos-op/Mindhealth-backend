package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "metas_bienestar")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_MetaBienestar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_meta")
    private Long idMeta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private G6_MH_Usuario usuario;

    @Column(nullable = false, length = 30) // Validación estricta de max 30 caracteres
    private String nombreMeta;

    @Column(nullable = false)
    private boolean completada; // Estado inicial: false (Pendiente)

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;
}