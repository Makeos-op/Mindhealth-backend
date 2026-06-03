package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "suscripciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_Suscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suscripcion")
    private Long idSuscripcion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private G6_MH_Usuario usuario;

    @Column(name = "tipo_plan", nullable = false) // Standard, VIP, Gold, Platinum
    private String tipoPlan;

    @Column(nullable = false)
    private String estado; // ACTIVA, CONGELADA, CANCELADA

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
}