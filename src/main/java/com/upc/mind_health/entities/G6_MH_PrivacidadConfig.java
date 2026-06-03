package com.upc.mind_health.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "privacidad_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_PrivacidadConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_privacidad")
    private Long idPrivacidad;

    @Builder.Default
    @Column(name = "anonimizacion", nullable = false)
    private Boolean anonimizacion = false;

    @Builder.Default
    @Column(name = "uso_ia", nullable = false)
    private Boolean usoIa = true; //

    @Builder.Default
    @Column(name = "visibilidad_datos", nullable = false)
    private Boolean visibilidadDatos = true; //

    @Builder.Default
    @Column(name = "compartir_con_terapeuta", nullable = false)
    private Boolean compartirConTerapeuta = true;

    @Builder.Default
    @Column(name = "permitir_analisis_ia", nullable = false)
    private Boolean permitirAnalisisIa = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    @JsonIgnoreProperties({"privacidadConfig", "contrasena"})
    private G6_MH_Usuario usuario;
}