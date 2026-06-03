package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_acceso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_HistorialAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acceso")
    private Long idAcceso;

    @Column(nullable = false)
    private String dispositivo; // Ej: "Chrome / Windows", "Safari / iPhone"

    @Column(nullable = false)
    private String ubicacion; // Ej: "Lima, Peru", "Madrid, Spain"

    @Column(name = "fecha_acceso", nullable = false)
    private LocalDateTime fechaAcceso;

    @Builder.Default
    @Column(name = "es_sospechoso", nullable = false)
    private Boolean esSospechoso = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private G6_MH_Usuario usuario;
}