package com.version.nutrition.controller;

import com.version.nutrition.model.PlanDieta;
import com.version.nutrition.model.Paciente;
import com.version.nutrition.service.PacienteService;
import com.version.nutrition.service.PlanNutricionalPacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/paciente/plan-nutricional")
public class PlanNutricionalPacienteController {

    private static final Logger logger = LoggerFactory.getLogger(PlanNutricionalPacienteController.class);

    @Autowired
    private PlanNutricionalPacienteService planNutricionalService;

    @Autowired
    private PacienteService pacienteService;

    // Ver plan nutricional actual
    @GetMapping
    public String verPlanNutricional(Model model, Authentication auth) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            model.addAttribute("paciente", paciente);

            PlanDieta planActual = planNutricionalService.obtenerPlanActual(paciente.getId());
            
            // Agregar validación para plan nulo
            if (planActual != null) {
                model.addAttribute("planActual", planActual); // Cambiado de "plan" a "planActual"
                model.addAttribute("comidas", planActual.getComidas());
                model.addAttribute("metricas", planNutricionalService.obtenerEstadisticasPlan(planActual.getId()));
            } else {
                // Agregar mensaje cuando no hay plan activo
                model.addAttribute("mensaje", "No hay un plan nutricional activo en este momento.");
            }

            return "pacientes/plan-nutricional/ver-plan";
            
        } catch (Exception e) {
            logger.error("Error al cargar plan nutricional: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar el plan nutricional: " + e.getMessage());
            return "pacientes/plan-nutricional/ver-plan";
        }
    }

    // Ver comidas del día
    @GetMapping("/comidas/{dia}")
    public String verComidasDia(
            @PathVariable LocalDate dia,
            Model model,
            Authentication auth) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            PlanDieta planActual = planNutricionalService.obtenerPlanActual(paciente.getId());
            
            if (planActual != null) {
                model.addAttribute("comidas", planNutricionalService.obtenerComidasDelDia(planActual.getId(), dia));
                model.addAttribute("fecha", dia);
                model.addAttribute("plan", planActual);
            }

            return "pacientes/plan-nutricional/comida-diaria";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar las comidas del día: " + e.getMessage());
            return "redirect:/paciente/plan-nutricional";
        }
    }

    // Ver alimentos recomendados
    @GetMapping("/alimentos-recomendados")
    public String verAlimentosRecomendados(Model model, Authentication auth) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            model.addAttribute("alimentos", planNutricionalService.obtenerAlimentosRecomendados(paciente.getId()));
            return "pacientes/plan-nutricional/alimentos-recomendados";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los alimentos recomendados: " + e.getMessage());
            return "redirect:/paciente/plan-nutricional";
        }
    }

    // Registrar consumo de alimentos
    @PostMapping("/registrar-consumo")
    @ResponseBody
    public Map<String, Object> registrarConsumo(
            @RequestParam Long alimentoId,
            @RequestParam double cantidad,
            Authentication auth) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            planNutricionalService.registrarConsumoAlimento(
                paciente.getId(),
                alimentoId,
                cantidad,
                LocalDate.now()
            );

            return Map.of(
                "success", true,
                "message", "Consumo registrado exitosamente"
            );
            
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }

    // Ver calendario del plan
    @GetMapping("/calendario")
    public String verCalendario(Model model, Authentication auth) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            PlanDieta planActual = planNutricionalService.obtenerPlanActual(paciente.getId());
            
            if (planActual != null) {
                model.addAttribute("plan", planActual);
                model.addAttribute("comidas", planActual.getComidas());
                model.addAttribute("fechaInicio", planActual.getFechaInicio());
                model.addAttribute("fechaFin", planActual.getFechaFin());
            }

            return "pacientes/plan-nutricional/calendario";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el calendario: " + e.getMessage());
            return "redirect:/paciente/plan-nutricional";
        }
    }

    // Ver progreso del plan
    @GetMapping("/progreso")
    public String verProgreso(Model model, Authentication auth) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            PlanDieta planActual = planNutricionalService.obtenerPlanActual(paciente.getId());
            
            if (planActual != null) {
                model.addAttribute("plan", planActual);
                model.addAttribute("estadisticas", planNutricionalService.obtenerEstadisticasPlan(planActual.getId()));
                model.addAttribute("progreso", paciente.calcularProgreso());
            }

            return "pacientes/plan-nutricional/progreso";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el progreso: " + e.getMessage());
            return "redirect:/paciente/plan-nutricional";
        }
    }
}
