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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/nutricionista/citas")
public class CitaNutricionistaController {
    private static final Logger logger = LoggerFactory.getLogger(CitaNutricionistaController.class);

    @Autowired
    private CitaService citaService;
    
    @Autowired
    private NutricionistaService nutricionistaService;

    @Autowired
    private PacienteService pacienteService;

    // Listar citas con relaciones completas
    @GetMapping
    public String listarCitas(Model model, Authentication auth) {
        nutricionistaService.buscarPorEmail(auth.getName())
            .ifPresent(nutricionista -> {
                model.addAttribute("citas", citaService.buscarCitasNutrionistaConRelaciones(nutricionista.getId()));
                model.addAttribute("nutricionista", nutricionista);
            });
        return "nutricionista/citas/lista-citas";
    }

    // Mostrar formulario de agendar con lista de pacientes disponibles
    @GetMapping("/agendar")
    public String mostrarFormularioAgendar(Model model, Authentication auth) {
        nutricionistaService.buscarPorEmail(auth.getName())
            .ifPresent(nutricionista -> {
                model.addAttribute("cita", new Cita());
                model.addAttribute("pacientes", nutricionista.getPacientes());
                model.addAttribute("tiposCita", new String[]{
                    "PRIMERA_VEZ", 
                    "SEGUIMIENTO", 
                    "CONTROL", 
                    "EMERGENCIA"
                });
            });
        // Corregir la ruta de retorno
        return "nutricionista/citas/agendar-cita"; // Cambio aquí
    }

    // Procesar agendamiento de cita con validaciones
    @PostMapping("/agendar")
    public String agendarCita(
            @RequestParam Long pacienteId,
            @RequestParam String fecha, // Cambiado a String
            @RequestParam String tipoCita,
            @RequestParam String motivoConsulta,
            @RequestParam(defaultValue = "false") boolean esVirtual,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            // Validar que la fecha no sea nula o vacía
            if (fecha == null || fecha.trim().isEmpty()) {
                throw new IllegalArgumentException("La fecha es requerida");
            }

            // La fecha ya debería venir en formato ISO-8601
            LocalDateTime fechaCita = LocalDateTime.parse(fecha);

            logger.info("Fecha recibida: " + fecha);
            logger.info("Fecha parseada: " + fechaCita);
            
            Nutricionista nutricionista = nutricionistaService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));

            Paciente paciente = pacienteService.buscarPorId(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            // Validar disponibilidad primero
//            if (!citaService.verificarDisponibilidad(nutricionista.getId(), fechaCita)) {
//                throw new IllegalStateException("El horario seleccionado no está disponible");
//            }

            // Crear y configurar la cita
            Cita cita = new Cita();
            cita.setFecha(fechaCita);
            cita.setTipoCita(tipoCita);
            cita.setMotivoConsulta(motivoConsulta);
            cita.setEsVirtual(esVirtual);
            cita.setPaciente(paciente);
            cita.setNutricionista(nutricionista);
            cita.setEstado("PENDIENTE");
            cita.setFechaCreacion(LocalDateTime.now());

            // Guardar la cita
            Cita citaGuardada = citaService.agendarCita(nutricionista.getId(), cita);
            
            if (citaGuardada != null && citaGuardada.getId() != null) {
                redirectAttributes.addFlashAttribute("mensaje", "Cita agendada exitosamente");
                return "redirect:/nutricionista/citas";
            } else {
                throw new RuntimeException("Error al guardar la cita en la base de datos");
            }
        } catch (Exception e) {
            logger.error("Error al agendar cita. Fecha recibida: '{}', Error: {}", fecha, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al agendar cita: " + e.getMessage());
            return "redirect:/nutricionista/citas/agendar";
        }
    }

    // Ver detalle de cita con relaciones completas
    @GetMapping("/{id}")
    public String verDetalleCita(@PathVariable Long id, Model model) {
        citaService.buscarPorId(id).ifPresent(cita -> {
            model.addAttribute("cita", cita);
            model.addAttribute("paciente", cita.getPaciente());
            model.addAttribute("historialCitas", 
                citaService.buscarCitasPorPaciente(cita.getPaciente().getId()));
        });
        return "nutricionista/citas/detalle-cita";
    }

    // Cancelar cita manteniendo historial
    @PostMapping("/{id}/cancelar")
    public String cancelarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            citaService.cancelarCitaConHistorial(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cita cancelada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar la cita: " + e.getMessage());
        }
        return "redirect:/nutricionista/citas";
    }

    // Completar cita y registrar seguimiento
    @PostMapping("/{id}/completar")
    public String completarCita(@PathVariable Long id, 
                              @RequestParam(required = false) String notas,
                              RedirectAttributes redirectAttributes) {
        try {
            Cita cita = citaService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
                
            citaService.completarCita(id);
            if (notas != null && !notas.isEmpty()) {
                citaService.actualizarNotas(id, notas);
            }
            
            // Actualizar fecha última consulta del paciente
            pacienteService.actualizarUltimaConsulta(cita.getPaciente().getId());
            
            redirectAttributes.addFlashAttribute("mensaje", "Cita completada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al completar la cita: " + e.getMessage());
        }
        return "redirect:/nutricionista/citas";
    }
}
