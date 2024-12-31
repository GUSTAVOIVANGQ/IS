package com.version.nutrition.service;

import com.version.nutrition.model.*;
import com.version.nutrition.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class SeguimientoService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PacienteService pacienteService;

    // Registrar nuevo seguimiento
    public SeguimientoPeso registrarSeguimiento(Long pacienteId, SeguimientoPeso seguimiento) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        // Calcular IMC y otras métricas
        seguimiento.calcularIMC(paciente.getEstatura());
        seguimiento.setFechaRegistro(LocalDateTime.now());
        seguimiento.setPaciente(paciente);

        // Actualizar métricas del paciente
        paciente.setPeso((float) seguimiento.getPeso());
        paciente.calcularIMC();
        paciente.calcularTasaMetabolicaBasal();

        // Agregar seguimiento a la lista del paciente
        paciente.getSeguimientoPeso().add(seguimiento);
        pacienteRepository.save(paciente);

        return seguimiento;
    }

    // Obtener historial de seguimiento
    public List<SeguimientoPeso> obtenerHistorial(Long pacienteId) {
        List<SeguimientoPeso> historial = pacienteService.obtenerHistorialSeguimiento(pacienteId);
        // Ordenar por fecha de registro, del más antiguo al más reciente
        historial.sort((a, b) -> a.getFechaRegistro().compareTo(b.getFechaRegistro()));
        return historial;
    }

    // Obtener métricas de progreso
    public Map<String, Object> obtenerMetricasProgreso(Long pacienteId) {
        return pacienteService.obtenerMetricasProgreso(pacienteId);
    }

    // Registrar actividad física
    public void registrarActividadFisica(Long pacienteId, String tipoActividad, int duracionMinutos, 
                                       int intensidad, LocalDateTime fecha) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        // Aquí puedes agregar la lógica para registrar la actividad física
        // Por ejemplo, crear una nueva entidad ActividadFisica y asociarla al paciente
        paciente.setHorasDeActividad(paciente.getHorasDeActividad() + (duracionMinutos / 60));
        pacienteRepository.save(paciente);
    }

    // Calcular tendencias
    public Map<String, Object> calcularTendencias(Long pacienteId) {
        List<SeguimientoPeso> historial = obtenerHistorial(pacienteId);
        Map<String, Object> tendencias = new HashMap<>();

        if (historial.size() >= 2) {
            // Calcular tendencia de peso
            double primerPeso = historial.get(historial.size() - 1).getPeso();
            double ultimoPeso = historial.get(0).getPeso();
            double cambioPeso = ultimoPeso - primerPeso;

            // Calcular tendencia de IMC
            double primerIMC = historial.get(historial.size() - 1).getImc();
            double ultimoIMC = historial.get(0).getImc();
            double cambioIMC = ultimoIMC - primerIMC;

            tendencias.put("tendenciaPeso", cambioPeso);
            tendencias.put("tendenciaIMC", cambioIMC);
            tendencias.put("diasSeguimiento", calcularDiasSeguimiento(historial));
        } else {
            // Valores por defecto si no hay suficientes mediciones
            tendencias.put("tendenciaPeso", 0.0);
            tendencias.put("tendenciaIMC", 0.0);
            tendencias.put("diasSeguimiento", 0L);
        }

        return tendencias;
    }

    // Métodos de utilidad
    private long calcularDiasSeguimiento(List<SeguimientoPeso> historial) {
        if (historial.isEmpty()) return 0;

        LocalDateTime primerRegistro = historial.get(0).getFechaRegistro();
        LocalDateTime ultimoRegistro = historial.get(historial.size() - 1).getFechaRegistro();

        return java.time.Duration.between(primerRegistro, ultimoRegistro).toDays();
    }

    // Verificar cumplimiento de objetivos
    public boolean verificarCumplimientoObjetivos(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        return paciente.calcularProgreso() >= 80.0;
    }

    // Generar reporte de progreso
    public Map<String, Object> generarReporteProgreso(Long pacienteId) {
        Map<String, Object> reporte = new HashMap<>();
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        reporte.put("progresoGeneral", paciente.calcularProgreso());
        reporte.put("tendencias", calcularTendencias(pacienteId));
        reporte.put("cumplimientoObjetivos", verificarCumplimientoObjetivos(pacienteId));
        reporte.put("estadoNutricional", paciente.getEstadoNutricional());
        reporte.put("imc", paciente.getIndiceMasaCorporal());

        return reporte;
    }

    // Obtener alertas y recomendaciones
    public List<String> obtenerAlertas(Long pacienteId) {
        return pacienteService.generarAlertas(pacienteId);
    }
}
