package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.entidades;

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
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private int capacidadMaxima;
    private String estado; // ESPERANDO, JUGANDO, FINALIZADA
    private LocalDateTime fechaCreacion;

    private int tiempoLimiteSegundos = 120; // Tiempo límite en segundos (default 2 minutos)
    private int jugadoresListosParaReiniciar = 0; // Contador para volver a jugar

    @OneToMany(mappedBy = "sala", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Jugador> jugadores = new ArrayList<>();

    @OneToOne(mappedBy = "sala", cascade = CascadeType.ALL)
    private PartidaMulti partida;

    public int getCantidadJugadores() {
        return jugadores.size();
    }

    public boolean estaLlena() {
        return jugadores.size() >= capacidadMaxima;
    }

    public boolean puedeIniciar() {
        return jugadores.size() >= 2 && estado.equals("ESPERANDO");
    }
}
