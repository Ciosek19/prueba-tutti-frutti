package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.controladores;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.entidades.Jugador;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.entidades.PartidaMulti;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.entidades.Sala;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.servicios.SalaServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/juego/multijugador")
public class MultijugadorController {

    @Autowired
    private SalaServicio salaServicio;

    @GetMapping("/")
    public String listarSalas(Model model, HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuario");
        if (nombreUsuario == null) {
            System.out.println("ERROR NOMBRE USUARIO NULL");
            return "redirect:/";
        }

        List<Sala> salas = salaServicio.listarSalasDisponibles();
        model.addAttribute("salas", salas);
        model.addAttribute("nombreUsuario", nombreUsuario);

        return "listaSalas";
    }

    @PostMapping("/crear")
    public String crearSala(@RequestParam String nombreSala,
                           @RequestParam int capacidadMaxima,
                           @RequestParam int tiempoLimite,
                           HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuario");
        if (nombreUsuario == null) {
            return "redirect:/";
        }

        Sala sala = salaServicio.crearSala(nombreSala, capacidadMaxima, tiempoLimite);
        return "redirect:/juego/multijugador/sala/" + sala.getId();
    }

    @GetMapping("/sala/{salaId}")
    public String verSala(@PathVariable Long salaId, Model model, HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuario");
        if (nombreUsuario == null) {
            return "redirect:/";
        }

        Optional<Sala> salaOpt = salaServicio.obtenerSala(salaId);
        if (salaOpt.isEmpty()) {
            return "redirect:/juego/multijugador/";
        }

        Sala sala = salaOpt.get();

        // Unir al jugador a la sala si no está ya
        Jugador jugador = salaServicio.unirseASala(salaId, nombreUsuario, session.getId());
        if (jugador == null) {
            return "redirect:/juego/multijugador/";
        }

        session.setAttribute("jugadorId", jugador.getId());
        session.setAttribute("salaId", salaId);

        model.addAttribute("sala", sala);
        model.addAttribute("jugador", jugador);

        // Si la partida ya comenzó, redirigir al juego
        if (sala.getEstado().equals("JUGANDO")) {
            return "redirect:/juego/multijugador/jugar";
        }

        // Si la partida terminó, redirigir a resultados
        if (sala.getEstado().equals("FINALIZADA")) {
            return "redirect:/juego/multijugador/resultados";
        }

        return "salaEspera";
    }

    @PostMapping("/sala/{salaId}/iniciar")
    public String iniciarPartida(@PathVariable Long salaId, HttpSession session) throws JsonProcessingException {
        Long jugadorId = (Long) session.getAttribute("jugadorId");
        if (jugadorId == null) {
            return "redirect:/";
        }

        salaServicio.iniciarPartida(salaId);
        return "redirect:/juego/multijugador/jugar";
    }

    @GetMapping("/jugar")
    public String jugar(Model model, HttpSession session) throws JsonProcessingException {
        Long jugadorId = (Long) session.getAttribute("jugadorId");
        Long salaId = (Long) session.getAttribute("salaId");

        if (jugadorId == null || salaId == null) {
            return "redirect:/";
        }

        Optional<Sala> salaOpt = salaServicio.obtenerSala(salaId);
        Optional<PartidaMulti> partidaOpt = salaServicio.obtenerPartidaDeSala(salaId);

        if (salaOpt.isEmpty() || partidaOpt.isEmpty()) {
            return "redirect:/juego/multijugador/";
        }

        Sala sala = salaOpt.get();
        PartidaMulti partida = partidaOpt.get();

        // Si ya terminó, redirigir a resultados
        if (sala.getEstado().equals("FINALIZADA")) {
            return "redirect:/juego/multijugador/resultados";
        }

        // Buscar el jugador
        Optional<Jugador> jugadorOpt = sala.getJugadores().stream()
                .filter(j -> j.getId().equals(jugadorId))
                .findFirst();

        if (jugadorOpt.isEmpty()) {
            return "redirect:/juego/multijugador/";
        }

        Jugador jugador = jugadorOpt.get();

        // Si ya respondió, mostrar pantalla de espera
        if (jugador.isListo()) {
            model.addAttribute("sala", sala);
            return "esperandoJugadores";
        }

        // Calcular tiempo restante
        long tiempoRestante = Duration.between(LocalDateTime.now(), partida.getTiempoLimite()).getSeconds();
        if (tiempoRestante < 0) {
            tiempoRestante = 0;
        }

        // Mostrar formulario de juego
        List<String> categorias = salaServicio.obtenerCategoriasDeSala(salaId);
        model.addAttribute("letra", partida.getLetra());
        model.addAttribute("categorias", categorias);
        model.addAttribute("nombreUsuario", jugador.getNombre());
        model.addAttribute("tiempoRestante", tiempoRestante);
        model.addAttribute("salaId", salaId);

        return "multijugadorPantalla";
    }

    @PostMapping("/responder")
    @ResponseBody
    public ResponseEntity<Map<String, String>> responder(@RequestBody Map<String, String> params, HttpSession session) {
        Long jugadorId = (Long) session.getAttribute("jugadorId");
        Long salaId = (Long) session.getAttribute("salaId");

        if (jugadorId == null || salaId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sesión inválida"));
        }

        Optional<PartidaMulti> partidaOpt = salaServicio.obtenerPartidaDeSala(salaId);
        if (partidaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Partida no encontrada"));
        }

        PartidaMulti partida = partidaOpt.get();

        // Verificar que no se haya acabado el tiempo
        if (LocalDateTime.now().isAfter(partida.getTiempoLimite())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tiempo agotado"));
        }

        // Extraer solo las respuestas (sin nombreUsuario ni otros campos)
        Map<String, String> respuestas = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().equals("nombreUsuario")) {
                respuestas.put(entry.getKey(), entry.getValue());
            }
        }

        salaServicio.guardarRespuestas(jugadorId, respuestas, partida.getLetra());

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("/finalizar-por-tiempo/{salaId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> finalizarPorTiempo(@PathVariable Long salaId) {
        salaServicio.finalizarPartidaPorTiempo(salaId);
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @GetMapping("/resultados")
    public String verResultados(Model model, HttpSession session) {
        Long salaId = (Long) session.getAttribute("salaId");
        Long jugadorId = (Long) session.getAttribute("jugadorId");

        if (salaId == null) {
            return "redirect:/";
        }

        Optional<Sala> salaOpt = salaServicio.obtenerSala(salaId);
        if (salaOpt.isEmpty()) {
            return "redirect:/juego/multijugador/";
        }

        Sala sala = salaOpt.get();

        // Si aún no terminó, volver al juego
        if (!sala.getEstado().equals("FINALIZADA")) {
            return "redirect:/juego/multijugador/jugar";
        }

        // Ordenar jugadores por puntaje descendente
        List<Jugador> jugadoresOrdenados = sala.getJugadores().stream()
                .sorted((j1, j2) -> Integer.compare(j2.getPuntajeTotal(), j1.getPuntajeTotal()))
                .toList();

        model.addAttribute("sala", sala);
        model.addAttribute("jugadores", jugadoresOrdenados);
        model.addAttribute("jugadorId", jugadorId);

        return "multijugadorResultados";
    }

    @PostMapping("/reiniciar")
    @ResponseBody
    public ResponseEntity<Map<String, String>> solicitarReinicio(HttpSession session) {
        Long jugadorId = (Long) session.getAttribute("jugadorId");
        if (jugadorId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sesión inválida"));
        }

        boolean success = salaServicio.solicitarReinicio(jugadorId);
        if (success) {
            return ResponseEntity.ok(Map.of("status", "success"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "No se pudo solicitar reinicio"));
        }
    }

    @GetMapping("/api/salas")
    @ResponseBody
    public List<Sala> obtenerSalas() {
        return salaServicio.listarSalasDisponibles();
    }

    @GetMapping("/api/sala/{salaId}")
    @ResponseBody
    public ResponseEntity<Sala> obtenerSala(@PathVariable Long salaId) {
        Optional<Sala> sala = salaServicio.obtenerSala(salaId);
        return sala.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
