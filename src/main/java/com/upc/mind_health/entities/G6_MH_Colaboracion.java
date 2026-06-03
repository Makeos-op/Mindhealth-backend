package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "colaboracion_profesional")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_Colaboracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idColaboracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_derivacion", nullable = false)
    @JsonIgnoreProperties({"sesion", "hibernateLazyInitializer", "handler"})
    private G6_MH_Derivacion derivacion;

    // El psicólogo original que pide ayuda
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emisor", nullable = false)
    @JsonIgnoreProperties({"derivaciones", "usuario", "hibernateLazyInitializer", "handler"})
    private G6_MH_Psicologo emisor;

    // El psicólogo invitado a colaborar
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_receptor", nullable = false)
    @JsonIgnoreProperties({"derivaciones", "usuario", "hibernateLazyInitializer", "handler"})
    private G6_MH_Psicologo receptor;

    @Column(name = "estado_solicitud", nullable = false, length = 50)
    @Builder.Default
    private String estadoSolicitud = "PENDIENTE"; // PENDIENTE, ACEPTADA, RECHAZADA

    @Column(name = "observaciones_compartidas", length = 1500)
    private String observacionesCompartidas; // El espacio compartido del Escenario 2

    private LocalDateTime fechaSolicitud;
}