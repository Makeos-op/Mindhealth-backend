package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "politica_datos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_PoliticaDatos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_politica")
    private Long idPolitica;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenidoHtml;

    @Column(nullable = false)
    private String version;

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;
}