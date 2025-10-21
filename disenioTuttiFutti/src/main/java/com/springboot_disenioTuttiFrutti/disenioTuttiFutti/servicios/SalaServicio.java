package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.servicios;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.entidades.*;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos.CategoriasDTO;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos.ValidacionesDTO;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.repositorios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SalaServicio {

    @Autowired
    private ISalaRepositorio salaRepositorio;

    @Autowired
    private IJugadorRepositorio jugadorRepositorio;

    @Autowired
    private IPartidaMultiRepositorio partidaMultiRepositorio;

    @Autowired
    private IRespuestaJugadorRepositorio respuestaJugadorRepositorio;

    @Autowired
    private PartidaServicio partidaServicio;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Sala crearSala(String nombre, int capacidadMaxima) {
        Sala sala = new Sala();
        sala.setNombre(nombre);
        sala.setCapacidadMaxima(capacidadMaxima);
        sala.setEstado("ESPERANDO");
        sala.setFechaCreacion(LocalDateTime.now());
        return salaRepositorio.save(sala);
    }

    public List<Sala> listarSalasDisponibles() {
        return salaRepositorio.findByEstadoOrderByFechaCreacionDesc("ESPERANDO");
    }

    public Optional<Sala> obtenerSala(Long salaId) {
        return salaRepositorio.findById(salaId);
    }

    @Transactional
    public Jugador unirseASala(Long salaId, String nombreJugador, String sessionId) {
        Optional<Sala> salaOpt = salaRepositorio.findById(salaId);
        if (salaOpt.isEmpty() || salaOpt.get().estaLlena()) {
            return null;
        }

        Sala sala = salaOpt.get();

        // Verificar si el jugador ya está en la sala
        Optional<Jugador> jugadorExistente = jugadorRepositorio.findBySessionIdAndSalaId(sessionId, salaId);
        if (jugadorExistente.isPresent()) {
            return jugadorExistente.get();
        }

        Jugador jugador = new Jugador();
        jugador.setNombre(nombreJugador);
        jugador.setSessionId(sessionId);
        jugador.setListo(false);
        jugador.setPuntajeTotal(0);
        jugador.setFechaIngreso(LocalDateTime.now());
        jugador.setSala(sala);

        return jugadorRepositorio.save(jugador);
    }

    @Transactional
    public boolean iniciarPartida(Long salaId) throws JsonProcessingException {
        Optional<Sala> salaOpt = salaRepositorio.findById(salaId);
        if (salaOpt.isEmpty() || !salaOpt.get().puedeIniciar()) {
            return false;
        }

        Sala sala = salaOpt.get();

        // Generar letra y categorías usando el servicio existente
        CategoriasDTO categoriasDTO = partidaServicio.iniciarPartida(5);

        // Crear partida multijugador
        PartidaMulti partida = new PartidaMulti();
        partida.setLetra(categoriasDTO.getLetra());
        partida.setCategoriasJson(objectMapper.writeValueAsString(categoriasDTO.getCategorias()));
        partida.setFechaInicio(LocalDateTime.now());
        partida.setSala(sala);

        partidaMultiRepositorio.save(partida);

        // Cambiar estado de la sala
        sala.setEstado("JUGANDO");
        salaRepositorio.save(sala);

        return true;
    }

    @Transactional
    public void guardarRespuestas(Long jugadorId, Map<String, String> respuestas, String letra) {
        Optional<Jugador> jugadorOpt = jugadorRepositorio.findById(jugadorId);
        if (jugadorOpt.isEmpty()) {
            return;
        }

        Jugador jugador = jugadorOpt.get();

        // Validar respuestas con el servicio existente
        ValidacionesDTO validaciones = partidaServicio.validarRespuestas(letra, respuestas);

        // Guardar cada respuesta
        for (ValidacionesDTO.ValidacionItem item : validaciones.getValidaciones()) {
            RespuestaJugador respuesta = new RespuestaJugador();
            respuesta.setCategoria(item.getCategoria());
            respuesta.setRespuesta(item.getRespuestaUsuario());
            respuesta.setEsValida(item.isEsValida());
            respuesta.setPuntos(item.getPuntos());
            respuesta.setRazon(item.getRazon());
            respuesta.setJugador(jugador);
            respuestaJugadorRepositorio.save(respuesta);
        }

        // Actualizar puntaje y marcar como listo
        jugador.setPuntajeTotal(validaciones.getPuntajeTotal());
        jugador.setListo(true);
        jugadorRepositorio.save(jugador);

        // Verificar si todos terminaron
        verificarFinDePartida(jugador.getSala().getId());
    }

    @Transactional
    public void verificarFinDePartida(Long salaId) {
        Optional<Sala> salaOpt = salaRepositorio.findById(salaId);
        if (salaOpt.isEmpty()) {
            return;
        }

        Sala sala = salaOpt.get();

        // Verificar si todos los jugadores están listos
        boolean todosListos = sala.getJugadores().stream().allMatch(Jugador::isListo);

        if (todosListos && sala.getEstado().equals("JUGANDO")) {
            sala.setEstado("FINALIZADA");
            Optional<PartidaMulti> partidaOpt = partidaMultiRepositorio.findBySalaId(salaId);
            partidaOpt.ifPresent(partida -> {
                partida.setFechaFin(LocalDateTime.now());
                partidaMultiRepositorio.save(partida);
            });
            salaRepositorio.save(sala);
        }
    }

    public Optional<Jugador> obtenerJugadorPorSession(String sessionId) {
        return jugadorRepositorio.findBySessionId(sessionId);
    }

    public Optional<PartidaMulti> obtenerPartidaDeSala(Long salaId) {
        return partidaMultiRepositorio.findBySalaId(salaId);
    }

    public List<String> obtenerCategoriasDeSala(Long salaId) throws JsonProcessingException {
        Optional<PartidaMulti> partidaOpt = partidaMultiRepositorio.findBySalaId(salaId);
        if (partidaOpt.isEmpty()) {
            return List.of();
        }
        return objectMapper.readValue(partidaOpt.get().getCategoriasJson(), List.class);
    }
}
