package Persona;

import java.time.LocalDate;
import Excepciones.ReglaNegocioException;
import Equipos.OrdenMantenimiento;
import Equipos.Falla;
import Equipos.Equipo;
import Enums.Especialidad;
import Enums.Prioridad;
import Barrio.BarrioCerrado;
import Persona.Personal;


public class EmpleadoMantenimiento extends Personal {
    private final Especialidad especialidad;

    public EmpleadoMantenimiento(String nombre, String apellido, String dni, String telefono, String email, int legajo, LocalDate fechaIngreso, Especialidad especialidad) {
        super(nombre, apellido, dni, telefono, email, legajo, fechaIngreso);
        this.especialidad = especialidad;
    }

    public void registrarReparacion(OrdenMantenimiento orden, String descripcion, LocalDate fecha) {
        verificarAsignacion(orden);
        orden.registrarReparacion(fecha, descripcion);
    }

    public void cerrarOrden(OrdenMantenimiento orden) {
        verificarAsignacion(orden);
        orden.cerrarOrden(LocalDate.now());
    }

    private void verificarAsignacion(OrdenMantenimiento orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La orden es obligatoria");
        }
        if (!this.equals(orden.getTecnico())) {
            throw new ReglaNegocioException("La orden no está asignada a este empleado");
        }
    }

    public Especialidad getEspecialidad() { return especialidad; }
}
