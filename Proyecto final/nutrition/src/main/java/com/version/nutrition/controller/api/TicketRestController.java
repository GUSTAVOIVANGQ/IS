package com.version.nutrition.controller.api;

import com.version.nutrition.dto.TicketDTO;
import com.version.nutrition.dto.ApiResponse;
import com.version.nutrition.model.Ticket;
import com.version.nutrition.service.TicketService;
import com.version.nutrition.mapper.TicketMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
public class TicketRestController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketMapper ticketMapper;

    // Crear nuevo ticket
    @PostMapping
    public ResponseEntity<ApiResponse<TicketDTO>> crearTicket(@Valid @RequestBody TicketDTO ticketDTO) {
        try {
            Ticket ticket = ticketMapper.toEntity(ticketDTO);
            Ticket ticketCreado = ticketService.crearTicket(ticketDTO.getPacienteId(), ticket);
            return ResponseEntity.ok(new ApiResponse<>(
                true, "Ticket creado exitosamente", ticketMapper.toDTO(ticketCreado)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "Error al crear el ticket: " + e.getMessage(), null));
        }
    }

    // Obtener todos los tickets de un paciente
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<ApiResponse<List<TicketDTO>>> listarTicketsPaciente(@PathVariable Long pacienteId) {
        try {
            List<Ticket> tickets = ticketService.listarTicketsPaciente(pacienteId);
            List<TicketDTO> ticketsDTO = tickets.stream()
                .map(ticketMapper::toDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(
                true, "Tickets recuperados exitosamente", ticketsDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al recuperar tickets: " + e.getMessage(), null));
        }
    }

    // Obtener ticket por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDTO>> obtenerTicket(@PathVariable Long id) {
        try {
            return ticketService.buscarPorId(id)
                .map(ticket -> ResponseEntity.ok(new ApiResponse<>(
                    true, "Ticket encontrado", ticketMapper.toDTO(ticket))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Ticket no encontrado", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al recuperar el ticket", null));
        }
    }

    // Obtener todos los tickets
    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketDTO>>> listarTodos() {
        try {
            List<Ticket> tickets = ticketService.buscarTodos();
            List<TicketDTO> ticketsDTO = tickets.stream()
                .map(ticketMapper::toDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(
                true, "Tickets recuperados exitosamente", ticketsDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al recuperar tickets: " + e.getMessage(), null));
        }
    }

    // Actualizar ticket
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDTO>> actualizarTicket(
            @PathVariable Long id, 
            @Valid @RequestBody TicketDTO ticketDTO) {
        try {
            Ticket ticketExistente = ticketService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

            ticketMapper.updateEntityFromDTO(ticketExistente, ticketDTO);
            Ticket ticketActualizado = ticketService.actualizarTicket(id, ticketExistente);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true, "Ticket actualizado exitosamente", ticketMapper.toDTO(ticketActualizado)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al actualizar el ticket", null));
        }
    }

    // Actualizar estado del ticket
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<TicketDTO>> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {
        try {
            Ticket ticketActualizado = ticketService.actualizarEstado(id, nuevoEstado);
            return ResponseEntity.ok(new ApiResponse<>(
                true, "Estado actualizado exitosamente", ticketMapper.toDTO(ticketActualizado)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al actualizar el estado: " + e.getMessage(), null));
        }
    }

    // Eliminar ticket
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarTicket(@PathVariable Long id) {
        try {
            ticketService.eliminarTicket(id);
            return ResponseEntity.ok(new ApiResponse<>(
                true, "Ticket eliminado exitosamente", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Ticket no encontrado", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al eliminar el ticket", null));
        }
    }

    // Agregar etiquetas a un ticket
    @PostMapping("/{id}/etiquetas")
    public ResponseEntity<ApiResponse<TicketDTO>> agregarEtiquetas(
            @PathVariable Long id,
            @RequestBody List<String> etiquetas) {
        try {
            Ticket ticketActualizado = ticketService.agregarEtiquetas(id, etiquetas);
            return ResponseEntity.ok(new ApiResponse<>(
                true, "Etiquetas agregadas exitosamente", ticketMapper.toDTO(ticketActualizado)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al agregar etiquetas: " + e.getMessage(), null));
        }
    }
}
