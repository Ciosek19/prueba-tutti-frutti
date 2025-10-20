package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.servicios;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos.GeminiPeticion;
import com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos.GeminiRespuesta;

@Service
public class IAGeminiServicio {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private static final String GEMINI_API_KEY = "AIzaSyAVikz9Im33EZncOlMBsMQseH4FxvOAP08";

    public String obtenerRespuestaIA(String preguntaUsuario) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders cabeceras = new HttpHeaders();
            cabeceras.setContentType(MediaType.APPLICATION_JSON);

            // Construir la petición
            GeminiPeticion peticion = new GeminiPeticion();
            GeminiPeticion.Contenido contenido = new GeminiPeticion.Contenido();
            contenido.getParts().add(new GeminiPeticion.Parte(preguntaUsuario));
            peticion.getContents().add(contenido);

            HttpEntity<GeminiPeticion> entidad = new HttpEntity<>(peticion, cabeceras);

            String urlCompleta = GEMINI_API_URL + "?key=" + GEMINI_API_KEY;

            ResponseEntity<GeminiRespuesta> respuesta = restTemplate.exchange(
                urlCompleta,
                HttpMethod.POST,
                entidad,
                GeminiRespuesta.class
            );

            if (respuesta.getBody() != null &&
                respuesta.getBody().getCandidatos() != null &&
                !respuesta.getBody().getCandidatos().isEmpty() &&
                respuesta.getBody().getCandidatos().get(0).getContenido() != null &&
                !respuesta.getBody().getCandidatos().get(0).getContenido().getPartes().isEmpty()) {

                return respuesta.getBody()
                    .getCandidatos()
                    .get(0)
                    .getContenido()
                    .getPartes()
                    .get(0)
                    .getTexto();
            } else {
                return "No se recibió respuesta de la IA";
            }

        } catch (Exception e) {
            throw new RuntimeException("Error llamando a Gemini API: " + e.getMessage(), e);
        }
    }
}
