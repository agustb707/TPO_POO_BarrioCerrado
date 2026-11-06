
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

    public Administrador(String nombre, String apellido, String dni, String telefono, String email, int legajo, LocalDate fechaIngreso) {

        super(nombre, apellido, dni, telefono, email, legajo, fechaIngreso);
    }

    // ASIGNAR BARRIO

    public void setBarrio(BarrioCerrado barrio) {
        if (barrio == null) {
            throw new IllegalArgumentException("El barrio es obligatorio.");
        }

        this.barrio = barrio;
    }

    // ASIGNAR RECLAMO

    public void asignarReclamo(Reclamo reclamo, Personal empleado) {

        if (reclamo == null || empleado == null) {
            throw new IllegalArgumentException(
                    "El reclamo y el empleado son obligatorios.");
        }

        reclamo.asignarEmpleado(empleado);
    }

    // GENERAR ORDEN DE MANTENIMIENTO

    public OrdenMantenimiento generarOrdenMantenimiento(Falla falla, EmpleadoMantenimiento empleado, String descripcion) {

        if (falla == null || empleado == null) {
            throw new IllegalArgumentException(
                    "La falla y el empleado son obligatorios.");
        }

        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripcion del trabajo es obligatoria.");
        }

        if (barrio == null) {
            throw new ReglaNegocioException("El administrador no tiene un barrio asignado.");
        }

        Equipo equipo = falla.getEquipo();

        if (!barrio.getEquipos().contains(equipo)) {
            throw new ReglaNegocioException("El equipo no pertenece al barrio.");
        }

        if (!equipo.getFallas().contains(falla)) {
            throw new ReglaNegocioException("La falla no esta registrada en el equipo.");
        }

        if (falla.getOrden() != null) {
            throw new ReglaNegocioException("La falla ya tiene una orden de mantenimiento.");
        }

        int cantidadSector = barrio.contarFallasSector(equipo.getSector());

        Prioridad prioridad = equipo.determinarPrioridadFalla(cantidadSector);

        OrdenMantenimiento orden = new OrdenMantenimiento(this, falla, prioridad, descripcion);

        orden.asignarEmpleado(empleado);

        return orden;
    }

    // GETTER

    public BarrioCerrado getBarrio() {
        return barrio;
    }
}
