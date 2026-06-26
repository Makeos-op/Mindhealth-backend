package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "calificaciones_contenido")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_CalificacionContenido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_calificacion")
    private Long idCalificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contenido", nullable = false)
    private G6_MH_ContenidoTerapeutico contenido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private G6_MH_Usuario usuario;

    @Column(nullable = false)
    private Integer estrellas; // Rango de 1 a 5 estrellas (Escenario 1)

    private String comentario;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}