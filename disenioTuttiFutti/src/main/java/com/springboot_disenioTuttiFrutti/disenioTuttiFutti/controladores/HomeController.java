package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/")
    public String index(@RequestParam String usuario, HttpSession session) {
        if (usuario != null && !usuario.trim().isEmpty()) {
            // Guardar nombre en la sesión desde el inicio
            session.setAttribute("usuario", usuario);
            return "redirect:/menu/";
        }
        return "index";
    }
}
