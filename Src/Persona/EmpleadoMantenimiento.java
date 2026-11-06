package Persona;

import java.time.LocalDate;
import Excepciones.ReglaNegocioException;
import Equipos.OrdenMantenimiento;
import Enums.Especialidad;

public class EmpleadoMantenimiento extends Personal {
    private final Especialidad especialidad;
    public EmpleadoMantenimiento(String nombre, String apellido, int dni, int telefono,
                                 String email, int legajo, LocalDate fechaIngreso, Especialidad especialidad) {
        super(nombre, apellido, dni, telefono, email, legajo, fechaIngreso);
        if (especialidad == null) throw new IllegalArgumentException("Especialidad obligatoria.");
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
        if (orden == null) throw new IllegalArgumentException("La orden es obligatoria.");
        if (orden.getTecnico() == null || orden.getTecnico().getLegajo() != getLegajo()) {
            throw new ReglaNegocioException("La orden no esta asignada a este empleado.");
        }
    }
    public Especialidad getEspecialidad() { return especialidad; }
}