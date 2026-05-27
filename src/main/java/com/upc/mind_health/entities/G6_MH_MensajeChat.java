package com.upc.mind_health.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensaje_chat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_MensajeChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMensaje;

    @ManyToOne
    @JoinColumn(name = "id_sesion", nullable = false)
    @JsonIgnoreProperties("mensajes")
    private G6_MH_SesionTerapia sesion;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    private LocalDateTime fechaEnvio;

    private String tipoRemitente; // "PACIENTE" o "IA_ASISTENTE"
}