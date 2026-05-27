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

    @Column(name = "activo", nullable = false)
    private Boolean activo = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_rol", // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    private Set<G6_MH_Rol> roles = new HashSet<>();

    // --- CAMPOS DE VALIDACIÓN PARA HU DE SEGURIDAD ---
    @Column(name = "token_activacion", length = 255)
    private String tokenActivacion;

    @Column(name = "fecha_expiracion_token")
    private LocalDateTime fechaExpiracionToken;
}
