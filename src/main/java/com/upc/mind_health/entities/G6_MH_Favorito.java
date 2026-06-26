package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ejercicios_favoritos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_Favorito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favorito")
    private Long idFavorito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private G6_MH_Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contenido", nullable = false)
    private G6_MH_ContenidoTerapeutico contenido;

    @Column(name = "fecha_guardado", nullable = false)
    private LocalDateTime fechaGuardado;
}