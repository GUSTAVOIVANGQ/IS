package com.version.nutrition.controller;

import com.version.nutrition.model.*;
import com.version.nutrition.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/nutricionista/planes")
public class PlanDietaController {

    private static final Logger logger = LoggerFactory.getLogger(PlanDietaController.class);

    @Autowired
    private PlanDietaService planDietaService;

    @Autowired
    private NutricionistaService nutricionistaService;

    @Autowired
    private GeneradorPlanService generadorPlanService;

    @Autowired
    private AlimentoService alimentoService; // Agregar este servicio

    // Listar todos los planes
    @GetMapping
    public String listarPlanes(Authentication auth, Model model) {
        String email = auth.getName();
        nutricionistaService.buscarPorEmail(email).ifPresent(nutricionista -> {
            model.addAttribute("nutricionista", nutricionista); // Agregar el nutricionista al modelo
            model.addAttribute("planes", nutricionista.getPlanesCreados());
        });
        return "nutricionista/planes/lista-planes";
    }

    // Formulario para crear nuevo plan
    @GetMapping("/crear")
    public String mostrarFormularioCreacion(@RequestParam Long pacienteId, Model model) {
        model.addAttribute("pacienteId", pacienteId);
        model.addAttribute("planDieta", new PlanDieta());
        return "nutricionista/planes/crear-plan";
    }

    // Procesar creación de plan
    @PostMapping("/crear")
    public String crearPlan(@RequestParam Long pacienteId,
                           @RequestParam Map<String, Object> criterios,
                           Authentication auth,
                           RedirectAttributes redirectAttributes) {
        try {
            String email = auth.getName();
            nutricionistaService.buscarPorEmail(email).ifPresent(nutricionista -> {
                PlanDieta plan = nutricionistaService.crearPlanPersonalizado(
                    nutricionista.getId(),
                    pacienteId,
                    criterios
                );
                redirectAttributes.addFlashAttribute("mensaje", "Plan creado exitosamente");
            });
            return "redirect:/nutricionista/planes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el plan: " + e.getMessage());
            return "redirect:/nutricionista/planes/crear?pacienteId=" + pacienteId;
        }
    }

    // Ver detalles del plan
    @GetMapping("/{id}")
    public String verPlan(@PathVariable Long id, Model model) {
        planDietaService.buscarPorId(id).ifPresent(plan -> {
            model.addAttribute("plan", plan);
            
            // Calcular métricas del plan
            Map<String, Object> metricas = new HashMap<>();
            metricas.put("caloriasActuales", plan.calcularCaloriasTotales());
            metricas.put("cumplimientoObjetivo", plan.validarObjetivoCalorico());
            model.addAttribute("metricas", metricas);
        });
        return "nutricionista/planes/ver-plan";
    }

    // Formulario para editar plan
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        planDietaService.buscarPorId(id).ifPresent(plan -> {
            model.addAttribute("plan", plan);
            List<Alimento> alimentos = alimentoService.listarTodos();
            logger.info("Cargando {} alimentos para el formulario", alimentos.size());
            model.addAttribute("alimentos", alimentos);
        });
        return "nutricionista/planes/editar-plan";
    }

    // Procesar edición de plan
    @PostMapping("/{id}/editar")
    public String editarPlan(@PathVariable Long id, 
                           @ModelAttribute PlanDieta planActualizado,
                           Authentication auth,
                           RedirectAttributes redirectAttributes) {
        try {
            // Obtener el plan existente
            PlanDieta planExistente = planDietaService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
            
            // Actualizar campos básicos
            planExistente.setNombre(planActualizado.getNombre());
            planExistente.setDescripcion(planActualizado.getDescripcion());
            planExistente.setObjetivo(planActualizado.getObjetivo());
            planExistente.setFechaInicio(planActualizado.getFechaInicio());
            planExistente.setFechaFin(planActualizado.getFechaFin());
            
            // Actualizar objetivos nutricionales
            planExistente.setCaloriasObjetivo(planActualizado.getCaloriasObjetivo());
            planExistente.setProteinasObjetivo(planActualizado.getProteinasObjetivo());
            planExistente.setCarbohidratosObjetivo(planActualizado.getCarbohidratosObjetivo());
            planExistente.setGrasasObjetivo(planActualizado.getGrasasObjetivo());
            
            // Actualizar restricciones
            planExistente.setEsVegetariano(planActualizado.isEsVegetariano());
            planExistente.setEsVegano(planActualizado.isEsVegano());
            planExistente.setLibreGluten(planActualizado.isLibreGluten());
            planExistente.setRestriccionesAdicionales(planActualizado.getRestriccionesAdicionales());
            
            // Guardar el plan actualizado
            String email = auth.getName();
            nutricionistaService.buscarPorEmail(email).ifPresent(nutricionista -> {
                nutricionistaService.modificarPlanDieta(nutricionista.getId(), planExistente);
            });
            
            redirectAttributes.addFlashAttribute("mensaje", "Plan actualizado exitosamente");
            return "redirect:/nutricionista/planes/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el plan: " + e.getMessage());
            return "redirect:/nutricionista/planes/" + id + "/editar";
        }
    }

    // Generar plan automático
    @PostMapping("/generar-automatico")
    public String generarPlanAutomatico(@RequestParam Long pacienteId,
                                      @RequestParam Map<String, Object> criterios,
                                      Authentication auth,
                                      RedirectAttributes redirectAttributes) {
        try {
            String email = auth.getName();
            nutricionistaService.buscarPorEmail(email).ifPresent(nutricionista -> {
                PlanDieta plan = nutricionistaService.crearPlanPersonalizado(
                    nutricionista.getId(),
                    pacienteId,
                    criterios
                );
                redirectAttributes.addFlashAttribute("mensaje", 
                    "Plan generado automáticamente con éxito");
            });
            return "redirect:/nutricionista/planes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error al generar el plan automático: " + e.getMessage());
            return "redirect:/nutricionista/pacientes/" + pacienteId;
        }
    }

    // Activar/Desactivar plan
    @PostMapping("/{id}/toggle-estado")
    @ResponseBody
    public Map<String, Object> toggleEstadoPlan(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            planDietaService.buscarPorId(id).ifPresent(plan -> {
                plan.setActivo(!plan.isActivo());
                planDietaService.crearPlan(plan);
                response.put("success", true);
                response.put("estado", plan.isActivo());
            });
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    // Filtrar planes
    @GetMapping("/filtrar")
    public String filtrarPlanes(@RequestParam Map<String, String> filtros, Model model) {
        List<PlanDieta> planesFilterados = planDietaService.buscarPlanesPorRestricciones(
            Boolean.parseBoolean(filtros.getOrDefault("vegetariano", "false")),
            Boolean.parseBoolean(filtros.getOrDefault("vegano", "false")),
            Boolean.parseBoolean(filtros.getOrDefault("sinGluten", "false"))
        );
        model.addAttribute("planes", planesFilterados);
        return "nutricionista/planes/lista-planes";
    }

    @PostMapping("/{planId}/agregar-alimento")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarAlimentoAPlan(
            @PathVariable Long planId,
            @RequestParam Long comidaId,
            @RequestParam Long alimentoId,
            @RequestParam Double cantidad) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // Buscar el plan
            PlanDieta plan = planDietaService.buscarPorId(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
            
            // Buscar el alimento
            Alimento alimento = alimentoService.buscarPorId(alimentoId)
                .orElseThrow(() -> new RuntimeException("Alimento no encontrado"));
            
            // Crear nueva porción
            AlimentoPorcion nuevaPorcion = new AlimentoPorcion(alimento, cantidad, "g");
            
            // Agregar a la comida correspondiente
            boolean alimentoAgregado = false;
            for (ComidaDiaria comida : plan.getComidas()) {
                if (comida.getId().equals(comidaId)) {
                    comida.getAlimentos().add(nuevaPorcion);
                    alimentoAgregado = true;
                    break;
                }
            }
            
            if (alimentoAgregado) {
                planDietaService.crearPlan(plan);
                response.put("success", true);
                response.put("mensaje", "Alimento agregado exitosamente");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Comida no encontrada en el plan");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{planId}/eliminar-alimento")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarAlimentoDePlan(
            @PathVariable Long planId,
            @RequestParam Long comidaId,
            @RequestParam Long alimentoId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // Buscar el plan
            PlanDieta plan = planDietaService.buscarPorId(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
            
            // Buscar la comida y eliminar el alimento
            boolean alimentoEliminado = false;
            for (ComidaDiaria comida : plan.getComidas()) {
                if (comida.getId().equals(comidaId)) {
                    comida.setAlimentos(
                        comida.getAlimentos().stream()
                            .filter(a -> !a.getId().equals(alimentoId))
                            .collect(Collectors.toList())
                    );
                    alimentoEliminado = true;
                    break;
                }
            }
            
            if (alimentoEliminado) {
                planDietaService.crearPlan(plan);
                response.put("success", true);
                response.put("mensaje", "Alimento eliminado exitosamente");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Alimento no encontrado en el plan");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
