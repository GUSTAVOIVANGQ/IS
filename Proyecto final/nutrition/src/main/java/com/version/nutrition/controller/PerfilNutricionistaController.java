package com.version.nutrition.controller;

import com.version.nutrition.model.Nutricionista;
import com.version.nutrition.service.NutricionistaService;
import com.version.nutrition.service.FileStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/nutricionista/perfil")
public class PerfilNutricionistaController {
    
    @Autowired
    private NutricionistaService nutricionistaService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @GetMapping
    public String verPerfil(Authentication auth, Model model) {
        try {
            String email = auth.getName();
            Nutricionista nutricionista = nutricionistaService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
            
            model.addAttribute("nutricionista", nutricionista);
            
            // Obtener y validar estadísticas
            Map<String, Object> estadisticas = nutricionistaService.obtenerEstadisticasPerfil(nutricionista.getId());
            if (estadisticas == null || estadisticas.isEmpty()) {
                estadisticas = new HashMap<>();
                estadisticas.put("totalPacientes", 0);
                estadisticas.put("totalConsultas", 0);
                estadisticas.put("calificacionPromedio", 0.0);
                estadisticas.put("planesCreados", 0);
                estadisticas.put("tasaExito", 0.0);
            }
            
            model.addAttribute("estadisticas", estadisticas);
            return "nutricionista/perfil/ver-perfil";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el perfil: " + e.getMessage());
            return "nutricionista/perfil/ver-perfil"; // Cambiado para no redirigir al dashboard
        }
    }

    @GetMapping("/editar")
    public String editarPerfil(Authentication auth, Model model) {
        String email = auth.getName();
        nutricionistaService.buscarPorEmail(email).ifPresent(nutricionista -> {
            model.addAttribute("nutricionista", nutricionista);
        });
        return "nutricionista/perfil/editar-perfil";
    }

    @PostMapping("/editar")
    public String actualizarPerfil(@ModelAttribute Nutricionista nutricionistaActualizado,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        try {
            String email = auth.getName();
            Nutricionista nutricionistaExistente = nutricionistaService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));

            // Mantener datos que no deben cambiar
            nutricionistaActualizado.setId(nutricionistaExistente.getId());
            nutricionistaActualizado.setEmail(nutricionistaExistente.getEmail());
            nutricionistaActualizado.setPassword(nutricionistaExistente.getPassword());
            nutricionistaActualizado.setRol(nutricionistaExistente.getRol());
            nutricionistaActualizado.setFotoPerfil(nutricionistaExistente.getFotoPerfil());
            
            // Actualizar el perfil
            nutricionistaService.actualizarPerfil(nutricionistaExistente.getId(), nutricionistaActualizado);
            
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado exitosamente");
            return "redirect:/nutricionista/perfil";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
            return "redirect:/nutricionista/perfil/editar";
        }
    }

    @PostMapping("/actualizar-foto")
    public String actualizarFotoPerfil(@RequestParam("foto") MultipartFile foto,
                                      Authentication auth,
                                      RedirectAttributes redirectAttributes) {
        try {
            String email = auth.getName();
            Nutricionista nutricionista = nutricionistaService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));

            String fileName = fileStorageService.storeFile(foto, "profile-photos");
            nutricionistaService.actualizarFotoPerfil(nutricionista.getId(), fileName);
            
            redirectAttributes.addFlashAttribute("mensaje", "Foto de perfil actualizada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la foto de perfil: " + e.getMessage());
        }
        return "redirect:/nutricionista/perfil";
    }

    @GetMapping("/horarios")
    public String editarHorarios(Authentication auth, Model model) {
        String email = auth.getName();
        nutricionistaService.buscarPorEmail(email).ifPresent(nutricionista -> {
            model.addAttribute("nutricionista", nutricionista);
            model.addAttribute("horarios", nutricionista.getHorarioAtencion());
        });
        return "nutricionista/perfil/horarios";
    }

    @PostMapping("/horarios")
    public String actualizarHorarios(@RequestParam String horarios,
                                   Authentication auth,
                                   RedirectAttributes redirectAttributes) {
        try {
            String email = auth.getName();
            Nutricionista nutricionista = nutricionistaService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
            
            if (nutricionistaService.validarDisponibilidadHorario(nutricionista.getId(), horarios)) {
                nutricionista.setHorarioAtencion(horarios);
                nutricionistaService.actualizarPerfil(nutricionista.getId(), nutricionista);
                redirectAttributes.addFlashAttribute("mensaje", "Horarios actualizados exitosamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "Formato de horario inválido");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar los horarios: " + e.getMessage());
        }
        return "redirect:/nutricionista/perfil/horarios";
    }
}
