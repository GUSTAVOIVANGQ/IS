package com.version.nutrition.controller;

import com.version.nutrition.model.Paciente;
import com.version.nutrition.service.PacienteService;
import com.version.nutrition.service.UserService;
import com.version.nutrition.service.FileStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/paciente/profile")
public class PacienteProfileController {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public String verPerfil(Model model, Authentication auth) {
        Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        model.addAttribute("paciente", paciente);
        return "pacientes/profile/ver-perfil";
    }

    @GetMapping("/editar")
    public String editarPerfilForm(Model model, Authentication auth) {
        Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        model.addAttribute("paciente", paciente);
        return "pacientes/profile/editar-perfil";
    }

    @PostMapping("/editar")
    public String actualizarPerfil(
            @ModelAttribute Paciente pacienteActualizado,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Paciente pacienteExistente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
            // Mantener campos que no deben cambiar
            pacienteActualizado.setId(pacienteExistente.getId());
            pacienteActualizado.setEmail(pacienteExistente.getEmail());
            pacienteActualizado.setPassword(pacienteExistente.getPassword());
            pacienteActualizado.setRol(pacienteExistente.getRol());
            pacienteActualizado.setFotoPerfil(pacienteExistente.getFotoPerfil());
            //pacienteActualizado.setActivo(pacienteExistente.getActivo());
            
            // Actualizar el paciente con todos los campos del formulario
            pacienteService.actualizarPaciente(pacienteExistente.getId(), pacienteActualizado);
            
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado exitosamente");
            return "redirect:/paciente/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
            return "redirect:/paciente/profile/editar";
        }
    }

    @PostMapping("/actualizar-foto")
    public String actualizarFotoPerfil(
            @RequestParam("foto") MultipartFile foto,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            String fileName = fileStorageService.storeFile(foto, "profile-photos");
            userService.updateProfilePhoto(auth.getName(), fileName);
            redirectAttributes.addFlashAttribute("mensaje", "Foto de perfil actualizada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la foto de perfil");
        }
        return "redirect:/paciente/profile";
    }
}
