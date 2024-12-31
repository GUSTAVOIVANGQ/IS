package com.version.nutrition.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Node
public class Nutricionista extends Usuario {
    private String nombre;
    private String apellido;
    private String especialidad;
    @Relationship(type = "CREATES_PLAN")
    private List<PlanDieta> planesDeDieta;

    // Información profesional
    private String numeroLicencia;
    private String universidad;
    private int añosExperiencia;
    private String especialidades; // Lista separada por comas: "Deportiva,Clínica,Pediátrica"
    private String descripcionProfesional;
    
    // Información de contacto
    private String telefono;
    private String direccionConsultorio;
    private String horarioAtencion;
    
    // Información laboral
    private double tarifaConsulta;
    private String metodoPago;
    private boolean disponibleConsultaOnline;
    private int duracionConsultaMinutos;
    
    // Estadísticas
    private int pacientesActivos;
    private int consultasRealizadas;
    private double calificacionPromedio;

    // Relaciones
    @Relationship(type = "ATIENDE", direction = Relationship.Direction.OUTGOING)
    private List<Paciente> pacientes = new ArrayList<>();

    @Relationship(type = "TIENE_CITA")
    private List<Cita> citas;

    @Relationship(type = "HA_CREADO")
    private List<PlanDieta> planesCreados = new ArrayList<>();;

    // Constructor básico
    public Nutricionista() {
        super();
        initializeLists();
    }

    // Constructor con parámetros esenciales
    public Nutricionista(String nombre, String apellido, String email, String especialidad) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.setEmail(email); // Use the inherited setter
        this.especialidades = especialidad;
        this.setActivo(true);
        this.pacientesActivos = 0;
        this.consultasRealizadas = 0;
        this.calificacionPromedio = 0.0;
        initializeLists();
    }

    // Métodos de utilidad
    public boolean verificarDisponibilidad(LocalDateTime fecha) {
        return citas.stream()
                   .noneMatch(cita -> cita.getFecha().equals(fecha));
    }

    public void agregarPaciente(Paciente paciente) {
        if (pacientes == null) {
            pacientes = new ArrayList<>();
        }
        pacientes.add(paciente);
        pacientesActivos++;
    }

    public void registrarConsulta() {
        this.consultasRealizadas++;
    }

    public void actualizarCalificacion(double nuevaCalificacion) {
        this.calificacionPromedio = 
            (this.calificacionPromedio * this.consultasRealizadas + nuevaCalificacion) / 
            (this.consultasRealizadas + 1);
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getNumeroLicencia() {
        return numeroLicencia;
    }

    public void setNumeroLicencia(String numeroLicencia) {
        this.numeroLicencia = numeroLicencia;
    }

    public String getUniversidad() {
        return universidad;
    }

    public void setUniversidad(String universidad) {
        this.universidad = universidad;
    }

    public int getAñosExperiencia() {
        return añosExperiencia;
    }

    public void setAñosExperiencia(int añosExperiencia) {
        this.añosExperiencia = añosExperiencia;
    }

    public String getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(String especialidades) {
        this.especialidades = especialidades;
    }

    public String getDescripcionProfesional() {
        return descripcionProfesional;
    }

    public void setDescripcionProfesional(String descripcionProfesional) {
        this.descripcionProfesional = descripcionProfesional;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccionConsultorio() {
        return direccionConsultorio;
    }

    public void setDireccionConsultorio(String direccionConsultorio) {
        this.direccionConsultorio = direccionConsultorio;
    }

    public String getHorarioAtencion() {
        return horarioAtencion;
    }

    public void setHorarioAtencion(String horarioAtencion) {
        this.horarioAtencion = horarioAtencion;
    }

    public double getTarifaConsulta() {
        return tarifaConsulta;
    }

    public void setTarifaConsulta(double tarifaConsulta) {
        this.tarifaConsulta = tarifaConsulta;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public boolean isDisponibleConsultaOnline() {
        return disponibleConsultaOnline;
    }

    public void setDisponibleConsultaOnline(boolean disponibleConsultaOnline) {
        this.disponibleConsultaOnline = disponibleConsultaOnline;
    }

    public int getDuracionConsultaMinutos() {
        return duracionConsultaMinutos;
    }

    public void setDuracionConsultaMinutos(int duracionConsultaMinutos) {
        this.duracionConsultaMinutos = duracionConsultaMinutos;
    }

    public int getConsultasRealizadas() {
        return consultasRealizadas;
    }

    public double getCalificacionPromedio() {
        return calificacionPromedio;
    }

    @Override
    public String toString() {
        return "Nutricionista{" +
                "nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", especialidad='" + especialidad + '\'' +
                ", email='" + getEmail() + '\'' +
                ", numeroLicencia='" + numeroLicencia + '\'' +
                ", universidad='" + universidad + '\'' +
                ", añosExperiencia=" + añosExperiencia +
                ", pacientesActivos=" + pacientesActivos +
                '}';
    }

    // Override métodos abstractos si los hubiera en Usuario
    @Override
    public void setActivo(boolean activo) {
        super.setActivo(activo);
        // Lógica adicional específica para nutricionista si es necesaria
    }

    // Método para obtener el número real de pacientes activos
    public int getPacientesActivos() {
        if (pacientes == null) return 0;
        
        return (int) pacientes.stream()
                .filter(p -> p != null && 
                           p.getEstadoSeguimiento() != null && 
                           p.getEstadoSeguimiento().equals("ACTIVO"))
                .count();
    }

    public void setPacientesActivos(int pacientesActivos) {
        this.pacientesActivos = pacientesActivos;
    }

    // Inicialización de listas si son null
    private void initializeLists() {
        if (pacientes == null) pacientes = new ArrayList<>();
        if (citas == null) citas = new ArrayList<>();
        if (planesCreados == null) planesCreados = new ArrayList<>();
        if (planesDeDieta == null) planesDeDieta = new ArrayList<>();
    }

    public List<Paciente> getPacientes() {
        return pacientes != null ? pacientes : new ArrayList<>();
    }

    public void setPacientes(List<Paciente> pacientes) {
        this.pacientes = pacientes;
    }

    public List<Cita> getCitas() {
        return citas;
    }

    public void setCitas(List<Cita> citas) {
        this.citas = citas;
    }

    public List<PlanDieta> getPlanesCreados() {
        return planesCreados;
    }

    public void setPlanesCreados(List<PlanDieta> planesCreados) {
        this.planesCreados = planesCreados;
    }
}