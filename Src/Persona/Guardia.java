package Persona;

import Enums.Turno;
import Enums.Puesto;
import Ingresos.SistemaIngreso;
import Ingresos.AutorizacionIngreso;
import Ingresos.RegistroIngreso;
import Equipos.Equipo;
import Equipos.Falla;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Guardia extends Personal {
    private Turno turno;
    private Puesto puesto;
    private SistemaIngreso sistemaIngreso;

    public Guardia(String nombre, String apellido, String dni, String telefono,
                   String email, int legajo, LocalDate fechaIngreso, Turno turno,
                   Puesto puesto, SistemaIngreso sistemaIngreso) {
        super(nombre, apellido, dni, telefono, email, legajo, fechaIngreso);
        if (turno == null || puesto == null || sistemaIngreso == null) {
            throw new IllegalArgumentException("Faltan datos del guardia.");
        }
        this.turno = turno;
        this.puesto = puesto;
        this.sistemaIngreso = sistemaIngreso;
    }
    public Falla registrarFalla(Equipo equipo, String descripcion) {
        if (equipo == null) throw new IllegalArgumentException("Equipo obligatorio.");
        Falla falla = new Falla(equipo, this, descripcion);
        equipo.registrarFalla(falla);
        return falla;
    }
    public AutorizacionIngreso consultarAutorizacion(Visitante visitante, LocalDateTime fechaHora) {
        return sistemaIngreso.buscarAutorizacionVigente(visitante, fechaHora);
    }
    public RegistroIngreso registrarIngreso(Visitante visitante, LocalDateTime fechaHora) {
        return sistemaIngreso.registrarIngreso(visitante, this, fechaHora);
    }
    public void registrarEgreso(RegistroIngreso registro, LocalDateTime fechaHora) {
        sistemaIngreso.registrarEgreso(registro, fechaHora);
    }
    public Turno getTurno() { return turno; }
    public Puesto getPuesto() { return puesto; }
}
