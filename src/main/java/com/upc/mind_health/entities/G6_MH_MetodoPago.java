package com.upc.mind_health.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "metodos_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class G6_MH_MetodoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo_pago")
    private Long idMetodoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private G6_MH_Usuario usuario;

    @Column(name = "tipo_metodo", nullable = false) // Ej: "Billetera digital", "Tarjeta"
    private String tipoMetodo;

    @Column(name = "proveedor") // Ej: "Yape", "Plin", "Visa"
    private String proveedor;

    @Column(name = "predeterminado", nullable = false)
    private boolean predeterminado;
}