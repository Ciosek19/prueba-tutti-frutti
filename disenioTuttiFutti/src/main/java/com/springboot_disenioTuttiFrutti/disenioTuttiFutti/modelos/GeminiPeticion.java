package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class GeminiPeticion {

    @JsonProperty("contents")
    private List<Contenido> contents = new ArrayList<>();

    public List<Contenido> getContents() {
        return contents;
    }

    public void setContents(List<Contenido> contents) {
        this.contents = contents;
    }

    public static class Contenido {
        @JsonProperty("parts")
        private List<Parte> parts = new ArrayList<>();

        public List<Parte> getParts() {
            return parts;
        }

        public void setParts(List<Parte> parts) {
            this.parts = parts;
        }
    }

    public static class Parte {
        @JsonProperty("text")
        private String text;

        public Parte() {}

        public Parte(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
