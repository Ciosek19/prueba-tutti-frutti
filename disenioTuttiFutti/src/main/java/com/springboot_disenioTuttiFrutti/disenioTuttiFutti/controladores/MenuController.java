package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/menu")
public class MenuController {


    @GetMapping("/")
    public String menuPrincipal() {
        return "menuPrincipal";
    }

    @PostMapping("/solitario")
    public String solitario() {
        return "redirect:/juego/solitario/";
    }

    @PostMapping("/multijugador")
    public String multijugador() {
        return "redirect:/";
    }
}
