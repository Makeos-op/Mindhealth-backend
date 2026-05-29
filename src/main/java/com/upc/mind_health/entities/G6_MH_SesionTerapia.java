package com.upc.mind_health.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sesion_terapia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_SesionTerapia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSesion;

    private LocalDateTime fechaInicio;

    private String ultimaEmocionDetectada;
    private String nivelUrgenciaActual;
    private String estado;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties("sesiones")
    private G6_MH_Usuario usuario;

    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<G6_MH_MensajeChat> mensajes;
}