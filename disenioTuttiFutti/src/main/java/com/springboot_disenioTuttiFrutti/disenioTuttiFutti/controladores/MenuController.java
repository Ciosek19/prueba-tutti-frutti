package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/menu")
public class MenuController {


    @GetMapping("/")
    public String menuPrincipal(HttpSession session, Model model) {
        // Verificar si hay usuario en sesión
        String usuario = (String) session.getAttribute("usuario");
        
        if (usuario == null || usuario.isEmpty()) {
            // Si no hay usuario, redirigir al inicio
            return "redirect:/";
        }
        
        // Pasar el nombre del usuario a la vista (opcional)
        model.addAttribute("usuario", usuario);
        return "menuPrincipal";
    }

    @PostMapping("/solitario")
    public String solitario() {
        return "redirect:/juego/solitario/";
    }

    @PostMapping("/multijugador")
    public String multijugador() {
        // Redirigir a la página de multijugador (cuando la crees)
        // Por ahora, podrías crear una vista temporal
        return "redirect:/juego/multijugador/";
    }
}
