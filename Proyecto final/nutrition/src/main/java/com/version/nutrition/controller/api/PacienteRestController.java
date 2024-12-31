package com.version.nutrition.controller.api;

import com.version.nutrition.model.Paciente;
import com.version.nutrition.service.PacienteService;
import com.version.nutrition.dto.PacienteDTO;
import com.version.nutrition.mapper.PacienteMapper;
import com.version.nutrition.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteRestController {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private PacienteMapper pacienteMapper;

    // POST /api/pacientes - Registrar nuevo paciente
    @PostMapping
    public ResponseEntity<ApiResponse<Paciente>> crearPaciente(@Valid @RequestBody Paciente paciente) {
        try {
            Paciente nuevoPaciente = pacienteService.registrarPaciente(paciente);
            return ResponseEntity.ok(new ApiResponse<>(
                true, "Paciente registrado exitosamente", nuevoPaciente));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "Error al registrar paciente: " + e.getMessage(), null));
        }
    }

    // GET /api/pacientes - Listar todos los pacientes
    @GetMapping
    public ResponseEntity<ApiResponse<List<Paciente>>> listarPacientes() {
        try {
            List<Paciente> pacientes = pacienteService.listarTodos();
            return ResponseEntity.ok(new ApiResponse<>(
                true, "Lista de pacientes recuperada exitosamente", pacientes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al recuperar la lista de pacientes", null));
        }
    }

    // GET /api/pacientes/{id} - Obtener paciente por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Paciente>> obtenerPaciente(@PathVariable Long id) {
        try {
            return pacienteService.buscarPorId(id)
                .map(paciente -> ResponseEntity.ok(new ApiResponse<>(
                    true, "Paciente encontrado", paciente)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Paciente no encontrado", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al recuperar el paciente", null));
        }
    }

    // PUT /api/pacientes/{id} - Actualizar paciente
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Paciente>> actualizarPaciente(
            @PathVariable Long id, 
            @Valid @RequestBody Paciente paciente) {
        try {
            Paciente pacienteActualizado = pacienteService.actualizarPacienteAPI(id, paciente);
            return ResponseEntity.ok(new ApiResponse<>(
                true, "Paciente actualizado exitosamente", pacienteActualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al actualizar el paciente", null));
        }
    }

    // DELETE /api/pacientes/{id} - Eliminar paciente
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarPaciente(@PathVariable Long id) {
        try {
            pacienteService.eliminarPaciente(id);
            return ResponseEntity.ok(new ApiResponse<>(
                true, "Paciente eliminado exitosamente", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Paciente no encontrado", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al eliminar el paciente", null));
        }
    }

    // GET /api/pacientes/buscar - Buscar pacientes por criterios
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<Paciente>>> buscarPacientes(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido) {
        try {
            List<Paciente> pacientes = pacienteService
                .buscarPorNombreOApellido(nombre, apellido);
            return ResponseEntity.ok(new ApiResponse<>(
                true, "Búsqueda realizada exitosamente", pacientes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error en la búsqueda de pacientes", null));
        }
    }
}
