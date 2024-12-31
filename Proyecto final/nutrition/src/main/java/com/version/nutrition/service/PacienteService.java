package com.version.nutrition.service;

import com.version.nutrition.model.*;
import com.version.nutrition.repository.PacienteRepository;
import com.version.nutrition.repository.NutricionistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@Transactional
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final NutricionistaRepository nutricionistaRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PacienteService(
            PacienteRepository pacienteRepository,
            NutricionistaRepository nutricionistaRepository,
            PasswordEncoder passwordEncoder) {
        this.pacienteRepository = pacienteRepository;
        this.nutricionistaRepository = nutricionistaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Operaciones CRUD básicas
    public Paciente registrarPaciente(Paciente paciente) {
        // Configurar valores predeterminados
        paciente.setPassword(passwordEncoder.encode(paciente.getPassword()));
        paciente.setRol("ROLE_PACIENTE");
        paciente.setActivo(true);
        
        // Establecer foto de perfil predeterminada basada en el género
        String fotoPerfil = switch (paciente.getGenero()) {
            case "MASCULINO" -> "paciente-male-default.png";
            case "FEMENINO" -> "paciente-female-default.png";
            default -> "paciente-default.png";
        };
        paciente.setFotoPerfil(fotoPerfil);

        // Guardar el paciente
        Paciente pacienteGuardado = pacienteRepository.save(paciente);

        // Vincular con el nutricionista admin
        nutricionistaRepository.vincularPacienteConAdmin(pacienteGuardado.getId());

        return pacienteGuardado;
    }

    public Optional<Paciente> buscarPorId(Long id) {
        return pacienteRepository.findById(id);
    }

    public Optional<Paciente> buscarPorEmail(String email) {
        return pacienteRepository.findByEmail(email);
    }

    public void actualizarPaciente(Long id, Paciente pacienteActualizado) {
        pacienteRepository.findById(id).ifPresent(paciente -> {
            // Actualizar todos los campos editables
            paciente.setNombre(pacienteActualizado.getNombre());
            paciente.setApellido(pacienteActualizado.getApellido());
            paciente.setEdad(pacienteActualizado.getEdad());
            paciente.setGenero(pacienteActualizado.getGenero());
            paciente.setTelefono(pacienteActualizado.getTelefono());
            paciente.setDireccion(pacienteActualizado.getDireccion());
            
            // Información médica
            paciente.setPeso(pacienteActualizado.getPeso());
            paciente.setEstatura(pacienteActualizado.getEstatura());
            paciente.setGrupoSanguineo(pacienteActualizado.getGrupoSanguineo());
            paciente.setActividadFisica(pacienteActualizado.getActividadFisica());
            
            // Objetivos y preferencias
            paciente.setObjetivoNutricional(pacienteActualizado.getObjetivoNutricional());
            paciente.setPesoObjetivo(pacienteActualizado.getPesoObjetivo());
            paciente.setPreferenciasAlimenticias(pacienteActualizado.getPreferenciasAlimenticias());
            paciente.setAlergias(pacienteActualizado.getAlergias());
            paciente.setEnfermedadesCronicas(pacienteActualizado.getEnfermedadesCronicas());
            
            // Recalcular métricas
            paciente.calcularIMC();
            paciente.calcularTasaMetabolicaBasal();
            
            pacienteRepository.save(paciente);
        });
    }

    public void agregarHistorialMedico(Long pacienteId, String historialMedico) {
        pacienteRepository.findById(pacienteId).ifPresent(paciente -> {
            paciente.setHistorialMedico(historialMedico);
            pacienteRepository.save(paciente);
        });
    }

    // Gestión de mediciones y seguimiento
    public void registrarMedicion(Long pacienteId, double peso, double cintura, 
                                double cadera, double grasaCorporal) {
        pacienteRepository.findById(pacienteId).ifPresent(paciente -> {
            paciente.agregarMedicion(peso, cintura, cadera, grasaCorporal);
            pacienteRepository.save(paciente);
        });
    }

    public void actualizarSeguimiento(Long pacienteId, SeguimientoPeso seguimiento) {
        pacienteRepository.findById(pacienteId).ifPresent(paciente -> {
            seguimiento.calcularIMC(paciente.getEstatura());
            paciente.getSeguimientoPeso().add(seguimiento);
            pacienteRepository.save(paciente);
        });
    }

    // Gestión de planes de dieta
    public void asignarPlanDieta(Long pacienteId, PlanDieta plan) {
        pacienteRepository.findById(pacienteId).ifPresent(paciente -> {
            paciente.getPlanesDeDieta().add(plan);
            pacienteRepository.save(paciente);
        });
    }

    public List<PlanDieta> obtenerPlanesActivos(Long pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .map(Paciente::getPlanesDeDieta)
                .orElse(List.of());
    }

    // Gestión de citas
    public void agendarCita(Long pacienteId, Cita cita) {
        pacienteRepository.findById(pacienteId).ifPresent(paciente -> {
            paciente.getCitas().add(cita);
            pacienteRepository.save(paciente);
        });
    }

    public List<Cita> obtenerCitasFuturas(Long pacienteId) {
        LocalDateTime ahora = LocalDateTime.now();
        return pacienteRepository.findById(pacienteId)
                .map(paciente -> paciente.getCitas().stream()
                        .filter(cita -> cita.getFecha().isAfter(ahora))
                        .toList())
                .orElse(List.of());
    }
    public void actualizarUltimaConsulta(Long pacienteId) {
        // Implementation for updating the last consultation date of the patient
    }

    // Consultas especializadas
    public List<Paciente> buscarPorRangoIMC(double imcMin, double imcMax) {
        return pacienteRepository.findByRangoIMC(imcMin, imcMax);
    }

    public List<Paciente> buscarPorAlergia(String alergia) {
        return pacienteRepository.findByAlergia(alergia);
    }

    public List<Paciente> buscarPacientesSinSeguimiento() {
        LocalDate fechaLimite = LocalDate.now().minusDays(30);
        return pacienteRepository.findPacientesSinSeguimiento(fechaLimite);
    }

    // Métodos de análisis y estadísticas
    public double calcularProgresoObjetivo(Long pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .map(Paciente::calcularProgreso)
                .orElse(0.0);
    }

    public boolean necesitaActualizarMediciones(Long pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .map(Paciente::necesitaActualizarMediciones)
                .orElse(true);
    }

    public List<Map<String, Object>> obtenerEstadisticasNutricionales() {
        return pacienteRepository.obtenerEstadisticasNutricionales();
    }

    // Agregar métodos para soportar operaciones REST
    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    // Agregar método para búsqueda por nombre o apellido
    public List<Paciente> buscarPorNombreOApellido(String nombre, String apellido) {
        if ((nombre == null || nombre.isEmpty()) && (apellido == null || apellido.isEmpty())) {
            return pacienteRepository.findAll();
        }
        return pacienteRepository.findByNombreContainingOrApellidoContaining(
            nombre != null ? nombre : "", 
            apellido != null ? apellido : ""
        );
    }

    // Modificar el método actualizarPaciente para la API REST
    public Paciente actualizarPacienteAPI(Long id, Paciente paciente) {
        return pacienteRepository.findById(id)
                .map(p -> {
                    // Actualizar campos básicos
                    p.setNombre(paciente.getNombre());
                    p.setApellido(paciente.getApellido());
                    p.setEdad(paciente.getEdad());
                    p.setGenero(paciente.getGenero());
                    p.setEmail(paciente.getEmail());
                    p.setTelefono(paciente.getTelefono());
                    p.setDireccion(paciente.getDireccion());

                    // Actualizar métricas
                    p.setPeso(paciente.getPeso());
                    p.setEstatura(paciente.getEstatura());
                    p.setPesoObjetivo(paciente.getPesoObjetivo());
                    p.setActividadFisica(paciente.getActividadFisica());

                    // Actualizar información médica
                    p.setEnfermedadesCronicas(paciente.getEnfermedadesCronicas());
                    p.setAlergias(paciente.getAlergias());
                    p.setGrupoSanguineo(paciente.getGrupoSanguineo());
                    p.setObjetivoNutricional(paciente.getObjetivoNutricional());

                    // Recalcular métricas
                    p.calcularIMC();
                    p.calcularTasaMetabolicaBasal();

                    // Guardar cambios
                    return pacienteRepository.save(p);
                })
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));
    }

    // Método para eliminar paciente de forma segura
    public void eliminarPaciente(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));
            
        // Verificar si tiene citas o planes activos antes de eliminar
        if (!paciente.getCitas().isEmpty()) {
            throw new RuntimeException("No se puede eliminar el paciente porque tiene citas programadas");
        }
        
//        if (paciente.getPlanesDeDieta().stream().anyMatch(p -> "ACTIVO".equals(p.getEstado()))) {
//            throw new RuntimeException("No se puede eliminar el paciente porque tiene planes de dieta activos");
//        }
        
        pacienteRepository.deleteById(id);
    }

    // Gestión de planes nutricionales
    public void asignarPlanNutricional(Long pacienteId, PlanDieta plan) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
        // Verificar plan actual
        paciente.getPlanesDeDieta().stream()
            .filter(p -> p.isActivo())
            .forEach(p -> p.setActivo(false));
            
        plan.setPaciente(paciente);
        plan.setActivo(true);
        plan.setFechaInicio(LocalDate.now());
        paciente.getPlanesDeDieta().add(plan);
        
        pacienteRepository.save(paciente);
    }

    public List<PlanDieta> obtenerHistorialPlanes(Long pacienteId) {
        return pacienteRepository.findById(pacienteId)
            .map(Paciente::getPlanesDeDieta)
            .orElse(new ArrayList<>());
    }

    // Seguimiento y monitoreo
    public void registrarSeguimientoDiario(Long pacienteId, SeguimientoPeso seguimiento) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
        // Calcular métricas
        seguimiento.calcularIMC(paciente.getEstatura());
        seguimiento.setFechaRegistro(LocalDateTime.now());
        
        // Actualizar estado del paciente
        paciente.setPeso((float) seguimiento.getPeso());
        paciente.calcularIMC();
        paciente.calcularTasaMetabolicaBasal();
        
        // Agregar seguimiento
        paciente.getSeguimientoPeso().add(seguimiento);
        
        pacienteRepository.save(paciente);
    }

    public Map<String, Object> obtenerMetricasProgreso(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
        Map<String, Object> metricas = new HashMap<>();
        metricas.put("progresoObjetivo", paciente.calcularProgreso());
        metricas.put("imc", paciente.getIndiceMasaCorporal());
        metricas.put("estadoNutricional", paciente.getEstadoNutricional());
        metricas.put("diasSeguimiento", paciente.getDiasSeguimiento());
        
        return metricas;
    }

    public List<SeguimientoPeso> obtenerHistorialSeguimiento(Long pacienteId) {
        return pacienteRepository.findById(pacienteId)
            .map(paciente -> {
                List<SeguimientoPeso> seguimientos = paciente.getSeguimientoPeso();
                seguimientos.sort((s1, s2) -> s2.getFechaRegistro().compareTo(s1.getFechaRegistro()));
                return seguimientos;
            })
            .orElse(new ArrayList<>());
    }

    // Gestión de objetivos y metas
    public void actualizarObjetivos(Long pacienteId, double pesoObjetivo, LocalDate fechaObjetivo) {
        pacienteRepository.findById(pacienteId).ifPresent(paciente -> {
            paciente.setPesoObjetivo(pesoObjetivo);
            paciente.setFechaObjetivo(fechaObjetivo);
            paciente.calcularIMC();
            pacienteRepository.save(paciente);
        });
    }

    public boolean verificarCumplimientoPlan(Long pacienteId) {
        return pacienteRepository.findById(pacienteId)
            .map(paciente -> {
                if (paciente.getPlanesDeDieta().isEmpty()) return false;
                
                PlanDieta planActual = paciente.getPlanesDeDieta().stream()
                    .filter(PlanDieta::isActivo)
                    .findFirst()
                    .orElse(null);
                
                if (planActual == null) return false;
                
                return planActual.validarObjetivoCalorico();
            })
            .orElse(false);
    }

    // Alertas y notificaciones
    public List<String> generarAlertas(Long pacienteId) {
        List<String> alertas = new ArrayList<>();
        pacienteRepository.findById(pacienteId).ifPresent(paciente -> {
            if (paciente.necesitaActualizarMediciones()) {
                alertas.add("Se requiere actualización de mediciones");
            }
            
            if (paciente.getIndiceMasaCorporal() > 30) {
                alertas.add("IMC en rango de obesidad - Requiere atención");
            }
            
            if (paciente.calcularProgreso() < 10) {
                alertas.add("Bajo progreso hacia el objetivo");
            }
        });
        return alertas;
    }
    
}
