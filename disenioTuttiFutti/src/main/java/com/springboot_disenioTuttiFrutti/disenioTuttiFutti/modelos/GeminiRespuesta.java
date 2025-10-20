package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GeminiRespuesta {
    
    @JsonProperty("candidates")
    private List<Candidato> candidatos;

    public List<Candidato> getCandidatos() {
        return candidatos;
    }

    public void setCandidatos(List<Candidato> candidatos) {
        this.candidatos = candidatos;
    }

    public static class Candidato {
        @JsonProperty("content")
        private Contenido contenido;

        public Contenido getContenido() {
            return contenido;
        }

        public void setContenido(Contenido contenido) {
            this.contenido = contenido;
        }
    }

    public static class Contenido {
        @JsonProperty("parts")
        private List<Parte> partes;

        public List<Parte> getPartes() {
            return partes;
        }

        public void setPartes(List<Parte> partes) {
            this.partes = partes;
        }
    }

    public static class Parte {
        @JsonProperty("text")
        private String texto;

        public String getTexto() {
            return texto;
        }

        public void setTexto(String texto) {
            this.texto = texto;
        }
    }
}