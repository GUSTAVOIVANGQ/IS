package com.version.nutrition.controller;

import com.version.nutrition.dto.PacienteFiltro;
import com.version.nutrition.model.*;
import com.version.nutrition.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/nutricionista")
public class NutricionistaController {

    @Autowired
    private NutricionistaService nutricionistaService;

    @Autowired
    private PacienteService pacienteService;

    // Dashboard del nutricionista
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String email = auth.getName();
        Nutricionista nutricionista = nutricionistaService.buscarPorEmail(email)
            .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
        
        // Asegurar que los objetivos coincidan exactamente con los valores en el modelo Paciente
        List<String> objetivos = Arrays.asList(
            "PERDIDA_PESO",
            "GANANCIA_PESO", 
            "MANTENIMIENTO",
            "RENDIMIENTO_DEPORTIVO"
        );
        
        model.addAttribute("objetivos", objetivos);
        model.addAttribute("nutricionista", nutricionista);
        return "nutricionista/dashboard";
    }

    // Lista de pacientes con filtros
    @GetMapping("/pacientes")
    public String listarPacientes(
            @ModelAttribute PacienteFiltro filtro,
            Authentication auth, 
            Model model) {
        try {
            String email = auth.getName();
            List<Paciente> pacientes;
            
            if (filtro != null && filtro.tieneValores()) {
                // Aplicar filtros si existen
                pacientes = filtrarPacientes(
                    nutricionistaService.listarPacientes(email), 
                    filtro
                );
            } else {
                // Obtener todos los pacientes si no hay filtros
                pacientes = nutricionistaService.listarPacientes(email);
            }
            
            model.addAttribute("pacientes", pacientes);
            model.addAttribute("filtro", filtro != null ? filtro : new PacienteFiltro());
            return "nutricionista/pacientes/lista-pacientes";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la lista de pacientes: " + e.getMessage());
            return "nutricionista/pacientes/lista-pacientes";
        }
    }

    // Mostrar formulario para nuevo paciente
    @GetMapping("/pacientes/nuevo")
    public String mostrarFormularioNuevoPaciente(Model model) {
        model.addAttribute("paciente", new Paciente());
        return "nutricionista/pacientes/nuevo-paciente";
    }

    // Procesar el registro de nuevo paciente
    @PostMapping("/pacientes/nuevo")
    public String registrarNuevoPaciente(
            @ModelAttribute Paciente paciente,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            String email = auth.getName();
            Nutricionista nutricionista = nutricionistaService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));

            Paciente nuevoPaciente = nutricionistaService.registrarNuevoPaciente(
                nutricionista.getId(), 
                paciente
            );

            redirectAttributes.addFlashAttribute("mensaje", 
                "Paciente registrado exitosamente: " + nuevoPaciente.getNombre());
            return "redirect:/nutricionista/pacientes";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al registrar paciente: " + e.getMessage());
            return "redirect:/nutricionista/pacientes/nuevo";
        }
    }

    // Método para aplicar filtros
    private List<Paciente> filtrarPacientes(List<Paciente> pacientes, PacienteFiltro filtro) {
        return pacientes.stream()
            .filter(p -> filtro.getEstadoNutricional() == null || 
                        p.getEstadoNutricional().equals(filtro.getEstadoNutricional()))
            .filter(p -> filtro.getEstadoSeguimiento() == null || 
                        p.getEstadoSeguimiento().equals(filtro.getEstadoSeguimiento()))
            .filter(p -> filtro.getImcMin() == null || 
                        p.getIndiceMasaCorporal() >= filtro.getImcMin())
            .filter(p -> filtro.getImcMax() == null || 
                        p.getIndiceMasaCorporal() <= filtro.getImcMax())
            .filter(p -> filtro.getObjetivo() == null || 
                        p.getObjetivoNutricional().equals(filtro.getObjetivo()))
            .filter(p -> filtro.getNombre() == null || 
                        p.getNombre().toLowerCase().contains(filtro.getNombre().toLowerCase()))
            .filter(p -> filtro.getApellido() == null || 
                        p.getApellido().toLowerCase().contains(filtro.getApellido().toLowerCase()))
            .filter(p -> filtro.getFechaUltimaConsultaDesde() == null || 
                        p.getUltimaConsulta() != null && 
                        !p.getUltimaConsulta().isBefore(filtro.getFechaUltimaConsultaDesde()))
            .filter(p -> filtro.getFechaUltimaConsultaHasta() == null || 
                        p.getUltimaConsulta() != null && 
                        !p.getUltimaConsulta().isAfter(filtro.getFechaUltimaConsultaHasta()))
            .collect(Collectors.toList());
    }

    // Endpoint para aplicar filtros vía AJAX
    @PostMapping("/pacientes/filtrar")
    @ResponseBody
    @CrossOrigin
    public ResponseEntity<Map<String, Object>> filtrarPacientesAjax(
            @RequestBody PacienteFiltro filtro,
            Authentication auth) {
        try {
            String email = auth.getName();
            List<Paciente> pacientesFiltrados = nutricionistaService.filtrarPacientes(email, filtro);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pacientes", pacientesFiltrados);
            response.put("total", pacientesFiltrados.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Ver detalle de paciente
    @GetMapping("/pacientes/{id}")
    public String verPaciente(@PathVariable Long id, Model model) {
        Paciente paciente = pacienteService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        model.addAttribute("paciente", paciente);
        return "nutricionista/pacientes/detalle-paciente";
    }

    // Formulario para editar paciente
    @GetMapping("/pacientes/{id}/editar")
    public String editarPacienteForm(@PathVariable Long id, Model model) {
        Paciente paciente = pacienteService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        model.addAttribute("paciente", paciente);
        return "nutricionista/pacientes/editar-paciente";
    }

    // Procesar edición de paciente
    @PostMapping("/pacientes/{id}/editar")
    public String actualizarPaciente(@PathVariable Long id, 
                                   @ModelAttribute Paciente paciente,
                                   RedirectAttributes redirectAttributes) {
        try {
            pacienteService.actualizarPaciente(id, paciente);
            redirectAttributes.addFlashAttribute("mensaje", "Paciente actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar paciente");
        }
        return "redirect:/nutricionista/pacientes/" + id;
    }

    // Ver historial médico
    @GetMapping("/pacientes/{id}/historial")
    public String verHistorialMedico(@PathVariable Long id, Model model) {
        Paciente paciente = pacienteService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        model.addAttribute("paciente", paciente);
        model.addAttribute("historialMediciones", paciente.getHistorialMediciones());
        return "nutricionista/pacientes/historial-medico";
    }

    @GetMapping("/pacientes/debug")
    @ResponseBody
    public String debugPacientes(Authentication auth) {
        String email = auth.getName();
        Nutricionista nutricionista = nutricionistaService.buscarPorEmail(email).orElse(null);
        if (nutricionista == null) return "Nutricionista no encontrado";
        
        List<Paciente> pacientes = nutricionista.getPacientes();
        StringBuilder debug = new StringBuilder();
        debug.append("Nutricionista: ").append(nutricionista.getEmail()).append("\n");
        debug.append("Número de pacientes: ").append(pacientes != null ? pacientes.size() : "null").append("\n");
        
        if (pacientes != null) {
            pacientes.forEach(p -> debug.append("Paciente: ")
                .append(p.getNombre())
                .append(" (").append(p.getEmail()).append(")\n"));
        }
        
        return debug.toString();
    }    
}
