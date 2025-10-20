package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos.CategoriasRespuesta;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos.ValidacionesRespuesta;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.servicios.PartidaServicio;

import java.util.Map;

@Controller
@RequestMapping("/juego/solitario")
public class SolitarioController {

    private final PartidaServicio partidaServicio;

    @Autowired
    public SolitarioController(PartidaServicio partidaServicio) {
        this.partidaServicio = partidaServicio;
    }

    /*
     * Página inicial - Obtiene categorías y muestra formulario
     * GET /juego/solitario/
     */
    @GetMapping("/")
    public String inicio(Model model) {
        try {
            // Obtener categorías y letra de la IA
            CategoriasRespuesta datos = partidaServicio.iniciarPartida(5);
            
            // Enviar a la vista
            model.addAttribute("letra", datos.getLetra());
            model.addAttribute("categorias", datos.getCategorias());
            
            System.out.println("✅ Juego iniciado - Letra: " + datos.getLetra());
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al iniciar el juego: " + e.getMessage());
            System.err.println("❌ Error: " + e.getMessage());
        }
        
        return "solitarioPantalla";
    }

    /**
     * Validar respuestas y mostrar resultados
     * POST /juego/solitario/validar
     */
    @PostMapping("/validar")
    public String validar(
            @RequestParam String letra,
            @RequestParam Map<String, String> respuestas,
            Model model) {
        
        try {
            // Remover el parámetro 'letra' del mapa de respuestas
            respuestas.remove("letra");
            
            System.out.println("📝 Validando letra: " + letra);
            System.out.println("📋 Respuestas: " + respuestas);
            
            // Validar con la IA
            ValidacionesRespuesta resultado = partidaServicio.validarRespuestas(letra, respuestas);
            
            // Enviar resultados a la vista
            model.addAttribute("letra", letra);
            model.addAttribute("resultado", resultado);
            
            System.out.println("✅ Puntaje: " + resultado.getPuntajeTotal());
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al validar: " + e.getMessage());
            System.err.println("❌ Error: " + e.getMessage());
        }
        
        return "solitarioResultados";
    }
}