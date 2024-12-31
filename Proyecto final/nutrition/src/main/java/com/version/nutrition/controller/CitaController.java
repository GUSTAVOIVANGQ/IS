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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private NutricionistaService nutricionistaService;

    @Autowired
    private PacienteService pacienteService;

    // Mostrar formulario de agendar cita
    @GetMapping("/agendar")
    public String mostrarFormularioAgendar(Model model, Authentication auth) {
        String email = auth.getName();
        Paciente paciente = pacienteService.buscarPorEmail(email)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
        model.addAttribute("cita", new Cita());
        model.addAttribute("nutricionistas", nutricionistaService.listarTodos());
        model.addAttribute("paciente", paciente);
        return "citas/agendar";
    }

    // Procesar nueva cita
    @PostMapping("/agendar")
    public String agendarCita(@Valid @ModelAttribute Cita cita,
                            BindingResult result,
                            @RequestParam Long nutricionistaId,
                            Authentication auth,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "citas/agendar";
        }

        try {
            Nutricionista nutricionista = nutricionistaService.buscarPorId(nutricionistaId)
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
            
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            cita.setNutricionista(nutricionista);
            cita.setPaciente(paciente);
            citaService.agendarCita(nutricionistaId, cita);

            redirectAttributes.addFlashAttribute("mensaje", "Cita agendada exitosamente");
            return "redirect:/citas/mis-citas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agendar la cita: " + e.getMessage());
            return "redirect:/citas/agendar";
        }
    }

    // Listar citas del usuario actual
    @GetMapping("/mis-citas")
    public String listarMisCitas(Model model, Authentication auth) {
        String email = auth.getName();
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_NUTRICIONISTA"))) {
            Nutricionista nutricionista = nutricionistaService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
            model.addAttribute("citas", citaService.buscarCitasPorNutricionista(nutricionista.getId()));
        } else {
            Paciente paciente = pacienteService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            model.addAttribute("citas", citaService.buscarCitasPorPaciente(paciente.getId()));
        }
        return "citas/mis-citas";
    }

    // Ver detalle de cita
    @GetMapping("/{id}")
    public String verDetalleCita(@PathVariable Long id, Model model) {
        Cita cita = citaService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        model.addAttribute("cita", cita);
        return "citas/detalle";
    }

    // Cancelar cita
    @PostMapping("/{id}/cancelar")
    public String cancelarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            citaService.cancelarCita(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cita cancelada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar la cita: " + e.getMessage());
        }
        return "redirect:/citas/mis-citas";
    }

    // Confirmar cita
    @PostMapping("/{id}/confirmar")
    public String confirmarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            citaService.confirmarCita(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cita confirmada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al confirmar la cita: " + e.getMessage());
        }
        return "redirect:/citas/mis-citas";
    }

    // Completar cita
    @PostMapping("/{id}/completar")
    public String completarCita(@PathVariable Long id, 
                              @RequestParam String notas,
                              RedirectAttributes redirectAttributes) {
        try {
            citaService.completarCita(id);
            citaService.actualizarNotas(id, notas);
            redirectAttributes.addFlashAttribute("mensaje", "Cita marcada como completada");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al completar la cita: " + e.getMessage());
        }
        return "redirect:/citas/mis-citas";
    }

    // Reprogramar cita
    @PostMapping("/{id}/reprogramar")
    public String reprogramarCita(@PathVariable Long id,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
                                LocalDateTime nuevaFecha,
                                RedirectAttributes redirectAttributes) {
        try {
            citaService.reprogramarCita(id, nuevaFecha);
            redirectAttributes.addFlashAttribute("mensaje", "Cita reprogramada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al reprogramar la cita: " + e.getMessage());
        }
        return "redirect:/citas/mis-citas";
    }

    // Verificar disponibilidad de horario
    @GetMapping("/verificar-disponibilidad")
    @ResponseBody
    public boolean verificarDisponibilidad(@RequestParam Long nutricionistaId,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
                                         LocalDateTime fecha) {
        return citaService.verificarDisponibilidad(nutricionistaId, fecha);
    }
}
