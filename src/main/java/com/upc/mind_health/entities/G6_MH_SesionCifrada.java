package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesion_cifrada")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_SesionCifrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion_cifrada")
    private Long idSesionCifrada;

    @Column(name = "codigo_sesion", nullable = false, unique = true)
    private String codigoSesion; // Ej: "SESS-2026-XYZ"

    @Column(name = "fecha_interaccion", nullable = false)
    private LocalDateTime fechaInteraccion;

    @Builder.Default
    @Column(name = "esta_protegido", nullable = false)
    private Boolean estaProtegido = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private G6_MH_Usuario usuario;
}