package Persona;

import java.time.LocalDate;
import Reclamos.Reclamo;

public abstract class Personal extends Persona {
    private int legajo;
    private LocalDate fechaIngreso;
    protected Personal(String nombre, String apellido, String dni, String telefono,
                       String email, int legajo, LocalDate fechaIngreso) {
        super(nombre, apellido, dni, telefono, email);
        if (legajo <= 0 || fechaIngreso == null) {
            throw new IllegalArgumentException("Legajo o fecha de ingreso invalidos.");
        }
        this.legajo = legajo;
        this.fechaIngreso = fechaIngreso;
    }
    public void comentarReclamo(Reclamo reclamo, String texto) {
        if (reclamo == null || texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El reclamo y el texto son obligatorios.");
        }
        reclamo.agregarComentario(this, texto);
    }
    public int getLegajo() { return legajo; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
}
