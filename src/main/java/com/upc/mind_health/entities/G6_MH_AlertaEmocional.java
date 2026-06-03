package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerta_emocional")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_AlertaEmocional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long idAlerta;

    @Column(nullable = false)
    private String tipoAlerta; // "PROGRESO_POSITIVO", "ALERTA_PREVENTIVA", "CRITICA_RECURRENTE"

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(length = 1000)
    private String sugerencias;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean leido = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private G6_MH_Usuario usuario;
}