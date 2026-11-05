package Equipos;

import java.time.LocalDate;
import Excepciones.ReglaNegocioException;
import Persona.Administrador;
import Persona.EmpleadoMantenimiento;
import Enums.Prioridad;
import Enums.EstadoEquipo;

/** Creador GRASP de las órdenes: la genera el Administrador a partir de una falla. */
public class OrdenMantenimiento {
    private int SECUENCIA = 0;

    private final int numero;
    private final LocalDate fechaApertura;
    private LocalDate fechaCierre;
    private final Prioridad prioridad;
    private final String descripcionTrabajo;
    private String descripcionReparacion;
    private LocalDate fechaReparacion;
    private final Falla falla;
    private final Administrador creador;
    private EmpleadoMantenimiento tecnico;

    public OrdenMantenimiento(Administrador creador, Falla falla, Prioridad prioridad, String descripcionTrabajo) {
        if (descripcionTrabajo == null || descripcionTrabajo.isBlank()) {
            throw new IllegalArgumentException("La descripción del trabajo es obligatoria");
        }
        this.numero = SECUENCIA++;
        this.fechaApertura = LocalDate.now();
        this.creador = creador;
        this.falla = falla;
        this.prioridad = prioridad;
        this.descripcionTrabajo = descripcionTrabajo;
    }

    public void asignarEmpleado(EmpleadoMantenimiento empleado) {
        if (!estaAbierta()) {
            throw new ReglaNegocioException("No se puede asignar un empleado a una orden cerrada");
        }
        this.tecnico = empleado ;
    }

    public void registrarReparacion(LocalDate fecha, String descripcion) {
        if (!estaAbierta()) {
            throw new ReglaNegocioException("La orden ya está cerrada");
        }
        if (tecnico == null) {
            throw new ReglaNegocioException("La orden no tiene técnico asignado");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción de la reparación es obligatoria");
        }
        if (fecha.isBefore(fechaApertura)) {
            throw new ReglaNegocioException("La reparación no puede ser anterior a la apertura de la orden");
        }
        this.fechaReparacion = fecha;
        this.descripcionReparacion = descripcion;
    }

    public void cerrarOrden(LocalDate fecha) {
        if (!estaAbierta()) {
            throw new ReglaNegocioException("La orden ya está cerrada");
        }
        if (fechaReparacion == null) {
            throw new ReglaNegocioException("No se puede cerrar una orden sin reparación registrada");
        }
        if (fecha.isBefore(fechaReparacion)) {
            throw new ReglaNegocioException("El cierre no puede ser anterior a la reparación");
        }
        this.fechaCierre = fecha;
        Equipo equipo = falla.getEquipo();
        if (equipo.getFallas().stream().noneMatch(Falla::estaActiva)) {
            equipo.cambiarEstado(EstadoEquipo.OPERATIVO);
        }
    }

    public boolean estaAbierta() {
        return fechaCierre == null;
    }

    public int getNumero() { return numero; }
    public LocalDate getFechaApertura() { return fechaApertura; }
    public LocalDate getFechaCierre() { return fechaCierre; }
    public Prioridad getPrioridad() { return prioridad; }
    public String getDescripcionTrabajo() { return descripcionTrabajo; }
    public String getDescripcionReparacion() { return descripcionReparacion; }
    public LocalDate getFechaReparacion() { return fechaReparacion; }
    public Falla getFalla() { return falla; }
    public Administrador getCreador() { return creador; }
    public EmpleadoMantenimiento getTecnico() { return tecnico; }
}

