package com.version.nutrition.controller;

import com.version.nutrition.model.*;
import com.version.nutrition.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Controller
@RequestMapping("/paciente/seguimiento")
public class SeguimientoController {

    @Autowired
    private SeguimientoService seguimientoService;

    @Autowired
    private PacienteService pacienteService;

    // Mostrar formulario de registro de medidas
    @GetMapping("/registrar")
    public String mostrarFormularioRegistro(Model model, Authentication auth) {
        Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        
        model.addAttribute("paciente", paciente);
        model.addAttribute("seguimiento", new SeguimientoPeso());
        return "pacientes/seguimiento/registrar";
    }

    // Procesar registro de medidas
    @PostMapping("/registrar")
    public String registrarSeguimiento(
            @ModelAttribute SeguimientoPeso seguimiento,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
            seguimientoService.registrarSeguimiento(paciente.getId(), seguimiento);
            redirectAttributes.addFlashAttribute("mensaje", "Medidas registradas exitosamente");
            return "redirect:/paciente/seguimiento/historial";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar medidas: " + e.getMessage());
            return "redirect:/paciente/seguimiento/registrar";
        }
    }

    // Ver historial de medidas
    @GetMapping("/historial")
    public String verHistorial(Model model, Authentication auth) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            List<SeguimientoPeso> seguimientos = seguimientoService.obtenerHistorial(paciente.getId());
            if (seguimientos == null) {
                seguimientos = new ArrayList<>();
            }

            // Debug - imprimir cantidad de seguimientos
            System.out.println("Número de seguimientos encontrados: " + seguimientos.size());
            
            // Ordenar por fecha de registro, del más antiguo al más reciente
            seguimientos.sort((a, b) -> a.getFechaRegistro().compareTo(b.getFechaRegistro()));
            
            model.addAttribute("paciente", paciente);
            model.addAttribute("seguimientos", seguimientos);
            
            // Debug - verificar si hay datos después de ordenar
            if (!seguimientos.isEmpty()) {
                System.out.println("Primer seguimiento: " + seguimientos.get(0).getFechaRegistro());
                System.out.println("Último seguimiento: " + seguimientos.get(seguimientos.size()-1).getFechaRegistro());
                
                model.addAttribute("metricas", seguimientoService.obtenerMetricasProgreso(paciente.getId()));
                model.addAttribute("tendencias", seguimientoService.calcularTendencias(paciente.getId()));
            }
            
            return "pacientes/seguimiento/historial";
            
        } catch (Exception e) {
            e.printStackTrace(); // Agregar para ver el error completo
            model.addAttribute("error", "Error al cargar historial: " + e.getMessage());
            return "redirect:/paciente/dashboard";
        }
    }

    // Registrar actividad física
    @PostMapping("/actividad")
    @ResponseBody
    public Map<String, Object> registrarActividad(
            @RequestParam String tipoActividad,
            @RequestParam int duracionMinutos,
            @RequestParam int intensidad,
            Authentication auth) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
            seguimientoService.registrarActividadFisica(
                paciente.getId(), 
                tipoActividad, 
                duracionMinutos, 
                intensidad,
                LocalDateTime.now()
            );
            
            return Map.of(
                "success", true,
                "message", "Actividad registrada exitosamente"
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }

    // Obtener reporte de progreso
    @GetMapping("/reporte")
    public String verReporte(Model model, Authentication auth) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
            model.addAttribute("paciente", paciente);
            model.addAttribute("reporte", seguimientoService.generarReporteProgreso(paciente.getId()));
            model.addAttribute("alertas", seguimientoService.obtenerAlertas(paciente.getId()));
            
            return "pacientes/seguimiento/reporte";
        } catch (Exception e) {
            model.addAttribute("error", "Error al generar reporte: " + e.getMessage());
            return "redirect:/paciente/dashboard";
        }
    }
}
