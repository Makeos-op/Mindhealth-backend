package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contenido_terapeutico")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_ContenidoTerapeutico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contenido")
    private Long idContenido;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String tipo; // VIDEO, ARTICULO, RECURSO_INTERACTIVO

    @Column(name = "url_recurso", nullable = false)
    private String urlRecurso;

    @Column(name = "emocion_asociada", nullable = false)
    private String emocionAsociada; // Ansiedad, Tristeza, Estrés, Calma
}