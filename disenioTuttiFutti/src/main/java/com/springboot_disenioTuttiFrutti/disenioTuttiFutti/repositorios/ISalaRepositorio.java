package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.repositorios;

import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.entidades.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISalaRepositorio extends JpaRepository<Sala, Long> {
    List<Sala> findByEstado(String estado);
    List<Sala> findByEstadoOrderByFechaCreacionDesc(String estado);
}
