package com.version.nutrition.controller;

import com.version.nutrition.model.Paciente;
import com.version.nutrition.model.Cita;
import com.version.nutrition.service.PacienteService;
import com.version.nutrition.service.CitaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/paciente")
public class PacienteController {
    
    @Autowired
    private PacienteService pacienteService;
    
    @Autowired
    private CitaService citaService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        try {
            // Obtener el paciente actual
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
            // Inicializar seguimiento si es null
            if (paciente.getSeguimientoPeso() == null) {
                paciente.setSeguimientoPeso(new ArrayList<>());
            }
            
            // Inicializar historial de mediciones si es null
            if (paciente.getHistorialMediciones() == null) {
                paciente.setHistorialMediciones(new ArrayList<>());
            }
            
            // Obtener la próxima cita
            Cita proximaCita = citaService.buscarProximaCitaPaciente(
                paciente.getId(), 
                LocalDateTime.now()
            );

            // Agregar atributos al modelo
            model.addAttribute("paciente", paciente);
            model.addAttribute("proximaCita", proximaCita);
            model.addAttribute("ultimasMediciones", paciente.getHistorialMediciones());
            model.addAttribute("planActual", paciente.getPlanesDeDieta().stream()
                .filter(plan -> plan.isActivo())
                .findFirst()
                .orElse(null));

            return "pacientes/dashboard";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el dashboard: " + e.getMessage());
            return "error";
        }
    }
}
