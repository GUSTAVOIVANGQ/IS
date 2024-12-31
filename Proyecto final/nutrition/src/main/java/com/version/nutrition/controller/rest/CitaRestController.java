package com.version.nutrition.controller.rest;

import com.version.nutrition.dto.CitaDTO;
import com.version.nutrition.model.Cita;
import com.version.nutrition.service.CitaService;
import com.version.nutrition.mapper.CitaMapper;
import com.version.nutrition.exception.CitaException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/consultas")
public class CitaRestController {
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private CitaMapper citaMapper;

    // Crear nueva cita
    @PostMapping
    public ResponseEntity<?> crearCita(@Valid @RequestBody CitaDTO citaDTO) {
        try {
            Cita cita = citaService.crearCitaRest(citaDTO);
            CitaDTO citaCreada = citaMapper.toDTO(cita);
            return new ResponseEntity<>(citaCreada, HttpStatus.CREATED);
        } catch (CitaException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al crear la cita", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Listar todas las citas
    @GetMapping
    public ResponseEntity<?> listarCitas() {
        try {
            List<Cita> citas = citaService.listarTodasLasCitas();
            List<CitaDTO> citasDTO = citas.stream()
                .map(citaMapper::toDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(citasDTO);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al obtener las citas", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener cita por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCita(@PathVariable Long id) {
        try {
            return citaService.obtenerCitaPorId(id)
                .map(cita -> ResponseEntity.ok(citaMapper.toDTO(cita)))
                .orElse(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>("Error al obtener la cita", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Manejo de errores global para este controlador
    @ExceptionHandler(CitaException.class)
    public ResponseEntity<String> handleCitaException(CitaException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
