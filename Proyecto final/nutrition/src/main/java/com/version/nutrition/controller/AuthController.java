package com.version.nutrition.controller;

import com.version.nutrition.model.Nutricionista;
import com.version.nutrition.model.Paciente;
import com.version.nutrition.service.NutricionistaService;
import com.version.nutrition.service.PacienteService;
import com.version.nutrition.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private NutricionistaService nutricionistaService;

    // Página de inicio de sesión
    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    // Página de registro - Selección de tipo de usuario
    @GetMapping("/registro")
    public String registroForm() {
        return "auth/registro-seleccion";
    }

    // Formulario de registro para pacientes
    @GetMapping("/registro/paciente")
    public String registroPacienteForm(Model model) {
        System.out.println("Accediendo a formulario de registro de paciente");
        model.addAttribute("paciente", new Paciente());
        return "auth/registro-paciente";
    }

    // Procesamiento de registro de paciente
    @PostMapping("/registro/paciente")
    public String registrarPaciente(@Valid @ModelAttribute Paciente paciente, 
                                  BindingResult result, 
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/registro-paciente";
        }

        try {
            pacienteService.registrarPaciente(paciente);
            redirectAttributes.addFlashAttribute("mensaje", "Registro exitoso. Por favor, inicia sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            result.rejectValue("email", "error.paciente", "El correo electrónico ya está registrado");
            return "auth/registro-paciente";
        }
    }

    // Formulario de registro para nutricionistas
    @GetMapping("/registro/nutricionista")
    public String registroNutricionistaForm(Model model) {
        model.addAttribute("nutricionista", new Nutricionista());
        return "auth/registro-nutricionista";
    }

    // Procesamiento de registro de nutricionista
    @PostMapping("/registro/nutricionista")
    public String registrarNutricionista(@Valid @ModelAttribute Nutricionista nutricionista, 
                                       BindingResult result, 
                                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/registro-nutricionista";
        }

        try {
            nutricionistaService.registrarNutricionista(nutricionista);
            redirectAttributes.addFlashAttribute("mensaje", "Registro exitoso. Por favor, inicia sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            result.rejectValue("email", "error.nutricionista", "El correo electrónico ya está registrado");
            return "auth/registro-nutricionista";
        }
    }

    // Recuperación de contraseña - Solicitud
    @GetMapping("/recuperar-password")
    public String recuperarPasswordForm() {
        return "auth/recuperar-password";
    }

    // Procesamiento de solicitud de recuperación
    @PostMapping("/recuperar-password")
    public String procesarRecuperacionPassword(@RequestParam String email, 
                                             RedirectAttributes redirectAttributes) {
        try {
            String token = userService.iniciarRecuperacionPassword(email);
            // Aquí deberías enviar el email con el token
            redirectAttributes.addFlashAttribute("mensaje", 
                "Se ha enviado un enlace de recuperación a tu correo electrónico.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se encontró ninguna cuenta con ese email.");
            return "redirect:/recuperar-password";
        }
    }

    // Formulario de reset de contraseña
    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        if (userService.validarToken(token)) {
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
        return "redirect:/login?error=invalid-token";
    }

    // Procesamiento de reset de contraseña
    @PostMapping("/reset-password")
    public String procesarResetPassword(@RequestParam String token,
                                      @RequestParam String password,
                                      @RequestParam String confirmPassword,
                                      RedirectAttributes redirectAttributes) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            return "redirect:/reset-password?token=" + token;
        }

        try {
            userService.resetearPassword(token, password);
            redirectAttributes.addFlashAttribute("mensaje", "Contraseña actualizada exitosamente");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la contraseña");
            return "redirect:/reset-password?token=" + token;
        }
    }
}
