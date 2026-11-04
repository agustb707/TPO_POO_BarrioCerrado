package Persona;

import java.time.LocalDate;
import Enums.Turno;
import Enums.Puesto;


public class Guardia extends Personal {
    private final Turno turno;
    private final Puesto puesto;
    private final SistemaIngreso sistemaIngreso;

    public Guardia(String nombre, String apellido, String dni, String telefono, String email,
                   int legajo, LocalDate fechaIngreso, Turno turno, Puesto puesto, SistemaIngreso sistemaIngreso) {
        super(nombre, apellido, dni, telefono, email, legajo, fechaIngreso);
        this.turno = turno;
        this.puesto = puesto;
        this.sistemaIngreso = sistemaIngreso;
    }

}
