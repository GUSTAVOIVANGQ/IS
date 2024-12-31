package com.version.nutrition.service;

import com.version.nutrition.model.RespuestaTicket;
import com.version.nutrition.model.Ticket;
import com.version.nutrition.model.Usuario;
import com.version.nutrition.repository.RespuestaTicketRepository;
import com.version.nutrition.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class RespuestaTicketService {

    @Autowired
    private RespuestaTicketRepository respuestaTicketRepository;

    @Autowired
    private TicketRepository ticketRepository;

    // Crear nueva respuesta
    public RespuestaTicket crearRespuesta(Long ticketId, String contenido, Usuario autor, boolean esAutomatica) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        RespuestaTicket respuesta = new RespuestaTicket();
        respuesta.setContenido(contenido);
        respuesta.setAutor(autor);
        respuesta.setTicket(ticket);
        respuesta.setEsRespuestaAutomatica(esAutomatica);
        respuesta.setFechaCreacion(LocalDateTime.now());
        respuesta.setEstado("PENDIENTE");

        // Si es respuesta automática, establecer el tipo correspondiente
        if (esAutomatica) {
            respuesta.setTipo("AUTOMATICA");
        } else {
            respuesta.setTipo("PUBLICA");
        }

        // Actualizar el estado del ticket si es necesario
        if ("ABIERTO".equals(ticket.getEstado())) {
            ticket.setEstado("EN_PROGRESO");
            ticketRepository.save(ticket);
        }

        return respuestaTicketRepository.save(respuesta);
    }

    // Obtener respuestas de un ticket
    public List<RespuestaTicket> obtenerRespuestasTicket(Long ticketId) {
        return respuestaTicketRepository.findByTicketId(ticketId);
    }

    // Marcar respuesta como leída
    public void marcarComoLeida(Long respuestaId) {
        respuestaTicketRepository.findById(respuestaId).ifPresent(respuesta -> {
            respuesta.marcarComoLeida();
            respuestaTicketRepository.save(respuesta);
        });
    }

    // Generar respuesta automática basada en palabras clave
    public RespuestaTicket generarRespuestaAutomatica(Ticket ticket) {
        Map<String, String> plantillasRespuesta = new HashMap<>();
        plantillasRespuesta.put("CONSULTA_DIETA", 
            "Gracias por tu consulta sobre el plan de dieta. Un nutricionista la revisará pronto.");
        plantillasRespuesta.put("PROBLEMA_TECNICO", 
            "Hemos registrado tu problema técnico. Nuestro equipo de soporte lo atenderá a la brevedad.");
        plantillasRespuesta.put("AJUSTE_PLAN", 
            "Tu solicitud de ajuste al plan será revisada por tu nutricionista asignado.");

        String respuestaAutomatica = plantillasRespuesta.getOrDefault(ticket.getCategoria(),
            "Gracias por tu mensaje. Un profesional te responderá pronto.");

        return crearRespuesta(
            ticket.getId(),
            respuestaAutomatica,
            null, // Usuario sistema
            true  // Es respuesta automática
        );
    }

    // Editar respuesta
    public RespuestaTicket editarRespuesta(Long respuestaId, String nuevoContenido) {
        return respuestaTicketRepository.findById(respuestaId)
            .map(respuesta -> {
                respuesta.setContenido(nuevoContenido);
                respuesta.setFechaModificacion(LocalDateTime.now());
                return respuestaTicketRepository.save(respuesta);
            })
            .orElseThrow(() -> new RuntimeException("Respuesta no encontrada"));
    }

    // Eliminar respuesta
    public void eliminarRespuesta(Long respuestaId) {
        respuestaTicketRepository.deleteById(respuestaId);
    }

    // Marcar respuesta como destacada
    public void toggleDestacada(Long respuestaId) {
        respuestaTicketRepository.findById(respuestaId).ifPresent(respuesta -> {
            respuesta.toggleDestacada();
            respuestaTicketRepository.save(respuesta);
        });
    }

    // Buscar respuestas por tipo
    public List<RespuestaTicket> buscarRespuestasPorTipo(String tipo) {
        return respuestaTicketRepository.findByTipo(tipo);
    }

    // Obtener estadísticas de respuestas
    public Map<String, Object> obtenerEstadisticas(Long ticketId) {
        Map<String, Object> estadisticas = new HashMap<>();
        List<RespuestaTicket> respuestas = respuestaTicketRepository.findByTicketId(ticketId);

        estadisticas.put("totalRespuestas", respuestas.size());
        estadisticas.put("respuestasAutomaticas", 
            respuestas.stream().filter(RespuestaTicket::isEsRespuestaAutomatica).count());
        estadisticas.put("tiempoPromedioRespuesta", calcularTiempoPromedioRespuesta(respuestas));

        return estadisticas;
    }

    private double calcularTiempoPromedioRespuesta(List<RespuestaTicket> respuestas) {
        if (respuestas.isEmpty()) return 0;

        return respuestas.stream()
            .mapToLong(r -> java.time.Duration.between(
                r.getTicket().getFechaCreacion(), 
                r.getFechaCreacion()
            ).toHours())
            .average()
            .orElse(0);
    }
}
