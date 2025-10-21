package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.entidades;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String sessionId; // Para identificar al jugador en la sesión HTTP
    private boolean listo; // Si ha terminado de responder
    private int puntajeTotal;
    private LocalDateTime fechaIngreso;
    private boolean quiereReiniciar = false; // Si quiere volver a jugar

    @ManyToOne
    @JoinColumn(name = "sala_id")
    @JsonBackReference
    private Sala sala;

    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<RespuestaJugador> respuestas = new ArrayList<>();
}
