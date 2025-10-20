package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.servicios;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ConstructorPrompt {
    
    // Lista maestra de categorías disponibles (SEPARADA)
    private static final List<String> CATEGORIAS_DISPONIBLES = Arrays.asList(
        "Nombre", "País", "Ciudad", "Animal", "Fruta", "Verdura", 
        "Color", "Objeto", "Profesión", "Marca", "Verbo", "Deporte", 
        "Comida", "Bebida", "Planta", "Película", "Canción", "Libro", 
        "Instrumento Musical", "Prenda de Ropa"
    );
    
    /**
     * Construye el prompt para solicitar categorías aleatorias
     */
    public String construirPromptCategorias(int cantidad) {
        return String.format("""
            Genera datos aleatorios para una partida del juego Tutti Frutti:
            
            1. Selecciona exactamente %d categorías DIFERENTES y ALEATORIAS de esta lista:
            %s
            
            2. Selecciona una letra ALEATORIA del abecedario español (A-Z), excluyendo: Ñ, K, W, X
            
            IMPORTANTE:
            - Las categorías deben tener variedad de dificultad
            - La letra debe ser aleatoria y común
            - Devuelve EXCLUSIVAMENTE un JSON con este formato exacto:
            {
              "letra": "M",
              "categorias": ["Animal", "País", "Profesión", "Color", "Fruta"]
            }
            - NO agregues texto antes o después del JSON
            - NO uses bloques de código markdown
            - SOLO el JSON puro
            """,
            cantidad,
            String.join(", ", CATEGORIAS_DISPONIBLES)
        );
    }
    
    /**
     * Construye el prompt para validar respuestas
     */
    public String construirPromptValidacion(String letra, Map<String, String> respuestas) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Eres un juez estricto del juego Tutti Frutti. Valida las siguientes respuestas:\n\n");
        prompt.append("LETRA: ").append(letra).append("\n\n");
        prompt.append("RESPUESTAS DEL JUGADOR:\n");
        
        respuestas.forEach((categoria, respuesta) -> {
            String valorMostrar = (respuesta == null || respuesta.isEmpty()) ? "(vacío)" : respuesta;
            prompt.append("- ").append(categoria).append(": ").append(valorMostrar).append("\n");
        });
        
        prompt.append("\nCRITERIOS DE VALIDACIÓN:\n");
        prompt.append("1. La respuesta DEBE comenzar exactamente con la letra \"").append(letra).append("\"\n");
        prompt.append("2. La respuesta debe ser un ejemplo real y válido de la categoría\n");
        prompt.append("3. No se aceptan marcas comerciales en categorías genéricas\n");
        prompt.append("4. Respuestas vacías son automáticamente inválidas\n");
        prompt.append("5. Verifica ortografía básica\n\n");
        
        prompt.append("PUNTUACIÓN:\n");
        prompt.append("- Respuesta válida: 10 puntos\n");
        prompt.append("- Respuesta inválida: 0 puntos\n\n");
        
        prompt.append("Devuelve EXCLUSIVAMENTE un JSON con este formato exacto:\n");
        prompt.append("{\n");
        prompt.append("  \"validaciones\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"categoria\": \"Animal\",\n");
        prompt.append("      \"respuestaUsuario\": \"Mono\",\n");
        prompt.append("      \"esValida\": true,\n");
        prompt.append("      \"puntos\": 10,\n");
        prompt.append("      \"razon\": \"Animal válido que comienza con M\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"puntajeTotal\": 50,\n");
        prompt.append("  \"respuestasValidas\": 5,\n");
        prompt.append("  \"respuestasInvalidas\": 0\n");
        prompt.append("}\n\n");
        
        prompt.append("IMPORTANTE:\n");
        prompt.append("- NO agregues texto antes o después del JSON\n");
        prompt.append("- NO uses bloques de código markdown\n");
        prompt.append("- La razón debe ser breve (máximo 15 palabras)\n");
        prompt.append("- SOLO el JSON puro");
        
        return prompt.toString();
    }
    
    /**
     * Getter para la lista de categorías disponibles
     */
    public List<String> getCategoriasDisponibles() {
        return CATEGORIAS_DISPONIBLES;
    }
}