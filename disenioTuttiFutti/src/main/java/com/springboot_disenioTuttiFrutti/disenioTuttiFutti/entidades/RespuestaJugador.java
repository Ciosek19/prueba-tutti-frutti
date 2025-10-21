package com.springboot_disenioTuttiFrutti.disenioTuttiFrutti.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaJugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoria;
    private String respuesta;
    private boolean esValida;
    private int puntos;

    @Column(length = 500)
    private String razon; // Razón de la validación dada por la IA

    @ManyToOne
    @JoinColumn(name = "jugador_id")
    private Jugador jugador;
}
