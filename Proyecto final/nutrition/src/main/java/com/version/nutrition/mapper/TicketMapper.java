package com.version.nutrition.mapper;

import com.version.nutrition.dto.TicketDTO;
import com.version.nutrition.model.Ticket;
import com.version.nutrition.model.Paciente;
import com.version.nutrition.model.Nutricionista;
import com.version.nutrition.model.Cita;
import com.version.nutrition.repository.PacienteRepository;
import com.version.nutrition.repository.NutricionistaRepository;
import com.version.nutrition.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private NutricionistaRepository nutricionistaRepository;
    
    @Autowired
    private CitaRepository citaRepository;
    
    public TicketDTO toDTO(Ticket ticket) {
        if (ticket == null) return null;
        
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setTitulo(ticket.getTitulo());
        dto.setDescripcion(ticket.getDescripcion());
        dto.setEstado(ticket.getEstado());
        dto.setPrioridad(ticket.getPrioridad());
        dto.setCategoria(ticket.getCategoria());
        dto.setFechaCreacion(ticket.getFechaCreacion());
        dto.setNumeroReferencia(ticket.getNumeroReferencia());
        dto.setEtiquetas(ticket.getEtiquetas());
        dto.setRespuestaAutomatica(ticket.getRespuestaAutomatica());
        
        if (ticket.getPaciente() != null) {
            dto.setPacienteId(ticket.getPaciente().getId());
        }
        if (ticket.getNutricionista() != null) {
            dto.setNutricionistaId(ticket.getNutricionista().getId());
        }
        if (ticket.getCitaRelacionada() != null) {
            dto.setCitaRelacionadaId(ticket.getCitaRelacionada().getId());
        }
        
        return dto;
    }

    public Ticket toEntity(TicketDTO dto) {
        if (dto == null) return null;
        
        Ticket ticket = new Ticket();
        updateEntityFromDTO(ticket, dto);
        return ticket;
    }
    
    public void updateEntityFromDTO(Ticket ticket, TicketDTO dto) {
        ticket.setTitulo(dto.getTitulo());
        ticket.setDescripcion(dto.getDescripcion());
        if (dto.getEstado() != null) ticket.setEstado(dto.getEstado());
        if (dto.getPrioridad() != null) ticket.setPrioridad(dto.getPrioridad());
        ticket.setCategoria(dto.getCategoria());
        ticket.setEtiquetas(dto.getEtiquetas());
        ticket.setRespuestaAutomatica(dto.getRespuestaAutomatica());
        
        // Establecer relaciones
        if (dto.getPacienteId() != null) {
            Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            ticket.setPaciente(paciente);
        }
        
        if (dto.getNutricionistaId() != null) {
            Nutricionista nutricionista = nutricionistaRepository.findById(dto.getNutricionistaId())
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
            ticket.setNutricionista(nutricionista);
        }
        
        if (dto.getCitaRelacionadaId() != null) {
            Cita cita = citaRepository.findById(dto.getCitaRelacionadaId())
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
            ticket.setCitaRelacionada(cita);
        }
    }
}
