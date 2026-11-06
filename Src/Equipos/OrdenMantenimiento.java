
package Equipos;

import java.time.LocalDate;

import Excepciones.ReglaNegocioException;
import Persona.Administrador;
import Persona.EmpleadoMantenimiento;
import Enums.Prioridad;
import Enums.EstadoEquipo;

public class OrdenMantenimiento {

    private static int secuencia = 0;

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

    public OrdenMantenimiento(Administrador creador, Falla falla,
                              Prioridad prioridad, String descripcionTrabajo) {

        if (creador == null) {
            throw new IllegalArgumentException("El administrador es obligatorio.");
        }

        if (falla == null) {
            throw new IllegalArgumentException("La falla es obligatoria.");
        }

        if (prioridad == null) {
            throw new IllegalArgumentException("La prioridad es obligatoria.");
        }

        if (descripcionTrabajo == null || descripcionTrabajo.isBlank()) {
            throw new IllegalArgumentException("La descripcion del trabajo es obligatoria.");
        }

        this.numero = ++secuencia;
        this.fechaApertura = LocalDate.now();
        this.creador = creador;
        this.falla = falla;
        this.prioridad = prioridad;
        this.descripcionTrabajo = descripcionTrabajo;

        // Asociar la orden a la falla y al equipo
        falla.asociarOrden(this);
        falla.getEquipo().registrarOrden(this);
    }

    // ASIGNAR EMPLEADO

    public void asignarEmpleado(EmpleadoMantenimiento empleado) {

        if (!estaAbierta()) {
            throw new ReglaNegocioException("No se puede asignar un empleado a una orden cerrada.");
        }

        if (empleado == null) {
            throw new IllegalArgumentException("El empleado es obligatorio.");
        }

        this.tecnico = empleado;
    }

    // REGISTRAR REPARACION

    public void registrarReparacion(LocalDate fecha, String descripcion) {

        if (!estaAbierta()) {
            throw new ReglaNegocioException("La orden ya esta cerrada.");
        }

        if (tecnico == null) {
            throw new ReglaNegocioException("La orden no tiene tecnico asignado.");
        }

        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripcion de la reparacion es obligatoria.");
        }

        if (fecha.isBefore(fechaApertura)) {
            throw new ReglaNegocioException("La reparacion no puede ser anterior a la apertura.");
        }

        this.fechaReparacion = fecha;
        this.descripcionReparacion = descripcion;
    }

    // CERRAR ORDEN

    public void cerrarOrden(LocalDate fecha) {

        if (!estaAbierta()) {
            throw new ReglaNegocioException("La orden ya esta cerrada.");
        }

        if (tecnico == null || fechaReparacion == null ||
                descripcionReparacion == null || descripcionReparacion.isBlank()) {
            throw new ReglaNegocioException("Debe registrar el tecnico y la reparacion antes de cerrar.");
        }

        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de cierre es obligatoria.");
        }

        if (fecha.isBefore(fechaReparacion)) {
            throw new ReglaNegocioException("El cierre no puede ser anterior a la reparacion.");
        }

        this.fechaCierre = fecha;

        // Verificar si el equipo tiene otras fallas activas
        Equipo equipo = falla.getEquipo();
        boolean hayFallasActivas = false;

        for (Falla fallaActual : equipo.getFallas()) {
            if (fallaActual.estaActiva()) {
                hayFallasActivas = true;
            }
        }

        // Si todas las fallas estan resueltas, vuelve a operativo
        if (!hayFallasActivas) {
            equipo.cambiarEstado(EstadoEquipo.OPERATIVO);
        }
    }

    // ESTADO DE LA ORDEN

    public boolean estaAbierta() {
        return fechaCierre == null;
    }

    // GETTERS

    public int getNumero() {
        return numero;
    }

    public LocalDate getFechaApertura() {
        return fechaApertura;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public String getDescripcionTrabajo() {
        return descripcionTrabajo;
    }

    public String getDescripcionReparacion() {
        return descripcionReparacion;
    }

    public LocalDate getFechaReparacion() {
        return fechaReparacion;
    }

    public Falla getFalla() {
        return falla;
    }

    public Equipo getEquipo() {
        return falla.getEquipo();
    }

    public Administrador getCreador() {
        return creador;
    }

    public EmpleadoMantenimiento getTecnico() {
        return tecnico;
    }
}
