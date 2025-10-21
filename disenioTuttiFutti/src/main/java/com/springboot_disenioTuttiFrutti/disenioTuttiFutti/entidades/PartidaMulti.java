package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.entidades;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartidaMulti {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String letra;

    @Column(length = 1000)
    private String categoriasJson; // Guardamos las categorías como JSON

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime tiempoLimite; // Cuando termina el tiempo límite

    @OneToOne
    @JoinColumn(name = "sala_id")
    @JsonBackReference
    private Sala sala;
}
