package Persona;

import java.time.LocalDate;

public abstract class Personal extends Persona {
    private int legajo;
    private LocalDate fechaIngreso;

    protected Personal(String nombre, String apellido, int dni, String telefono,
                       String email, int legajo, LocalDate fechaIngreso) {
        super(nombre, apellido, dni, telefono, email);
        if (legajo <= 0 || fechaIngreso == null) {
            throw new IllegalArgumentException("El legajo y la fecha de ingreso son obligatorios.");
        }
        this.legajo = legajo;
        this.fechaIngreso = fechaIngreso;
    }

    public int getLegajo() { return legajo; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
}
