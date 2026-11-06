
package Persona;

import java.time.LocalDate;
import Reclamos.Reclamo;

public abstract class Personal extends Persona {

    private int legajo;
    private LocalDate fechaIngreso;

    protected Personal(String nombre, String apellido, String dni, String telefono, String email, int legajo, LocalDate fechaIngreso) {

        super(nombre, apellido, dni, telefono, email);

        if (legajo <= 0 || fechaIngreso == null) {
            throw new IllegalArgumentException("El legajo y la fecha de ingreso son obligatorios.");
        }

        this.legajo = legajo;
        this.fechaIngreso = fechaIngreso;
    }

    // COMENTAR RECLAMO

    public void comentarReclamo(Reclamo reclamo, String texto) {

        if (reclamo == null) {
            throw new IllegalArgumentException("El reclamo es obligatorio.");
        }

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El comentario es obligatorio.");
        }

        reclamo.agregarComentario(this, texto);
    }

    // GETTERS

    public int getLegajo() {
        return legajo;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }
}
