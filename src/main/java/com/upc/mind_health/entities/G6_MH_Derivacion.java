package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@Entity
@Table(name = "derivacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_Derivacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_derivacion")
    private Long idDerivacion;

    @Column(name = "motivo", nullable = false)
    private String motivo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "estado_atencion", nullable = false, length = 50)
    @Builder.Default
    private String estadoAtencion = "PENDIENTE"; // PENDIENTE o ATENDIDO

    @Column(name = "notas_seguimiento", length = 1000)
    private String notasSeguimiento;

    @Column(name = "fecha_atencion")
    private java.time.LocalDateTime fechaAtencion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sesion", nullable = false)
    @JsonIgnoreProperties({"mensajes", "usuario", "hibernateLazyInitializer", "handler"})
    private G6_MH_SesionTerapia sesion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profesional", nullable = false)
    @JsonIgnoreProperties({"derivaciones", "usuario", "hibernateLazyInitializer", "handler"})
    private G6_MH_Psicologo profesional;
}