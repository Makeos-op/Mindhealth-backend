package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "facturas_pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long idFactura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private G6_MH_Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private Double monto;

    @Column(nullable = false)
    private String estado; // COMPLETO, PENDIENTE, FALLIDO
}