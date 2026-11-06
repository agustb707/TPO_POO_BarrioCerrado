package Equipos;

import java.time.LocalDate;
import java.util.List;
import Excepciones.ReglaNegocioException;
import Persona.Administrador;
import Persona.EmpleadoMantenimiento;
import Enums.Prioridad;
import Enums.EstadoEquipo;

public class OrdenMantenimiento {
    private static int secuencia = 0;
    private int numero;
    private LocalDate fechaApertura;
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
        if (creador == null || falla == null || prioridad == null || descripcionTrabajo == null
                || descripcionTrabajo.isBlank()) {
            throw new IllegalArgumentException("Datos de la orden invalidos.");
        }
        numero = ++secuencia;
        fechaApertura = LocalDate.now();
        this.creador = creador;
        this.falla = falla;
        this.prioridad = prioridad;
        this.descripcionTrabajo = descripcionTrabajo;
        falla.asociarOrden(this);
        falla.getEquipo().registrarOrden(this);
    }
    public void asignarEmpleado(EmpleadoMantenimiento empleado) {
        if (!estaAbierta()) throw new ReglaNegocioException("La orden esta cerrada.");
        if (empleado == null) throw new IllegalArgumentException("El empleado es obligatorio.");
        tecnico = empleado;
    }
    public void registrarReparacion(LocalDate fecha, String descripcion) {
        if (!estaAbierta()) throw new ReglaNegocioException("La orden esta cerrada.");
        if (tecnico == null) throw new ReglaNegocioException("La orden no tiene tecnico.");
        if (fecha == null || descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("Datos de reparacion obligatorios.");
        }
        if (fecha.isBefore(fechaApertura)) throw new ReglaNegocioException("Reparacion anterior a apertura.");
        fechaReparacion = fecha;
        descripcionReparacion = descripcion;
    }
    public void cerrarOrden(LocalDate fecha) {
        if (!estaAbierta()) throw new ReglaNegocioException("La orden esta cerrada.");
        if (tecnico == null || fechaReparacion == null || descripcionReparacion == null
                || descripcionReparacion.isBlank()) {
            throw new ReglaNegocioException("Primero debe registrarse la reparacion.");
        }
        if (fecha == null || fecha.isBefore(fechaReparacion)) {
            throw new ReglaNegocioException("Fecha de cierre invalida.");
        }
        Equipo equipo = falla.getEquipo();
        fechaCierre = fecha;
        boolean quedanFallas = false;
        for (Falla fallaActual : equipo.getFallas()) {
            if (fallaActual.estaActiva()) quedanFallas = true;
        }
        if (!quedanFallas) equipo.finalizarReparacion();
    }
    public void restaurarDatos(int numero, LocalDate fechaApertura,
                               LocalDate fechaReparacion, String descripcionReparacion, LocalDate fechaCierre) {
        this.numero = numero;
        this.fechaApertura = fechaApertura;
        this.fechaReparacion = fechaReparacion;
        this.descripcionReparacion = descripcionReparacion;
        this.fechaCierre = fechaCierre;
    }
    public boolean estaAbierta() { return fechaCierre == null; }
    public static void restaurarSecuencia(List<Equipo> equipos) {
        secuencia = 0;
        for (Equipo equipo : equipos) {
            for (OrdenMantenimiento orden : equipo.getOrdenes()) {
                if (orden.getNumero() > secuencia) secuencia = orden.getNumero();
            }
        }
    }
    public int getNumero() { return numero; }
    public LocalDate getFechaApertura() { return fechaApertura; }
    public LocalDate getFechaCierre() { return fechaCierre; }
    public Prioridad getPrioridad() { return prioridad; }
    public String getDescripcionTrabajo() { return descripcionTrabajo; }
    public String getDescripcionReparacion() { return descripcionReparacion; }
    public LocalDate getFechaReparacion() { return fechaReparacion; }
    public Falla getFalla() { return falla; }
    public Equipo getEquipo() { return falla.getEquipo(); }
    public Administrador getCreador() { return creador; }
    public EmpleadoMantenimiento getTecnico() { return tecnico; }
}
