package com.autenticacion.roles.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.ui.Model;

@Controller
public class UsuarioController {

    @GetMapping("/user/profile")
    public String verPerfil(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("usuario", user);
        return "perfil";
    }
}
