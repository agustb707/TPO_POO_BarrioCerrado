package Persona;

import java.time.LocalDate;
import Barrio.BarrioCerrado;
import Equipos.OrdenMantenimiento;
import Equipos.Falla;
import Equipos.Equipo;
import Enums.Prioridad;
import Excepciones.ReglaNegocioException;
import Reclamos.Reclamo;

public class Administrador extends Personal {
    private BarrioCerrado barrio;
    public Administrador(String nombre, String apellido, int dni, int telefono,
                         String email, int legajo, LocalDate fechaIngreso) {
        super(nombre, apellido, dni, telefono, email, legajo, fechaIngreso);
    }
    public void setBarrio(BarrioCerrado barrio) {
        if (barrio == null) throw new IllegalArgumentException("Barrio obligatorio.");
        this.barrio = barrio;
    }
    public void asignarReclamo(Reclamo reclamo, Personal empleado) {
        if (reclamo == null || empleado == null) {
            throw new IllegalArgumentException("Reclamo y empleado obligatorios.");
        }
        reclamo.asignarEmpleado(empleado);
    }
    public OrdenMantenimiento generarOrdenMantenimiento(Falla falla,
                                                        EmpleadoMantenimiento empleado, String descripcion) {
        if (falla == null || empleado == null || descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("Datos de mantenimiento invalidos.");
        }
        if (barrio == null) throw new ReglaNegocioException("No hay barrio asignado.");
        Equipo equipo = falla.getEquipo();
        if (!barrio.getEquipos().contains(equipo) || !equipo.getFallas().contains(falla)) {
            throw new ReglaNegocioException("La falla no pertenece al barrio.");
        }
        if (falla.getOrden() != null) throw new ReglaNegocioException("La falla ya tiene una orden.");
        int cantidadSector = barrio.contarFallasSector(equipo.getSector());
        Prioridad prioridad = equipo.determinarPrioridadFalla(cantidadSector);
        OrdenMantenimiento orden = new OrdenMantenimiento(this, falla, prioridad, descripcion);
        orden.asignarEmpleado(empleado);
        return orden;
    }
    public BarrioCerrado getBarrio() { return barrio; }
}