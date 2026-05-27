package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRole;

    @Column(unique = true, nullable = false, length = 50)
    private String name; // Aquí se guardará "ROLE_PACIENTE", "ROLE_PROFESIONAL", "ROLE_ADMIN"
}