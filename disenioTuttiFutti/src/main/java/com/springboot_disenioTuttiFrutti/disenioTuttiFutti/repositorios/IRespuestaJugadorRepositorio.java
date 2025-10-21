package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.repositorios;

import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.entidades.RespuestaJugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRespuestaJugadorRepositorio extends JpaRepository<RespuestaJugador, Long> {
    List<RespuestaJugador> findByJugadorId(Long jugadorId);
}
