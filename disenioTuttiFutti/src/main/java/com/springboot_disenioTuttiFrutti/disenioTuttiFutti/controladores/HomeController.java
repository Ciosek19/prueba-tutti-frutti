package com.springboot_disenioTuttiFrutti.disenioTuttiFutti.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/")
    public String index(@RequestParam String usuario) {
        if (usuario != null && !usuario.trim().isEmpty())
            return "redirect:/menu/";
        return "index";
    }
}
