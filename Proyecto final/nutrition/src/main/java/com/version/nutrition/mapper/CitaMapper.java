package com.version.nutrition.mapper;

import com.version.nutrition.dto.CitaDTO;
import com.version.nutrition.model.Cita;
import com.version.nutrition.model.Nutricionista;
import com.version.nutrition.model.Paciente;
import com.version.nutrition.repository.NutricionistaRepository;
import com.version.nutrition.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CitaMapper {
    
    @Autowired
    private NutricionistaRepository nutricionistaRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    public CitaDTO toDTO(Cita cita) {
        if (cita == null) return null;
        
        CitaDTO dto = new CitaDTO();
        dto.setId(cita.getId());
        dto.setFecha(cita.getFecha());
        dto.setTipoCita(cita.getTipoCita());
        dto.setMotivoConsulta(cita.getMotivoConsulta());
        dto.setEsVirtual(cita.isEsVirtual());
        dto.setEstado(cita.getEstado());
        dto.setNotas(cita.getNotas());
        dto.setDuracionMinutos(cita.getDuracionMinutos());
        dto.setCosto(cita.getCosto());
        dto.setEnlaceReunion(cita.getEnlaceReunion());
        
        // Mapear IDs de relaciones
        if (cita.getNutricionista() != null) {
            dto.setNutricionistaId(cita.getNutricionista().getId());
        }
        if (cita.getPaciente() != null) {
            dto.setPacienteId(cita.getPaciente().getId());
        }
        
        return dto;
    }

    public Cita toEntity(CitaDTO dto) {
        if (dto == null) return null;
        
        Cita cita = new Cita();
        cita.setId(dto.getId());
        cita.setFecha(dto.getFecha());
        cita.setTipoCita(dto.getTipoCita());
        cita.setMotivoConsulta(dto.getMotivoConsulta());
        cita.setEsVirtual(dto.isEsVirtual());
        cita.setEstado(dto.getEstado());
        cita.setNotas(dto.getNotas());
        cita.setDuracionMinutos(dto.getDuracionMinutos());
        cita.setCosto(dto.getCosto());
        cita.setEnlaceReunion(dto.getEnlaceReunion());
        
        // Cargar relaciones desde los repositorios
        if (dto.getNutricionistaId() != null) {
            Nutricionista nutricionista = nutricionistaRepository.findById(dto.getNutricionistaId())
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
            cita.setNutricionista(nutricionista);
        }
        
        if (dto.getPacienteId() != null) {
            Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            cita.setPaciente(paciente);
        }
        
        return cita;
    }

    public void updateEntityFromDTO(Cita cita, CitaDTO dto) {
        if (dto.getFecha() != null) cita.setFecha(dto.getFecha());
        if (dto.getTipoCita() != null) cita.setTipoCita(dto.getTipoCita());
        if (dto.getMotivoConsulta() != null) cita.setMotivoConsulta(dto.getMotivoConsulta());
        cita.setEsVirtual(dto.isEsVirtual());
        if (dto.getEstado() != null) cita.setEstado(dto.getEstado());
        if (dto.getNotas() != null) cita.setNotas(dto.getNotas());
        if (dto.getDuracionMinutos() != null) cita.setDuracionMinutos(dto.getDuracionMinutos());
        if (dto.getCosto() != null) cita.setCosto(dto.getCosto());
        if (dto.getEnlaceReunion() != null) cita.setEnlaceReunion(dto.getEnlaceReunion());
    }
}
