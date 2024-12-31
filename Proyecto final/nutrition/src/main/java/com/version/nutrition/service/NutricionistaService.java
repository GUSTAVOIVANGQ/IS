package com.version.nutrition.service;

import com.version.nutrition.dto.PacienteFiltro;
import com.version.nutrition.model.*;
import com.version.nutrition.repository.NutricionistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class NutricionistaService {

    @Autowired
    private NutricionistaRepository nutricionistaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GeneradorPlanService generadorPlanService;

    // Operaciones CRUD básicas
    public Nutricionista registrarNutricionista(Nutricionista nutricionista) {
        nutricionista.setPassword(passwordEncoder.encode(nutricionista.getPassword()));
        nutricionista.setRol("ROLE_NUTRICIONISTA");
        nutricionista.setActivo(true);
        return nutricionistaRepository.save(nutricionista);
    }

    // Agregar nuevo método para registrar paciente
    public Paciente registrarNuevoPaciente(Long nutricionistaId, Paciente paciente) {
        Nutricionista nutricionista = nutricionistaRepository.findById(nutricionistaId)
            .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));

        // Configurar valores por defecto del paciente
        paciente.setPassword(passwordEncoder.encode(paciente.getPassword()));
        paciente.setRol("ROLE_PACIENTE");
        paciente.setActivo(true);
        paciente.setFechaRegistro(LocalDate.now());
        paciente.setEstadoSeguimiento("ACTIVO");

        // Establecer foto de perfil predeterminada basada en el género
        String fotoPerfil = switch (paciente.getGenero()) {
            case "MASCULINO" -> "paciente-male-default.png";
            case "FEMENINO" -> "paciente-female-default.png";
            default -> "paciente-default.png";
        };
        paciente.setFotoPerfil(fotoPerfil);

        // Calcular métricas iniciales
        if (paciente.getEstatura() > 0 && paciente.getPeso() > 0) {
            paciente.calcularIMC();
            paciente.calcularTasaMetabolicaBasal();
        }

        // Agregar el paciente al nutricionista
        nutricionista.agregarPaciente(paciente);
        nutricionistaRepository.save(nutricionista);

        return paciente;
    }

    public Optional<Nutricionista> buscarPorId(Long id) {
        return nutricionistaRepository.findById(id);
    }

    public Optional<Nutricionista> buscarPorEmail(String email) {
        return nutricionistaRepository.findByEmail(email);
    }

    public List<Nutricionista> listarTodos() {
        return nutricionistaRepository.findAll();
    }

    // Gestión de pacientes
    public void agregarPaciente(Long nutricionistaId, Paciente paciente) {
        nutricionistaRepository.findById(nutricionistaId).ifPresent(nutricionista -> {
            nutricionista.agregarPaciente(paciente);
            nutricionistaRepository.save(nutricionista);
        });
    }

    public List<Paciente> listarPacientes(Long nutricionistaId) {
        return nutricionistaRepository.findById(nutricionistaId)
                .map(Nutricionista::getPacientes)
                .orElse(new ArrayList<>());
    }

    public List<Paciente> listarPacientes(String email) {
        return nutricionistaRepository.findByEmail(email)
                .map(nutricionista -> {
                    // Forzar la carga de la relación
                    List<Paciente> pacientes = nutricionista.getPacientes();
                    if (pacientes == null) {
                        pacientes = new ArrayList<>();
                    }
                    return pacientes;
                })
                .orElse(new ArrayList<>());
    }

    // Método principal de filtrado
    public List<Paciente> filtrarPacientes(String email, PacienteFiltro filtro) {
        List<Paciente> pacientes = listarPacientes(email);
        
        if (filtro == null || !filtro.tieneValores()) {
            return pacientes;
        }

        return pacientes.stream()
            .filter(p -> cumpleFiltrosNutricionales(p, filtro))
            .filter(p -> cumpleFiltrosSeguimiento(p, filtro))
            .filter(p -> cumpleFiltrosPersonales(p, filtro))
            .filter(p -> cumpleFiltrosFecha(p, filtro))
            .collect(Collectors.toList());
    }

    // Métodos auxiliares de filtrado
    private boolean cumpleFiltrosNutricionales(Paciente paciente, PacienteFiltro filtro) {
        boolean cumpleEstadoNutricional = filtro.getEstadoNutricional() == null ||
            paciente.getEstadoNutricional().equals(filtro.getEstadoNutricional());

        boolean cumpleIMC = true;
        if (filtro.getImcMin() != null) {
            cumpleIMC = paciente.getIndiceMasaCorporal() >= filtro.getImcMin();
        }
        if (filtro.getImcMax() != null) {
            cumpleIMC = cumpleIMC && paciente.getIndiceMasaCorporal() <= filtro.getImcMax();
        }

        boolean cumpleObjetivo = filtro.getObjetivo() == null ||
            paciente.getObjetivoNutricional().equals(filtro.getObjetivo());

        return cumpleEstadoNutricional && cumpleIMC && cumpleObjetivo;
    }

    private boolean cumpleFiltrosSeguimiento(Paciente paciente, PacienteFiltro filtro) {
        return filtro.getEstadoSeguimiento() == null ||
            paciente.getEstadoSeguimiento().equals(filtro.getEstadoSeguimiento());
    }

    private boolean cumpleFiltrosPersonales(Paciente paciente, PacienteFiltro filtro) {
        boolean cumpleNombre = filtro.getNombre() == null ||
            paciente.getNombre().toLowerCase().contains(filtro.getNombre().toLowerCase());

        boolean cumpleApellido = filtro.getApellido() == null ||
            paciente.getApellido().toLowerCase().contains(filtro.getApellido().toLowerCase());

        return cumpleNombre && cumpleApellido;
    }

    private boolean cumpleFiltrosFecha(Paciente paciente, PacienteFiltro filtro) {
        boolean cumpleFechaDesde = filtro.getFechaUltimaConsultaDesde() == null ||
            (paciente.getUltimaConsulta() != null &&
             !paciente.getUltimaConsulta().isBefore(filtro.getFechaUltimaConsultaDesde()));

        boolean cumpleFechaHasta = filtro.getFechaUltimaConsultaHasta() == null ||
            (paciente.getUltimaConsulta() != null &&
             !paciente.getUltimaConsulta().isAfter(filtro.getFechaUltimaConsultaHasta()));

        return cumpleFechaDesde && cumpleFechaHasta;
    }

    // Modificar método existente para incluir la opción de filtrado
    public List<Paciente> listarPacientes(String email, PacienteFiltro filtro) {
        if (filtro == null || !filtro.tieneValores()) {
            return listarPacientes(email);
        }
        return filtrarPacientes(email, filtro);
    }

    // Gestión de citas
    public boolean agendarCita(Long nutricionistaId, Cita cita) {
        return nutricionistaRepository.findById(nutricionistaId)
                .map(nutricionista -> {
                    if (nutricionista.verificarDisponibilidad(cita.getFecha())) {
                        nutricionista.getCitas().add(cita);
                        nutricionistaRepository.save(nutricionista);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    public List<Cita> listarCitasFuturas(Long nutricionistaId) {
        LocalDateTime ahora = LocalDateTime.now();
        return nutricionistaRepository.findById(nutricionistaId)
                .map(nutricionista -> nutricionista.getCitas().stream()
                        .filter(cita -> cita.getFecha().isAfter(ahora))
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // Gestión de planes de dieta
    public PlanDieta crearPlanDieta(Long nutricionistaId, PlanDieta plan) {
        return nutricionistaRepository.findById(nutricionistaId)
                .map(nutricionista -> {
                    nutricionista.getPlanesCreados().add(plan);
                    nutricionistaRepository.save(nutricionista);
                    return plan;
                })
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
    }

    // Gestión de planes nutricionales
    public PlanDieta crearPlanPersonalizado(Long nutricionistaId, Long pacienteId, Map<String, Object> criterios) {
        Nutricionista nutricionista = nutricionistaRepository.findById(nutricionistaId)
            .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
            
        Paciente paciente = nutricionista.getPacientes().stream()
            .filter(p -> p.getId().equals(pacienteId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        PlanDieta plan = generadorPlanService.generarPlanPersonalizado(paciente, criterios);
        plan.setNutricionista(nutricionista);
        nutricionista.getPlanesCreados().add(plan);
        
        return nutricionistaRepository.save(nutricionista).getPlanesCreados().get(
            nutricionista.getPlanesCreados().size() - 1
        );
    }

    public void modificarPlanDieta(Long nutricionistaId, PlanDieta planModificado) {
        nutricionistaRepository.findById(nutricionistaId).ifPresent(nutricionista -> {
            List<PlanDieta> planes = nutricionista.getPlanesCreados();
            for (int i = 0; i < planes.size(); i++) {
                if (planes.get(i).getId().equals(planModificado.getId())) {
                    planes.set(i, planModificado);
                    break;
                }
            }
            nutricionistaRepository.save(nutricionista);
        });
    }

    // Seguimiento de pacientes
    public void registrarSeguimiento(Long nutricionistaId, Long pacienteId, SeguimientoPeso seguimiento) {
        nutricionistaRepository.findById(nutricionistaId).ifPresent(nutricionista -> {
            nutricionista.getPacientes().stream()
                .filter(p -> p.getId().equals(pacienteId))
                .findFirst()
                .ifPresent(paciente -> {
                    paciente.getSeguimientoPeso().add(seguimiento);
                    nutricionistaRepository.save(nutricionista);
                });
        });
    }

    public List<Map<String, Object>> obtenerEstadisticasPaciente(Long nutricionistaId, Long pacienteId) {
        List<Map<String, Object>> estadisticas = new ArrayList<>();
        
        nutricionistaRepository.findById(nutricionistaId).ifPresent(nutricionista -> {
            nutricionista.getPacientes().stream()
                .filter(p -> p.getId().equals(pacienteId))
                .findFirst()
                .ifPresent(paciente -> {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("progresoObjetivo", paciente.calcularProgreso());
                    stats.put("cumplimientoPlan", calcularCumplimientoPlan(paciente));
                    stats.put("tendenciaPeso", calcularTendenciaPeso(paciente));
                    estadisticas.add(stats);
                });
        });
        
        return estadisticas;
    }

    // Análisis y reportes
    public Map<String, Object> generarReporteRendimiento(Long nutricionistaId) {
        Map<String, Object> reporte = new HashMap<>();
        
        nutricionistaRepository.findById(nutricionistaId).ifPresent(nutricionista -> {
            reporte.put("pacientesActivos", nutricionista.getPacientesActivos());
            reporte.put("planesActivos", contarPlanesActivos(nutricionista));
            reporte.put("tasaExito", calcularTasaExito(nutricionista));
            reporte.put("calificacionPromedio", nutricionista.getCalificacionPromedio());
        });
        
        return reporte;
    }

    private double calcularCumplimientoPlan(Paciente paciente) {
        if (paciente.getPlanesDeDieta().isEmpty()) return 0.0;
        
        PlanDieta planActual = paciente.getPlanesDeDieta().stream()
            .filter(PlanDieta::isActivo)
            .findFirst()
            .orElse(null);
            
        if (planActual == null) return 0.0;
        
        return planActual.validarObjetivoCalorico() ? 100.0 : 
            (planActual.calcularCaloriasTotales() / planActual.getCaloriasObjetivo()) * 100;
    }

    private String calcularTendenciaPeso(Paciente paciente) {
        List<SeguimientoPeso> seguimientos = paciente.getSeguimientoPeso();
        if (seguimientos.size() < 2) return "ESTABLE";
        
        double ultimoPeso = seguimientos.get(seguimientos.size() - 1).getPeso();
        double penultimoPeso = seguimientos.get(seguimientos.size() - 2).getPeso();
        
        if (Math.abs(ultimoPeso - penultimoPeso) < 0.5) return "ESTABLE";
        return ultimoPeso > penultimoPeso ? "AUMENTANDO" : "DISMINUYENDO";
    }

    private int contarPlanesActivos(Nutricionista nutricionista) {
        return (int) nutricionista.getPlanesCreados().stream()
            .filter(PlanDieta::isActivo)
            .count();
    }

    private double calcularTasaExito(Nutricionista nutricionista) {
        if (nutricionista.getPacientes().isEmpty()) return 0.0;
        
        long pacientesExitosos = nutricionista.getPacientes().stream()
            .filter(p -> p.calcularProgreso() >= 80.0)
            .count();
            
        return (double) pacientesExitosos / nutricionista.getPacientes().size() * 100;
    }

    // Métricas y estadísticas
    public void registrarConsulta(Long nutricionistaId, double calificacion) {
        nutricionistaRepository.findById(nutricionistaId).ifPresent(nutricionista -> {
            nutricionista.registrarConsulta();
            nutricionista.actualizarCalificacion(calificacion);
            nutricionistaRepository.save(nutricionista);
        });
    }

    public int contarPacientesActivos(Long nutricionistaId) {
        return nutricionistaRepository.countPacientesActivos(nutricionistaId);
    }

    public List<Nutricionista> buscarPorEspecialidad(String especialidad) {
        return nutricionistaRepository.findByEspecialidadContaining(especialidad);
    }

    public List<Nutricionista> buscarDisponiblesOnline() {
        return nutricionistaRepository.findAllAvailableOnline();
    }

    // Validaciones y utilidades
    public boolean validarDisponibilidad(Long nutricionistaId, LocalDateTime fecha) {
        return nutricionistaRepository.findById(nutricionistaId)
                .map(nutricionista -> nutricionista.verificarDisponibilidad(fecha))
                .orElse(false);
    }

    public boolean tieneCapacidadNuevosPacientes(Long nutricionistaId) {
        return nutricionistaRepository.findById(nutricionistaId)
                .map(nutricionista -> nutricionista.getPacientesActivos() < 20)
                .orElse(false);
    }

    // Método para actualizar perfil completo del nutricionista
    public Nutricionista actualizarPerfil(Long id, Nutricionista nutricionista) {
        return nutricionistaRepository.findById(id)
            .map(n -> {
                // Información personal
                n.setNombre(nutricionista.getNombre());
                n.setApellido(nutricionista.getApellido());
                n.setTelefono(nutricionista.getTelefono());
                
                // Información profesional
                n.setEspecialidades(nutricionista.getEspecialidades());
                n.setDescripcionProfesional(nutricionista.getDescripcionProfesional());
                n.setNumeroLicencia(nutricionista.getNumeroLicencia());
                n.setUniversidad(nutricionista.getUniversidad());
                n.setAñosExperiencia(nutricionista.getAñosExperiencia());
                
                // Información de consulta
                n.setDireccionConsultorio(nutricionista.getDireccionConsultorio());
                n.setHorarioAtencion(nutricionista.getHorarioAtencion());
                n.setTarifaConsulta(nutricionista.getTarifaConsulta());
                n.setDisponibleConsultaOnline(nutricionista.isDisponibleConsultaOnline());
                n.setDuracionConsultaMinutos(nutricionista.getDuracionConsultaMinutos());
                
                return nutricionistaRepository.save(n);
            })
            .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
    }

    // Método para actualizar foto de perfil
    public void actualizarFotoPerfil(Long id, String nombreArchivo) {
        nutricionistaRepository.findById(id).ifPresent(nutricionista -> {
            nutricionista.setFotoPerfil(nombreArchivo);
            nutricionistaRepository.save(nutricionista);
        });
    }

    // Método para obtener estadísticas del perfil
    public Map<String, Object> obtenerEstadisticasPerfil(Long id) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        nutricionistaRepository.findById(id).ifPresent(nutricionista -> {
            // Validar valores nulos y proporcionar valores por defecto
            estadisticas.put("totalPacientes", 
                nutricionista.getPacientes() != null ? nutricionista.getPacientes().size() : 0);
            estadisticas.put("totalConsultas", 
                nutricionista.getConsultasRealizadas());
            estadisticas.put("calificacionPromedio", 
                nutricionista.getCalificacionPromedio());
            estadisticas.put("planesCreados", 
                nutricionista.getPlanesCreados() != null ? nutricionista.getPlanesCreados().size() : 0);
            estadisticas.put("tasaExito", calcularTasaExito(nutricionista));
            
            // Agregar información adicional
            estadisticas.put("pacientesActivos", nutricionista.getPacientesActivos());
            estadisticas.put("citasPendientes", 
                nutricionista.getCitas() != null ? 
                    nutricionista.getCitas().stream()
                        .filter(c -> "PENDIENTE".equals(c.getEstado()))
                        .count() : 0);
        });
        
        return estadisticas;
    }
/* 
    private double calcularTasaExito(Nutricionista nutricionista) {
        if (nutricionista.getPacientes() == null || nutricionista.getPacientes().isEmpty()) {
            return 0.0;
        }
        
        long pacientesExitosos = nutricionista.getPacientes().stream()
            .filter(p -> p != null && p.calcularProgreso() >= 80.0)
            .count();
            
        return (double) pacientesExitosos / nutricionista.getPacientes().size() * 100;
    }
*/
    // Método para validar disponibilidad de horarios
    public boolean validarDisponibilidadHorario(Long id, String horario) {
        return nutricionistaRepository.findById(id)
            .map(nutricionista -> {
                // Implementar lógica de validación de horarios
                return !horario.isEmpty() && horario.matches("^([0-9]{1,2}:[0-9]{2}-[0-9]{1,2}:[0-9]{2},?)+$");
            })
            .orElse(false);
    }
}
