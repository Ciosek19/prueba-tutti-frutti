package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.modelos;

import java.util.List;

public class CategoriasDTO {

    private String letra;
    private List<String> categorias;

    public CategoriasDTO() {
    }

    public String getLetra() {
        return letra;
    }

    public void setLetra(String letra) {
        this.letra = letra;
    }

    public List<String> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<String> categorias) {
        this.categorias = categorias;
    }
}
