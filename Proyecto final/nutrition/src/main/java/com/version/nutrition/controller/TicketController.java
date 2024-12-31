package com.version.nutrition.controller;

import com.version.nutrition.model.Ticket;
import com.version.nutrition.model.RespuestaTicket;
import com.version.nutrition.model.Paciente;
import com.version.nutrition.service.TicketService;
import com.version.nutrition.service.RespuestaTicketService;
import com.version.nutrition.service.PacienteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/paciente/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private RespuestaTicketService respuestaTicketService;

    @Autowired
    private PacienteService pacienteService;

    // Listar tickets del paciente
    @GetMapping
    public String listarTickets(Model model, Authentication auth) {
        Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
        List<Ticket> tickets = ticketService.listarTicketsPaciente(paciente.getId());
        model.addAttribute("tickets", tickets);
        return "pacientes/tickets/lista";
    }

    // Mostrar formulario de creación
    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("ticket", new Ticket());
        return "pacientes/tickets/crear";
    }

    // Procesar creación de ticket
    @PostMapping("/crear")
    public String crearTicket(@ModelAttribute Ticket ticket, 
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
            ticketService.crearTicket(paciente.getId(), ticket);
            redirectAttributes.addFlashAttribute("mensaje", "Ticket creado exitosamente");
            return "redirect:/paciente/tickets";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el ticket: " + e.getMessage());
            return "redirect:/paciente/tickets/crear";
        }
    }

    // Ver detalle de ticket
    @GetMapping("/{id}")
    public String verTicket(@PathVariable Long id, Model model) {
        Ticket ticket = ticketService.buscarPorId(id)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
            
        List<RespuestaTicket> respuestas = respuestaTicketService.obtenerRespuestasTicket(id);
        
        model.addAttribute("ticket", ticket);
        model.addAttribute("respuestas", respuestas);
        model.addAttribute("nuevaRespuesta", new RespuestaTicket());
        return "pacientes/tickets/detalle";
    }

    // Agregar respuesta a ticket
    @PostMapping("/{id}/responder")
    public String responderTicket(@PathVariable Long id,
                                @ModelAttribute RespuestaTicket respuesta,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
                
            respuestaTicketService.crearRespuesta(id, respuesta.getContenido(), paciente, false);
            redirectAttributes.addFlashAttribute("mensaje", "Respuesta agregada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar respuesta: " + e.getMessage());
        }
        return "redirect:/paciente/tickets/" + id;
    }

    // Marcar ticket como resuelto
    @PostMapping("/{id}/resolver")
    public String resolverTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ticketService.actualizarEstado(id, "RESUELTO");
            redirectAttributes.addFlashAttribute("mensaje", "Ticket marcado como resuelto");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al resolver el ticket: " + e.getMessage());
        }
        return "redirect:/paciente/tickets/" + id;
    }

    // Cerrar ticket
    @PostMapping("/{id}/cerrar")
    public String cerrarTicket(@PathVariable Long id, 
                              @RequestParam String motivo,
                              RedirectAttributes redirectAttributes) {
        try {
            ticketService.cerrarTicket(id, motivo);
            redirectAttributes.addFlashAttribute("mensaje", "Ticket cerrado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cerrar el ticket: " + e.getMessage());
        }
        return "redirect:/paciente/tickets";
    }

    // Obtener estadísticas del ticket
    @GetMapping("/{id}/estadisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(@PathVariable Long id) {
        Map<String, Object> estadisticas = respuestaTicketService.obtenerEstadisticas(id);
        return ResponseEntity.ok(estadisticas);
    }

    // Agregar etiquetas al ticket
    @PostMapping("/{id}/etiquetas")
    public String agregarEtiquetas(@PathVariable Long id,
                                 @RequestParam List<String> etiquetas,
                                 RedirectAttributes redirectAttributes) {
        try {
            ticketService.agregarEtiquetas(id, etiquetas);
            redirectAttributes.addFlashAttribute("mensaje", "Etiquetas agregadas exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar etiquetas: " + e.getMessage());
        }
        return "redirect:/paciente/tickets/" + id;
    }

    // Generar ticket desde cita
    @PostMapping("/generar-desde-cita/{citaId}")
    public String generarTicketDesdeCita(@PathVariable Long citaId,
                                        Authentication auth,
                                        RedirectAttributes redirectAttributes) {
        try {
            Paciente paciente = pacienteService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
            ticketService.generarTicketDesdeCita(citaId, paciente.getId());
            redirectAttributes.addFlashAttribute("mensaje", "Ticket de cita generado exitosamente");
            return "redirect:/paciente/tickets";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al generar ticket: " + e.getMessage());
            return "redirect:/paciente/citas";
        }
    }
}
