package com.version.nutrition.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Node
public class Paciente extends Usuario {
    // Remove these lines
    //@Id
    //@GeneratedValue
    //private Long id;

    private String nombre;
    private String apellido;
    private int edad;
    private String genero;
    private double estatura;
    private float peso;
    // private String email;
    // private String password;
    // private String resetPasswordToken;
    private String enfermedadesCronicas;
    private String preferenciasAlimenticias;
    private String telefono;
    private String direccion;
    private String objetivoNutricional;
    private float imc;
    private String alergias;
    private String historialMedico;

    @Relationship(type = "HAS_PLAN")
    private List<PlanDieta> planesDeDieta = new ArrayList<>();

    @Relationship(type = "TIENE_CITA")
    private List<Cita> citas;

    @Relationship(type = "TIENE_SEGUIMIENTO")
    private List<SeguimientoPeso> seguimientoPeso = new ArrayList<>();

    // Información médica adicional
    private String grupoSanguineo;
    private boolean embarazo;
    private String actividadFisica; // SEDENTARIO, MODERADO, ACTIVO, MUY_ACTIVO
    private int horasDeActividad;
    private LocalDate fechaRegistro;
    private LocalDate ultimaConsulta;
    
    // Métricas calculadas
    private double indiceMasaCorporal;
    private double tasaMetabolicaBasal;
    private double pesoIdeal;
    private String estadoNutricional; // BAJO_PESO, NORMAL, SOBREPESO, OBESIDAD

    // Objetivos y seguimiento
    private double pesoObjetivo;
    private LocalDate fechaObjetivo;
    private String estadoSeguimiento; // ACTIVO, PAUSADO, COMPLETADO
    private int diasSeguimiento;
    
    @Relationship(type = "TIENE_MEDICION")
    private List<Medicion> historialMediciones;

    // Clase interna para mediciones
    @Node
    public static class Medicion {
        @Id @GeneratedValue
        private Long id;
        private LocalDate fecha;
        private double peso;
        private double circunferenciaCintura;
        private double circunferenciaCadera;
        private double porcentajeGrasaCorporal;
        
        // Constructor y getters/setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }

        public double getPeso() {
            return peso;
        }

        public void setPeso(double peso) {
            this.peso = peso;
        }

        public double getCircunferenciaCintura() {
            return circunferenciaCintura;
        }

        public void setCircunferenciaCintura(double circunferenciaCintura) {
            this.circunferenciaCintura = circunferenciaCintura;
        }

        public double getCircunferenciaCadera() {
            return circunferenciaCadera;
        }

        public void setCircunferenciaCadera(double circunferenciaCadera) {
            this.circunferenciaCadera = circunferenciaCadera;
        }

        public double getPorcentajeGrasaCorporal() {
            return porcentajeGrasaCorporal;
        }

        public void setPorcentajeGrasaCorporal(double porcentajeGrasaCorporal) {
            this.porcentajeGrasaCorporal = porcentajeGrasaCorporal;
        }
    }

    // Constructores
    public Paciente() {
        super();
        this.fechaRegistro = LocalDate.now();
        this.historialMediciones = new ArrayList<>();
        this.estadoSeguimiento = "ACTIVO"; // Establecer estado por defecto
    }

    // Constructor adicional con parámetros básicos
    public Paciente(String nombre, String apellido, String email, String password) {
        super();
        this.nombre = nombre;
        this.apellido = apellido;
        this.setEmail(email);
        this.setPassword(password);
        this.fechaRegistro = LocalDate.now();
        this.historialMediciones = new ArrayList<>();
        this.planesDeDieta = new ArrayList<>();
        this.seguimientoPeso = new ArrayList<>();
        this.estadoSeguimiento = "ACTIVO"; // Establecer estado por defecto
    }

    // Métodos de cálculo
    public void calcularIMC() {
        if (estatura > 0) {
            this.indiceMasaCorporal = peso / (estatura * estatura);
            actualizarEstadoNutricional();
        }
    }

    private void actualizarEstadoNutricional() {
        if (indiceMasaCorporal < 18.5) {
            estadoNutricional = "BAJO_PESO";
        } else if (indiceMasaCorporal < 25) {
            estadoNutricional = "NORMAL";
        } else if (indiceMasaCorporal < 30) {
            estadoNutricional = "SOBREPESO";
        } else {
            estadoNutricional = "OBESIDAD";
        }
    }

    public void calcularTasaMetabolicaBasal() {
        // Fórmula de Harris-Benedict
        if ("MASCULINO".equals(genero)) {
            tasaMetabolicaBasal = 88.362 + (13.397 * peso) + (4.799 * estatura * 100) - (5.677 * edad);
        } else {
            tasaMetabolicaBasal = 447.593 + (9.247 * peso) + (3.098 * estatura * 100) - (4.330 * edad);
        }
    }

    // Métodos de seguimiento
    public void agregarMedicion(double peso, double cintura, double cadera, double grasaCorporal) {
        Medicion medicion = new Medicion();
        medicion.fecha = LocalDate.now();
        medicion.peso = peso;
        medicion.circunferenciaCintura = cintura;
        medicion.circunferenciaCadera = cadera;
        medicion.porcentajeGrasaCorporal = grasaCorporal;
        
        this.historialMediciones.add(medicion);
        this.peso = (float) peso;
        calcularIMC();
    }

    public double calcularProgreso() {
        if (pesoObjetivo == 0) return 0;
        double pesoInicial = seguimientoPeso.get(0).getPeso();
        double progreso = Math.abs(pesoInicial - peso) / Math.abs(pesoInicial - pesoObjetivo) * 100;
        return Math.min(progreso, 100);
    }

    public boolean necesitaActualizarMediciones() {
        if (historialMediciones.isEmpty()) return true;
        LocalDate ultimaMedicion = historialMediciones.get(historialMediciones.size() - 1).fecha;
        return LocalDate.now().minusDays(7).isAfter(ultimaMedicion);
    }

    // Getters and Setters for all properties
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

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public float getPeso() {
        return peso;
    }

    public void setPeso(float peso) {
        this.peso = peso;
        calcularIMC();
    }

    public String getEnfermedadesCronicas() {
        return enfermedadesCronicas;
    }

    public void setEnfermedadesCronicas(String enfermedadesCronicas) {
        this.enfermedadesCronicas = enfermedadesCronicas;
    }

    public String getPreferenciasAlimenticias() {
        return preferenciasAlimenticias;
    }

    public void setPreferenciasAlimenticias(String preferenciasAlimenticias) {
        this.preferenciasAlimenticias = preferenciasAlimenticias;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getObjetivoNutricional() {
        return objetivoNutricional;
    }

    public void setObjetivoNutricional(String objetivoNutricional) {
        this.objetivoNutricional = objetivoNutricional;
    }

    public float getImc() {
        return imc;
    }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public String getHistorialMedico() {
        return historialMedico;
    }

    public void setHistorialMedico(String historialMedico) {
        this.historialMedico = historialMedico;
    }

    public String getGrupoSanguineo() {
        return grupoSanguineo;
    }

    public void setGrupoSanguineo(String grupoSanguineo) {
        this.grupoSanguineo = grupoSanguineo;
    }

    public boolean isEmbarazo() {
        return embarazo;
    }

    public void setEmbarazo(boolean embarazo) {
        this.embarazo = embarazo;
    }

    public String getActividadFisica() {
        return actividadFisica;
    }

    public void setActividadFisica(String actividadFisica) {
        this.actividadFisica = actividadFisica;
    }

    public int getHorasDeActividad() {
        return horasDeActividad;
    }

    public void setHorasDeActividad(int horasDeActividad) {
        this.horasDeActividad = horasDeActividad;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    public LocalDate getUltimaConsulta() {
        return ultimaConsulta;
    }

    public void setUltimaConsulta(LocalDate ultimaConsulta) {
        this.ultimaConsulta = ultimaConsulta;
    }

    public double getIndiceMasaCorporal() {
        return indiceMasaCorporal;
    }

    public double getTasaMetabolicaBasal() {
        return tasaMetabolicaBasal;
    }

    public double getPesoIdeal() {
        return pesoIdeal;
    }

    public void setPesoIdeal(double pesoIdeal) {
        this.pesoIdeal = pesoIdeal;
    }

    public String getEstadoNutricional() {
        return estadoNutricional;
    }

    public double getPesoObjetivo() {
        return pesoObjetivo;
    }

    public void setPesoObjetivo(double pesoObjetivo) {
        this.pesoObjetivo = pesoObjetivo;
    }

    public LocalDate getFechaObjetivo() {
        return fechaObjetivo;
    }

    public void setFechaObjetivo(LocalDate fechaObjetivo) {
        this.fechaObjetivo = fechaObjetivo;
    }

    public String getEstadoSeguimiento() {
        return estadoSeguimiento;
    }

    public void setEstadoSeguimiento(String estadoSeguimiento) {
        this.estadoSeguimiento = estadoSeguimiento;
    }

    public int getDiasSeguimiento() {
        return diasSeguimiento;
    }

    public void setDiasSeguimiento(int diasSeguimiento) {
        this.diasSeguimiento = diasSeguimiento;
    }

    public List<Medicion> getHistorialMediciones() {
        return historialMediciones;
    }

    public double getEstatura() {
        return estatura;
    }

    public void setEstatura(double estatura) {
        this.estatura = estatura;
    }

    public List<SeguimientoPeso> getSeguimientoPeso() {
        return seguimientoPeso;
    }

    public void setSeguimientoPeso(List<SeguimientoPeso> seguimientoPeso) {
        this.seguimientoPeso = seguimientoPeso;
    }

    public List<PlanDieta> getPlanesDeDieta() {
        return planesDeDieta;
    }

    public void setPlanesDeDieta(List<PlanDieta> planesDeDieta) {
        this.planesDeDieta = planesDeDieta;
    }

    public List<Cita> getCitas() {
        return citas;
    }

    public void setCitas(List<Cita> citas) {
        this.citas = citas;
    }
    
}