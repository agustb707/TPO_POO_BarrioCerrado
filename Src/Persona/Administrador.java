package Persona;
import java.time.LocalDate;
import Barrio.BarrioCerrado;
import Equipos.OrdenMantenimiento;
import Persona.Personal;
import Persona.Reclamo; //falta hacer la clase reclamo y todo el paquete
import Persona.EmpleadoMantenimiento;
import Equipos.Falla;
import Equipos.Equipo;
import Enums.Prioridad;

// SRP: inicia operaciones administrativas. No calcula expensas ni genera reportes. */
public class Administrador extends Personal {
    private BarrioCerrado barrio;

    public Administrador(String nombre, String apellido, String dni, String telefono, String email, int legajo, LocalDate fechaIngreso) {
        super(nombre, apellido, dni, telefono, email, legajo, fechaIngreso);
    }

    public void setBarrio(BarrioCerrado barrio) {
        this.barrio = barrio;
    }

    public void asignarReclamo(Reclamo reclamo, Personal empleado) {
        if (reclamo == null || empleado == null) {
            throw new IllegalArgumentException("El reclamo y el empleado son obligatorios");
        }
        reclamo.asignarEmpleado(empleado);
    }

    public OrdenMantenimiento generarOrdenMantenimiento(Falla falla, EmpleadoMantenimiento empleado,
                                                        String descripcion) {
        if (falla == null || empleado == null) {
            throw new IllegalArgumentException("La falla y el empleado son obligatorios");
        }
        Equipo equipo = falla.getEquipo();
        int cantidadSector = (barrio != null)
                ? barrio.contarFallasSector(equipo.getSector())
                : equipo.getFallas().size();
        Prioridad prioridad = equipo.determinarPrioridadFalla(cantidadSector);
        OrdenMantenimiento orden = new OrdenMantenimiento(this, falla, prioridad, descripcion);
        orden.asignarEmpleado(empleado);
        falla.asociarOrden(orden);
        return orden;
    }
}

