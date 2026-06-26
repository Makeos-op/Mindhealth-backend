package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "rutinas_preventivas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_RutinaPreventiva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rutina")
    private Long idRutina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private G6_MH_Usuario usuario;

    @Column(nullable = false, length = 1000)
    private String actividades; // Ej: "Mañana: Respiración 4-7-8; Noche: Meditación Guiada"

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDate fechaGeneracion;

    @Column(name = "es_preventiva_recaida", nullable = false)
    private boolean esPreventivaRecaida; // True si se activó por patrón negativo (Escenario 3)

    @Column(name = "utilidad_evaluada")
    private String utilidadEvaluada; // Guardará el feedback: "UTIL", "NEUTRO" o "NO_UTIL" (Escenario 5)
}