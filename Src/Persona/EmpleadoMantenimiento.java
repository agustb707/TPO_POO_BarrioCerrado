
package Persona;

import java.time.LocalDate;

import Excepciones.ReglaNegocioException;
import Equipos.OrdenMantenimiento;
import Enums.Especialidad;

public class EmpleadoMantenimiento extends Personal {

    private final Especialidad especialidad;

    public EmpleadoMantenimiento(String nombre, String apellido, String dni, String telefono, String email, int legajo, LocalDate fechaIngreso, Especialidad especialidad) {

        super(nombre, apellido, dni, telefono, email, legajo, fechaIngreso);

        if (especialidad == null) {
            throw new IllegalArgumentException("La especialidad es obligatoria.");
        }

        this.especialidad = especialidad;
    }

    // REGISTRAR REPARACION

    public void registrarReparacion(OrdenMantenimiento orden, String descripcion, LocalDate fecha) {

        verificarAsignacion(orden);

        orden.registrarReparacion(fecha, descripcion);
    }

    // CERRAR ORDEN

    public void cerrarOrden(OrdenMantenimiento orden) {

        verificarAsignacion(orden);

        orden.cerrarOrden(LocalDate.now());
    }

    // VERIFICAR EMPLEADO ASIGNADO

    private void verificarAsignacion(OrdenMantenimiento orden) {

        if (orden == null) {
            throw new IllegalArgumentException("La orden es obligatoria.");
        }

        if (orden.getTecnico() == null || orden.getTecnico().getLegajo() != this.getLegajo()) {

            throw new ReglaNegocioException("La orden no esta asignada a este empleado.");
        }
    }

    // GETTER

    public Especialidad getEspecialidad() {
        return especialidad;
    }
}
