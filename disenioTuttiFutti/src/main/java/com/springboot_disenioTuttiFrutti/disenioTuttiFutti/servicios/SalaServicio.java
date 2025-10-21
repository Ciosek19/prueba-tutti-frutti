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
import java.util.HashMap;
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

    @Autowired
    private WebSocketServicio webSocketServicio;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Sala crearSala(String nombre, int capacidadMaxima, int tiempoLimiteSegundos) {
        Sala sala = new Sala();
        sala.setNombre(nombre);
        sala.setCapacidadMaxima(capacidadMaxima);
        sala.setEstado("ESPERANDO");
        sala.setFechaCreacion(LocalDateTime.now());
        sala.setTiempoLimiteSegundos(tiempoLimiteSegundos);
        Sala savedSala = salaRepositorio.save(sala);

        // Notificar actualización de salas
        webSocketServicio.notificarActualizacionSalas();

        return savedSala;
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
        jugador.setQuiereReiniciar(false);
        jugador.setSala(sala);

        Jugador savedJugador = jugadorRepositorio.save(jugador);

        // Notificar a la sala que un jugador se unió
        Map<String, Object> datos = new HashMap<>();
        datos.put("jugador", nombreJugador);
        datos.put("cantidadJugadores", sala.getCantidadJugadores());
        webSocketServicio.enviarMensajeASala(salaId, "JUGADOR_UNIDO", datos);
        webSocketServicio.notificarActualizacionSalas();

        return savedJugador;
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
        partida.setTiempoLimite(LocalDateTime.now().plusSeconds(sala.getTiempoLimiteSegundos()));
        partida.setSala(sala);

        partidaMultiRepositorio.save(partida);

        // Cambiar estado de la sala
        sala.setEstado("JUGANDO");
        salaRepositorio.save(sala);

        // Notificar que la partida inició
        Map<String, Object> datos = new HashMap<>();
        datos.put("letra", categoriasDTO.getLetra());
        datos.put("categorias", categoriasDTO.getCategorias());
        datos.put("tiempoLimite", sala.getTiempoLimiteSegundos());
        webSocketServicio.enviarMensajeASala(salaId, "PARTIDA_INICIADA", datos);
        webSocketServicio.notificarActualizacionSalas();

        return true;
    }

    @Transactional
    public void guardarRespuestas(Long jugadorId, Map<String, String> respuestas, String letra) {
        Optional<Jugador> jugadorOpt = jugadorRepositorio.findById(jugadorId);
        if (jugadorOpt.isEmpty()) {
            return;
        }

        Jugador jugador = jugadorOpt.get();

        // Limpiar respuestas anteriores si existen
        respuestaJugadorRepositorio.deleteAll(jugador.getRespuestas());

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

        // Notificar que el jugador está listo
        Map<String, Object> datos = new HashMap<>();
        datos.put("jugador", jugador.getNombre());
        webSocketServicio.enviarMensajeASala(jugador.getSala().getId(), "JUGADOR_LISTO", datos);

        // Verificar si todos terminaron
        verificarFinDePartida(jugador.getSala().getId());
    }

    @Transactional
    public void finalizarPartidaPorTiempo(Long salaId) {
        Optional<Sala> salaOpt = salaRepositorio.findById(salaId);
        if (salaOpt.isEmpty()) {
            return;
        }

        Sala sala = salaOpt.get();

        if (!sala.getEstado().equals("JUGANDO")) {
            return;
        }

        // Marcar jugadores no listos con puntaje 0
        for (Jugador jugador : sala.getJugadores()) {
            if (!jugador.isListo()) {
                jugador.setListo(true);
                jugador.setPuntajeTotal(0);
                jugadorRepositorio.save(jugador);
            }
        }

        // Finalizar partida
        sala.setEstado("FINALIZADA");
        Optional<PartidaMulti> partidaOpt = partidaMultiRepositorio.findBySalaId(salaId);
        partidaOpt.ifPresent(partida -> {
            partida.setFechaFin(LocalDateTime.now());
            partidaMultiRepositorio.save(partida);
        });
        salaRepositorio.save(sala);

        // Notificar fin de partida
        webSocketServicio.enviarMensajeASala(salaId, "PARTIDA_FINALIZADA", null);
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

            // Notificar fin de partida
            webSocketServicio.enviarMensajeASala(salaId, "PARTIDA_FINALIZADA", null);
        }
    }

    @Transactional
    public boolean solicitarReinicio(Long jugadorId) {
        Optional<Jugador> jugadorOpt = jugadorRepositorio.findById(jugadorId);
        if (jugadorOpt.isEmpty()) {
            return false;
        }

        Jugador jugador = jugadorOpt.get();
        Sala sala = jugador.getSala();

        if (!sala.getEstado().equals("FINALIZADA")) {
            return false;
        }

        // Marcar jugador como queriendo reiniciar
        if (!jugador.isQuiereReiniciar()) {
            jugador.setQuiereReiniciar(true);
            jugadorRepositorio.save(jugador);

            sala.setJugadoresListosParaReiniciar(sala.getJugadoresListosParaReiniciar() + 1);
            salaRepositorio.save(sala);
        }

        // Notificar que un jugador quiere reiniciar
        Map<String, Object> datos = new HashMap<>();
        datos.put("jugador", jugador.getNombre());
        datos.put("listos", sala.getJugadoresListosParaReiniciar());
        datos.put("total", sala.getCantidadJugadores());
        webSocketServicio.enviarMensajeASala(sala.getId(), "REINICIO_SOLICITADO", datos);

        // Si todos quieren reiniciar, reiniciar la sala
        if (sala.getJugadoresListosParaReiniciar() >= sala.getCantidadJugadores()) {
            reiniciarSala(sala.getId());
        }

        return true;
    }

    @Transactional
    public void reiniciarSala(Long salaId) {
        Optional<Sala> salaOpt = salaRepositorio.findById(salaId);
        if (salaOpt.isEmpty()) {
            return;
        }

        Sala sala = salaOpt.get();

        // Limpiar respuestas de todos los jugadores
        for (Jugador jugador : sala.getJugadores()) {
            respuestaJugadorRepositorio.deleteAll(jugador.getRespuestas());
            jugador.setListo(false);
            jugador.setPuntajeTotal(0);
            jugador.setQuiereReiniciar(false);
            jugadorRepositorio.save(jugador);
        }

        // Eliminar partida anterior
        Optional<PartidaMulti> partidaOpt = partidaMultiRepositorio.findBySalaId(salaId);
        partidaOpt.ifPresent(partidaMultiRepositorio::delete);

        // Resetear sala
        sala.setEstado("ESPERANDO");
        sala.setJugadoresListosParaReiniciar(0);
        salaRepositorio.save(sala);

        // Notificar reinicio
        webSocketServicio.enviarMensajeASala(salaId, "SALA_REINICIADA", null);
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
