package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.servicios;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos.CategoriasRespuesta;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos.ValidacionesRespuesta;

@Service
public class IARespuestaParserServicio {
    private final ObjectMapper objectMapper;

    public IARespuestaParserServicio() {
        this.objectMapper = new ObjectMapper();
        // Configurar para ser más tolerante
        this.objectMapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
    }

    public ValidacionesRespuesta parsearValidaciones(String textoRespuesta){
        try {
            String limpio = textoRespuesta.trim().replaceAll("^\\uFEFF", "");
            System.out.println("JSON validación a parsear: " + limpio);
            
            ValidacionesRespuesta response = objectMapper.readValue(limpio, ValidacionesRespuesta.class);
            
            if (response.getValidaciones() == null) {
                throw new RuntimeException("Respuesta parseada tiene validaciones null");
            }
            
            return response;
            
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error parseando validación.\n" +
                            "Texto recibido: [" + textoRespuesta + "]\n" +
                            "Error: " + e.getMessage(),
                    e);
        }
    }

    public CategoriasRespuesta parsearCategorias(String textoRespuesta) {
        try {
            // Limpiar caracteres invisibles
            String limpio = textoRespuesta.trim().replaceAll("^\\uFEFF", "");

            // Debug: imprime lo que vas a parsear
            System.out.println("JSON a parsear: " + limpio);

            // Parsear
            CategoriasRespuesta response = objectMapper.readValue(limpio, CategoriasRespuesta.class);

            // Validar que no sea null
            if (response.getLetra() == null || response.getCategorias() == null) {
                throw new RuntimeException("Respuesta parseada tiene campos null");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error parseando categorías.\n" +
                            "Texto recibido: [" + textoRespuesta + "]\n" +
                            "Error: " + e.getMessage(),
                    e);
        }
    }
}
