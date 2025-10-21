package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.repositorios;

import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.entidades.PartidaMulti;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPartidaMultiRepositorio extends JpaRepository<PartidaMulti, Long> {
    Optional<PartidaMulti> findBySalaId(Long salaId);
}
