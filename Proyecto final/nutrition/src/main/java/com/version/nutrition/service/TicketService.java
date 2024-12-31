package com.version.nutrition.service;

import com.version.nutrition.model.Ticket;
import com.version.nutrition.model.Paciente;
import com.version.nutrition.model.Cita;
import com.version.nutrition.model.Nutricionista;
import com.version.nutrition.repository.TicketRepository;
import com.version.nutrition.repository.PacienteRepository;
import com.version.nutrition.repository.NutricionistaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@Transactional
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired 
    private NutricionistaRepository nutricionistaRepository;

    @Autowired
    private RespuestaTicketService respuestaTicketService;

    @Autowired
    private CitaService citaService;

    // Crear nuevo ticket
    public Ticket crearTicket(Long pacienteId, Ticket ticket) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        ticket.setPaciente(paciente);
        ticket.setFechaCreacion(LocalDateTime.now());
        ticket.setEstado("ABIERTO");
        ticket.setNumeroReferencia(generarNumeroReferencia());

        // Generar respuesta automática
        Ticket ticketGuardado = ticketRepository.save(ticket);
        respuestaTicketService.generarRespuestaAutomatica(ticketGuardado);

        return ticketGuardado;
    }

    // Generar ticket desde una cita
    public Ticket generarTicketDesdeCita(Long citaId, Long pacienteId) {
        Cita cita = citaService.buscarPorId(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
            
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Ticket ticket = new Ticket();
        ticket.generarDesdeCita(cita);
        ticket.setPaciente(paciente);
        ticket.setFechaCreacion(LocalDateTime.now());
        ticket.setEstado("ABIERTO");
        ticket.setNumeroReferencia(generarNumeroReferencia());

        // Generar respuesta automática basada en el tipo de cita
        Ticket ticketGuardado = ticketRepository.save(ticket);
        generarRespuestaAutomaticaCita(ticketGuardado);

        return ticketGuardado;
    }

    // Generar respuesta automática específica para citas
    private void generarRespuestaAutomaticaCita(Ticket ticket) {
        String respuestaAutomatica = switch (ticket.getCitaRelacionada().getTipoCita()) {
            case "PRIMERA_VEZ" -> "Bienvenido a su primera consulta. Por favor, asegúrese de tener a mano sus análisis clínicos y un registro de su alimentación de los últimos 3 días.";
            case "SEGUIMIENTO" -> "En esta consulta de seguimiento revisaremos su progreso y ajustaremos su plan según sea necesario.";
            case "CONTROL" -> "En esta cita de control evaluaremos sus avances y realizaremos las mediciones correspondientes.";
            case "EMERGENCIA" -> "Su consulta de emergencia ha sido registrada. Le daremos prioridad de atención.";
            default -> "Su cita ha sido registrada. Nos pondremos en contacto si necesitamos información adicional.";
        };

        respuestaTicketService.generarRespuestaAutomatica(ticket);
        ticket.setRespuestaAutomatica(respuestaAutomatica);
    }

    // Buscar ticket por ID
    public Optional<Ticket> buscarPorId(Long id) {
        return ticketRepository.findById(id);
    }

    // Listar tickets por paciente
    public List<Ticket> listarTicketsPaciente(Long pacienteId) {
        return ticketRepository.findByPacienteId(pacienteId);
    }

    // Listar tickets por nutricionista
    public List<Ticket> listarTicketsNutricionista(Long nutricionistaId) {
        return ticketRepository.findByNutricionistaId(nutricionistaId);
    }

    // Asignar nutricionista a ticket
    public Ticket asignarNutricionista(Long ticketId, Long nutricionistaId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        Nutricionista nutricionista = nutricionistaRepository.findById(nutricionistaId)
            .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));

        ticket.asignarNutricionista(nutricionista);
        return ticketRepository.save(ticket);
    }

    // Actualizar estado del ticket
    public Ticket actualizarEstado(Long ticketId, String nuevoEstado) {
        return ticketRepository.findById(ticketId)
            .map(ticket -> {
                ticket.cambiarEstado(nuevoEstado);
                return ticketRepository.save(ticket);
            })
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
    }

    // Actualizar un ticket existente
    @Transactional
    public Ticket actualizarTicket(Long id, Ticket ticket) {
        Ticket ticketExistente = ticketRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        // Actualizar solo los campos permitidos
        ticketExistente.setTitulo(ticket.getTitulo());
        ticketExistente.setDescripcion(ticket.getDescripcion());
        ticketExistente.setPrioridad(ticket.getPrioridad());
        ticketExistente.setCategoria(ticket.getCategoria());
        ticketExistente.setEtiquetas(ticket.getEtiquetas());
        ticketExistente.setFechaActualizacion(LocalDateTime.now());

        return ticketRepository.save(ticketExistente);
    }

    // Eliminar un ticket
    @Transactional
    public void eliminarTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
            
        // Verificar si el ticket puede ser eliminado
        if (!"CERRADO".equals(ticket.getEstado()) && !"RESUELTO".equals(ticket.getEstado())) {
            throw new IllegalStateException("Solo se pueden eliminar tickets cerrados o resueltos");
        }
        
        ticketRepository.deleteById(id);
    }

    // Buscar tickets por estado
    public List<Ticket> buscarPorEstado(String estado) {
        return ticketRepository.findByEstado(estado);
    }

    // Buscar tickets por categoría
    public List<Ticket> buscarPorCategoria(String categoria) {
        return ticketRepository.findByCategoria(categoria);
    }

    // Actualizar prioridad del ticket
    public void actualizarPrioridad(Long ticketId, String nuevaPrioridad) {
        ticketRepository.findById(ticketId).ifPresent(ticket -> {
            ticket.setPrioridad(nuevaPrioridad);
            ticketRepository.save(ticket);
        });
    }

    // Generar número de referencia único
    private String generarNumeroReferencia() {
        String año = String.valueOf(LocalDateTime.now().getYear());
        long numTickets = ticketRepository.count() + 1;
        return String.format("TKT-%s-%04d", año, numTickets);
    }

    // Obtener estadísticas de tickets
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        estadisticas.put("totalTickets", ticketRepository.count());
        estadisticas.put("ticketsAbiertos", ticketRepository.countByEstado("ABIERTO"));
        estadisticas.put("ticketsEnProgreso", ticketRepository.countByEstado("EN_PROGRESO"));
        estadisticas.put("ticketsResueltos", ticketRepository.countByEstado("RESUELTO"));
        estadisticas.put("tiempoPromedioResolucion", calcularTiempoPromedioResolucion());
        
        return estadisticas;
    }

    // Calcular tiempo promedio de resolución
    private double calcularTiempoPromedioResolucion() {
        List<Ticket> ticketsResueltos = ticketRepository.findByEstado("RESUELTO");
        if (ticketsResueltos.isEmpty()) return 0.0;

        return ticketsResueltos.stream()
            .mapToLong(Ticket::getTiempoResolucion)
            .average()
            .orElse(0.0);
    }

    // Agregar etiquetas al ticket
    @Transactional
    public Ticket agregarEtiquetas(Long ticketId, List<String> etiquetas) {
        return ticketRepository.findById(ticketId)
            .map(ticket -> {
                if (ticket.getEtiquetas() == null) {
                    ticket.setEtiquetas(new ArrayList<>());
                }
                ticket.getEtiquetas().addAll(etiquetas);
                return ticketRepository.save(ticket);
            })
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
    }

    // Buscar tickets por etiquetas
    public List<Ticket> buscarPorEtiquetas(List<String> etiquetas) {
        return ticketRepository.findByEtiquetasContaining(etiquetas);
    }

    // Buscar tickets por múltiples criterios
    public List<Ticket> buscarTickets(Map<String, Object> criterios) {
        String estado = (String) criterios.get("estado");
        String categoria = (String) criterios.get("categoria");
        String prioridad = (String) criterios.get("prioridad");
        
        return ticketRepository.findByFiltros(estado, categoria, prioridad);
    }

    // Cerrar ticket
    public void cerrarTicket(Long ticketId, String motivo) {
        ticketRepository.findById(ticketId).ifPresent(ticket -> {
            ticket.cambiarEstado("CERRADO");
            ticket.setDescripcion(ticket.getDescripcion() + "\nMotivo de cierre: " + motivo);
            ticketRepository.save(ticket);
        });
    }

    // Validación de ticket
    private void validarTicket(Ticket ticket) {
        if (ticket.getTitulo() == null || ticket.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("El título es requerido");
        }
        if (ticket.getDescripcion() == null || ticket.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción es requerida");
        }
        if (ticket.getCategoria() == null || ticket.getCategoria().trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría es requerida");
        }
    }

    // Agregar este nuevo método
    public List<Ticket> buscarTodos() {
        return ticketRepository.findAll();
    }

}
