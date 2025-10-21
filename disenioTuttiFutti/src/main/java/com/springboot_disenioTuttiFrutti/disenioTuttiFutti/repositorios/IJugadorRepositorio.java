package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.repositorios;

import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.entidades.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IJugadorRepositorio extends JpaRepository<Jugador, Long> {
    Optional<Jugador> findBySessionId(String sessionId);
    Optional<Jugador> findBySessionIdAndSalaId(String sessionId, Long salaId);
}
