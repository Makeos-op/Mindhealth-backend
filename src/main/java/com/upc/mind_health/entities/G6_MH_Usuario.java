package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String contrasena;

    private Integer edad;

    private String genero;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "estilo_lenguaje_ia", length = 20, nullable = false)
    private String estiloLenguajeIa;

    @Column(name = "objetivos_personales", length = 500)
    private String objetivosPersonales;

    @Column(name = "metodo_terapia_preferido")
    private String metodoTerapiaPreferido;

    @Column(name = "hora_inicio_recordatorio")
    private String horaInicioRecordatorio;

    @Column(name = "hora_fin_recordatorio")
    private String horaFinRecordatorio;

    @Column(name = "calendario_vinculado", nullable = false)
    private boolean calendarioVinculado;

    @Column(name = "correo_red_apoyo", length = 150)
    private String correoRedApoyo;

    @Column(name = "notificar_red_apoyo", nullable = false)
    private boolean notificarRedApoyo;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = false;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_rol", // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    private Set<G6_MH_Rol> roles = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<G6_MH_Token> tokens = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<G6_MH_SesionTerapia> sesiones;
}
