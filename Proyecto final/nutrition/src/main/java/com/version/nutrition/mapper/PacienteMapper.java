package com.version.nutrition.mapper;

import com.version.nutrition.model.Paciente;
import com.version.nutrition.dto.PacienteDTO;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class PacienteMapper {
    
    // Convertir de Entidad a DTO
    public PacienteDTO toDTO(Paciente paciente) {
        PacienteDTO dto = new PacienteDTO();
        dto.setId(paciente.getId());
        dto.setNombre(paciente.getNombre());
        dto.setApellido(paciente.getApellido());
        dto.setEdad(paciente.getEdad());
        dto.setGenero(paciente.getGenero());
        dto.setEmail(paciente.getEmail());
        dto.setPeso(paciente.getPeso());
        dto.setEstatura(paciente.getEstatura());
        dto.setTelefono(paciente.getTelefono());
        dto.setDireccion(paciente.getDireccion());
        dto.setEnfermedadesCronicas(paciente.getEnfermedadesCronicas());
        dto.setAlergias(paciente.getAlergias());
        dto.setGrupoSanguineo(paciente.getGrupoSanguineo());
        dto.setActividadFisica(paciente.getActividadFisica());
        dto.setObjetivoNutricional(paciente.getObjetivoNutricional());
        dto.setPesoObjetivo(paciente.getPesoObjetivo());
        dto.setFechaRegistro(paciente.getFechaRegistro());
        return dto;
    }

    // Convertir de DTO a Entidad
    public Paciente toEntity(PacienteDTO dto) {
        Paciente paciente = new Paciente();
        updateEntityFromDTO(paciente, dto);
        return paciente;
    }

    // Actualizar una entidad existente con datos del DTO
    public void updateEntityFromDTO(Paciente paciente, PacienteDTO dto) {
        paciente.setNombre(dto.getNombre());
        paciente.setApellido(dto.getApellido());
        paciente.setEdad(dto.getEdad());
        paciente.setGenero(dto.getGenero());
        paciente.setEmail(dto.getEmail());
        paciente.setPeso(dto.getPeso());
        paciente.setEstatura(dto.getEstatura());
        paciente.setTelefono(dto.getTelefono());
        paciente.setDireccion(dto.getDireccion());
        paciente.setEnfermedadesCronicas(dto.getEnfermedadesCronicas());
        paciente.setAlergias(dto.getAlergias());
        paciente.setGrupoSanguineo(dto.getGrupoSanguineo());
        paciente.setActividadFisica(dto.getActividadFisica());
        paciente.setObjetivoNutricional(dto.getObjetivoNutricional());
        paciente.setPesoObjetivo(dto.getPesoObjetivo());
        
        // Recalcular métricas después de actualizar datos
        paciente.calcularIMC();
        paciente.calcularTasaMetabolicaBasal();
    }

    // Método para convertir una lista de pacientes a DTOs
    public List<PacienteDTO> toDTOList(List<Paciente> pacientes) {
        return pacientes.stream()
                       .map(this::toDTO)
                       .collect(Collectors.toList());
    }
}
