package com.version.nutrition.controller;

import com.version.nutrition.model.Cita;
import com.version.nutrition.model.Nutricionista;
import com.version.nutrition.model.Paciente;
import com.version.nutrition.service.CitaService;
import com.version.nutrition.service.NutricionistaService;
import com.version.nutrition.service.PacienteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/paciente/citas")
public class CitaPacienteController {
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private PacienteService pacienteService;
    
    @Autowired
    private NutricionistaService nutricionistaService;

    // Listar citas del paciente
    @GetMapping
    public String listarCitas(Model model, Authentication auth) {
        Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        
        List<Cita> citas = citaService.buscarCitasPorPaciente(paciente.getId());
        model.addAttribute("citas", citas);
        return "pacientes/citas/lista";
    }

    // Mostrar formulario de creación de cita
    @GetMapping("/crear")
    public String mostrarFormularioCita(Model model) {
        model.addAttribute("cita", new Cita());
        model.addAttribute("nutricionistas", nutricionistaService.listarTodos());
        return "pacientes/citas/crear";
    }

    // Procesar la creación de una cita
    @PostMapping("/crear")
    public String crearCita(
            @RequestParam String tipoCita,
            @RequestParam String motivoConsulta,
            @RequestParam Long nutricionistaId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fecha,
            @RequestParam(defaultValue = "false") boolean esVirtual,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Obtener paciente y nutricionista
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
            // Crear objeto Cita con datos mínimos necesarios
            Cita cita = new Cita();
            cita.setFecha(fecha);
            cita.setTipoCita(tipoCita);
            cita.setMotivoConsulta(motivoConsulta);
            cita.setEsVirtual(esVirtual);
            cita.setPaciente(paciente);
            
            // Agendar la cita
            citaService.agendarCita(nutricionistaId, cita);
            
            redirectAttributes.addFlashAttribute("mensaje", "Cita agendada exitosamente");
            return "redirect:/paciente/citas";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agendar la cita: " + e.getMessage());
            return "redirect:/paciente/citas/crear";
        }
    }

    // Ver detalle de una cita
    @GetMapping("/{id}")
    public String verDetalleCita(@PathVariable Long id, Model model, Authentication auth) {
        Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
        Cita cita = citaService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
            
        // Verificar que la cita pertenezca al paciente
        if (!cita.getPaciente().getId().equals(paciente.getId())) {
            throw new RuntimeException("No autorizado para ver esta cita");
        }
        
        model.addAttribute("cita", cita);
        return "pacientes/citas/detalle";
    }

    // Cancelar una cita
    @PostMapping("/{id}/cancelar")
    public String cancelarCita(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
                
            Cita cita = citaService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
                
            if (!cita.getPaciente().getId().equals(paciente.getId())) {
                throw new RuntimeException("No autorizado para cancelar esta cita");
            }
            
            citaService.cancelarCita(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cita cancelada exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar la cita: " + e.getMessage());
        }
        
        return "redirect:/paciente/citas";
    }

    // Verificar disponibilidad de horario
    @GetMapping("/verificar-disponibilidad")
    @ResponseBody
    public boolean verificarDisponibilidad(
            @RequestParam Long nutricionistaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        return citaService.verificarDisponibilidad(nutricionistaId, fecha);
    }
}
