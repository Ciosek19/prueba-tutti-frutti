package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ValidacionesDTO {
    
    private List<ValidacionItem> validaciones;
    private int puntajeTotal;
    private int respuestasValidas;
    private int respuestasInvalidas;
    
    // Constructor vacío (necesario para Jackson)
    public ValidacionesDTO() {}
    
    // Constructor completo
    public ValidacionesDTO(List<ValidacionItem> validaciones, int puntajeTotal, 
                               int respuestasValidas, int respuestasInvalidas) {
        this.validaciones = validaciones;
        this.puntajeTotal = puntajeTotal;
        this.respuestasValidas = respuestasValidas;
        this.respuestasInvalidas = respuestasInvalidas;
    }
    
    // Getters y Setters
    public List<ValidacionItem> getValidaciones() {
        return validaciones;
    }
    
    public void setValidaciones(List<ValidacionItem> validaciones) {
        this.validaciones = validaciones;
    }
    
    public int getPuntajeTotal() {
        return puntajeTotal;
    }
    
    public void setPuntajeTotal(int puntajeTotal) {
        this.puntajeTotal = puntajeTotal;
    }
    
    public int getRespuestasValidas() {
        return respuestasValidas;
    }
    
    public void setRespuestasValidas(int respuestasValidas) {
        this.respuestasValidas = respuestasValidas;
    }
    
    public int getRespuestasInvalidas() {
        return respuestasInvalidas;
    }
    
    public void setRespuestasInvalidas(int respuestasInvalidas) {
        this.respuestasInvalidas = respuestasInvalidas;
    }
    
    @Override
    public String toString() {
        return "ValidacionRespuesta{" +
                "validaciones=" + validaciones +
                ", puntajeTotal=" + puntajeTotal +
                ", respuestasValidas=" + respuestasValidas +
                ", respuestasInvalidas=" + respuestasInvalidas +
                '}';
    }
    
    // Clase interna para cada validación individual
    public static class ValidacionItem {
        
        private String categoria;
        private String respuestaUsuario;
        
        @JsonProperty("esValida")
        private boolean esValida;
        
        private int puntos;
        private String razon;
        
        // Constructor vacío
        public ValidacionItem() {}
        
        // Constructor completo
        public ValidacionItem(String categoria, String respuestaUsuario, 
                             boolean esValida, int puntos, String razon) {
            this.categoria = categoria;
            this.respuestaUsuario = respuestaUsuario;
            this.esValida = esValida;
            this.puntos = puntos;
            this.razon = razon;
        }
        
        // Getters y Setters
        public String getCategoria() {
            return categoria;
        }
        
        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }
        
        public String getRespuestaUsuario() {
            return respuestaUsuario;
        }
        
        public void setRespuestaUsuario(String respuestaUsuario) {
            this.respuestaUsuario = respuestaUsuario;
        }
        
        public boolean isEsValida() {
            return esValida;
        }
        
        public void setEsValida(boolean esValida) {
            this.esValida = esValida;
        }
        
        public int getPuntos() {
            return puntos;
        }
        
        public void setPuntos(int puntos) {
            this.puntos = puntos;
        }
        
        public String getRazon() {
            return razon;
        }
        
        public void setRazon(String razon) {
            this.razon = razon;
        }
        
        @Override
        public String toString() {
            return "ValidacionItem{" +
                    "categoria='" + categoria + '\'' +
                    ", respuestaUsuario='" + respuestaUsuario + '\'' +
                    ", esValida=" + esValida +
                    ", puntos=" + puntos +
                    ", razon='" + razon + '\'' +
                    '}';
        }
    }
}