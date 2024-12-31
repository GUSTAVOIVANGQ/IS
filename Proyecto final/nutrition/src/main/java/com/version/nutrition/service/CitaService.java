package com.version.nutrition.service;

import com.version.nutrition.dto.CitaDTO;
import com.version.nutrition.exception.CitaException;
import com.version.nutrition.mapper.CitaMapper;
import com.version.nutrition.model.Cita;
import com.version.nutrition.model.Nutricionista;
import com.version.nutrition.model.Paciente;
import com.version.nutrition.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class CitaService {

    private final CitaRepository citaRepository;
    private static final Logger logger = LoggerFactory.getLogger(CitaService.class);

    @Autowired
    private NutricionistaService nutricionistaService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private CitaMapper citaMapper;

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    // Métodos principales CRUD
    // Modificar método existente para usar el nuevo query de creación con relaciones
    @Transactional
    public Cita agendarCita(Long nutricionistaId, Cita cita) {
        try {
            // Validaciones básicas
            if (cita.getPaciente() == null || cita.getPaciente().getId() == null ||
                cita.getFecha() == null) {
                throw new IllegalArgumentException("Datos de cita incompletos");
            }

            // Verificar disponibilidad
            if (!verificarDisponibilidad(nutricionistaId, cita.getFecha())) {
                throw new IllegalStateException("El horario seleccionado no está disponible");
            }

            // Crear Map con los datos básicos de la cita
            Map<String, Object> citaData = new HashMap<>();
            citaData.put("fecha", cita.getFecha());
            citaData.put("tipoCita", cita.getTipoCita());
            citaData.put("motivoConsulta", cita.getMotivoConsulta());
            citaData.put("esVirtual", cita.isEsVirtual());
            citaData.put("estado", "PENDIENTE");
            citaData.put("fechaCreacion", LocalDateTime.now());
            citaData.put("duracionMinutos", 30);

            // Crear la cita usando la consulta personalizada
            return citaRepository.crearCitaConRelaciones(
                nutricionistaId,
                cita.getPaciente().getId(),
                citaData
            );
        } catch (Exception e) {
            logger.error("Error al agendar cita: {}", e.getMessage());
            throw new RuntimeException("Error al agendar la cita: " + e.getMessage());
        }
    }

    public Optional<Cita> buscarPorId(Long id) {
        return citaRepository.findById(id);
    }

    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    // Métodos de gestión de citas
    public List<Cita> buscarCitasPorNutricionista(Long nutricionistaId) {
        return citaRepository.findByNutricionista(nutricionistaId);
    }

    // Agregar método para buscar citas por nutricionista con relaciones completas
    public List<Cita> buscarCitasNutrionistaConRelaciones(Long nutricionistaId) {
        return citaRepository.findByNutricionista(nutricionistaId);
    }

    public List<Cita> buscarCitasPorPaciente(Long pacienteId) {
        List<Cita> citas = citaRepository.findByPaciente(pacienteId);
        // Asegurarse de que el nutricionista esté cargado
        citas.forEach(cita -> {
            if (cita.getNutricionista() != null) {
                cita.getNutricionista().getNombre(); // Forzar la carga
            }
        });
        return citas;
    }

    public List<Cita> buscarCitasFuturas(Long nutricionistaId) {
        return citaRepository.findFuturasByNutricionista(nutricionistaId, LocalDateTime.now());
    }

    public boolean verificarDisponibilidad(Long nutricionistaId, LocalDateTime fecha) {
        return !citaRepository.existsCitaInHorario(nutricionistaId, fecha);
    }

    // Métodos de actualización de estado
    public void confirmarCita(Long citaId) {
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.confirmar();
        citaRepository.save(cita);
    }

    // Agregar método para cancelar cita manteniendo historial
    public Cita cancelarCitaConHistorial(Long citaId) {
        return citaRepository.cancelarCita(citaId, LocalDateTime.now());
    }

    public void cancelarCita(Long citaId) {
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.cancelar();
        citaRepository.save(cita);
    }

    public void completarCita(Long citaId) {
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.completar();
        citaRepository.save(cita);
    }

    // Métodos de búsqueda específica
    public List<Cita> buscarCitasPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return citaRepository.findByRangoFechas(fechaInicio, fechaFin);
    }

    public List<Cita> buscarCitasPorEstado(String estado) {
        return citaRepository.findByEstado(estado);
    }

    public List<Cita> buscarCitasVirtuales() {
        return citaRepository.findCitasVirtuales();
    }

    // Métodos de estadísticas
    public int contarCitasPendientes(Long nutricionistaId) {
        return citaRepository.countCitasPendientes(nutricionistaId);
    }

    public int contarCitasCompletadas(Long nutricionistaId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return citaRepository.countCitasCompletadas(nutricionistaId, fechaInicio, fechaFin);
    }

    // Métodos de validación y utilidad
    public boolean validarHorarioCita(LocalDateTime fecha) {
        int hora = fecha.getHour();
        return hora >= 8 && hora <= 18; // Horario de atención de 8:00 a 18:00
    }

    // Modificar método existente de reprogramar
    public Cita reprogramarCita(Long citaId, LocalDateTime nuevaFecha) {
        Cita cita = citaRepository.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        // Validaciones
        if (!validarHorarioCita(nuevaFecha)) {
            throw new IllegalArgumentException("Hora fuera del horario de atención");
        }

        if (!verificarDisponibilidad(cita.getNutricionista().getId(), nuevaFecha)) {
            throw new IllegalStateException("El nuevo horario no está disponible");
        }

        // Actualizar fecha y estado
        cita.setFecha(nuevaFecha);
        cita.setEstado("REPROGRAMADA");
        cita.setFechaModificacion(LocalDateTime.now());
        return citaRepository.save(cita);
    }

    public void actualizarNotas(Long citaId, String notas) {
        citaRepository.findById(citaId).ifPresent(cita -> {
            cita.setNotas(notas);
            citaRepository.save(cita);
        });
    }

    public Cita buscarProximaCitaPaciente(Long pacienteId, LocalDateTime fechaActual) {
        try {
            return citaRepository.findByPacienteAndFechaAfter(pacienteId, fechaActual)
                .orElse(null);
        } catch (Exception e) {
            logger.error("Error al buscar próxima cita para paciente {}: {}", pacienteId, e.getMessage());
            return null;
        }
    }

    // Agregar método para verificar si existe relación previa
    public boolean verificarRelacionNutricionistaPaciente(Long nutricionistaId, Long pacienteId) {
        return citaRepository.existeRelacionNutricionistaPaciente(nutricionistaId, pacienteId);
    }

    // Implementación de los nuevos métodos para la API REST
    public Cita crearCitaRest(CitaDTO citaDTO) {
        try {
            // Validar datos básicos
            if (!validarHorarioCita(citaDTO.getFecha())) {
                throw new CitaException("Horario fuera del rango permitido (8:00-18:00)");
            }

            // Verificar disponibilidad
            if (!verificarDisponibilidad(citaDTO.getNutricionistaId(), citaDTO.getFecha())) {
                throw new CitaException("El horario seleccionado no está disponible");
            }

            // Convertir DTO a entidad
            Cita cita = citaMapper.toEntity(citaDTO);
            
            // Establecer valores por defecto
            cita.setEstado("PENDIENTE");
            cita.setFechaCreacion(LocalDateTime.now());
            
            // Guardar la cita
            return citaRepository.save(cita);
            
        } catch (CitaException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear cita: {}", e.getMessage());
            throw new CitaException("Error al crear la cita: " + e.getMessage());
        }
    }

    public List<Cita> listarTodasLasCitas() {
        try {
            return citaRepository.findAll();
        } catch (Exception e) {
            logger.error("Error al listar citas: {}", e.getMessage());
            throw new CitaException("Error al obtener las citas");
        }
    }

    public Optional<Cita> obtenerCitaPorId(Long id) {
        try {
            return citaRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error al buscar cita {}: {}", id, e.getMessage());
            throw new CitaException("Error al obtener la cita");
        }
    }
}
