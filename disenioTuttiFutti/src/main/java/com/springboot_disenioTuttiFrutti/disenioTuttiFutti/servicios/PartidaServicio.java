package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos.CategoriasDTO;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos.ValidacionesDTO;

import java.util.Map;

@Service
public class PartidaServicio {
    
    private final IAGeminiServicio iaGeminiServicio;
    private final IARespuestaParserServicio parser;
    private final ConstructorPrompt constructorPrompt;
    
    @Autowired
    public PartidaServicio(IAGeminiServicio iaGeminiServicio, 
                          IARespuestaParserServicio parser,
                          ConstructorPrompt constructorPrompt) {
        this.iaGeminiServicio = iaGeminiServicio;
        this.parser = parser;
        this.constructorPrompt = constructorPrompt;
    }
    
    /**
     * Inicia una nueva partida obteniendo letra y categorías
     * ORQUESTA TODO EL FLUJO:
     * 1. Construir prompt
     * 2. Llamar IA
     * 3. Parsear respuesta
     * 4. Devolver objeto listo
     */
    public CategoriasDTO iniciarPartida(int cantidadCategorias) {
        try {
            // 1. Construir el prompt
            String prompt = constructorPrompt.construirPromptCategorias(cantidadCategorias);
            
            System.out.println("📤 Solicitando categorías a la IA...");
            
            // 2. Enviar a Gemini (recibe String con JSON)
            String textoRespuesta = iaGeminiServicio.obtenerRespuestaIA(prompt);
            
            System.out.println("📥 Respuesta recibida de IA");
            
            // 3. Parsear String → Objeto Java
            CategoriasDTO categorias = parser.parsearCategorias(textoRespuesta);
            
            System.out.println("✅ Partida iniciada - Letra: " + categorias.getLetra() + 
                             ", Categorías: " + categorias.getCategorias());
            
            // 4. Devolver objeto listo para usar
            return categorias;
            
        } catch (Exception e) {
            System.err.println("❌ Error iniciando partida: " + e.getMessage());
            throw new RuntimeException("Error al iniciar partida: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valida las respuestas del jugador
     * ORQUESTA TODO EL FLUJO:
     * 1. Construir prompt con las respuestas
     * 2. Llamar IA
     * 3. Parsear respuesta
     * 4. Devolver objeto listo
     */
    public ValidacionesDTO validarRespuestas(String letra, Map<String, String> respuestas) {
        try {
            // 1. Construir el prompt
            String prompt = constructorPrompt.construirPromptValidacion(letra, respuestas);
            
            System.out.println("📤 Solicitando validación a la IA para letra: " + letra);
            System.out.println("   Respuestas: " + respuestas);
            
            // 2. Enviar a Gemini (recibe String con JSON)
            String textoRespuesta = iaGeminiServicio.obtenerRespuestaIA(prompt);
            
            System.out.println("📥 Validación recibida de IA");
            
            // 3. Parsear String → Objeto Java
            ValidacionesDTO validacion = parser.parsearValidaciones(textoRespuesta);
            
            System.out.println("✅ Validación completada - Puntaje: " + validacion.getPuntajeTotal());
            System.out.println("   Válidas: " + validacion.getRespuestasValidas() + 
                             " | Inválidas: " + validacion.getRespuestasInvalidas());
            
            // 4. Devolver objeto listo
            return validacion;
            
        } catch (Exception e) {
            System.err.println("❌ Error validando respuestas: " + e.getMessage());
            throw new RuntimeException("Error al validar respuestas: " + e.getMessage(), e);
        }
    }
}