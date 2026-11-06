
package Ingresos;

import Persona.Guardia;
import java.time.LocalDateTime;

public class RegistroIngreso {

    private int numero;
    private LocalDateTime fechaHoraIngreso;
    private LocalDateTime fechaHoraEgreso;
    private AutorizacionIngreso autorizacion;
    private Guardia guardia;

    public RegistroIngreso(int numero, AutorizacionIngreso autorizacion, Guardia guardia, LocalDateTime fechaHora) {

        if (numero <= 0) {
            throw new IllegalArgumentException("El numero de ingreso debe ser positivo.");
        }

        if (autorizacion == null || guardia == null || fechaHora == null) {
            throw new IllegalArgumentException("Faltan datos del ingreso.");
        }

        if (!autorizacion.esValida(fechaHora)) {
            throw new IllegalArgumentException("El visitante no tiene una autorizacion valida.");
        }

        this.numero = numero;
        this.autorizacion = autorizacion;
        this.guardia = guardia;
        this.fechaHoraIngreso = fechaHora;
        this.fechaHoraEgreso = null;
    }

    // REGISTRAR EGRESO

    public void registrarEgreso(LocalDateTime fechaHora) {

        if (!estaDentro()) {
            throw new IllegalStateException("El egreso ya fue registrado.");
        }

        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha de egreso es obligatoria.");
        }

        if (fechaHora.isBefore(fechaHoraIngreso)) {
            throw new IllegalArgumentException("El egreso no puede ser anterior al ingreso.");
        }

        this.fechaHoraEgreso = fechaHora;
    }

    // CONSULTAR SI EL VISITANTE SIGUE DENTRO

    public boolean estaDentro() {
        return fechaHoraEgreso == null;
    }

    // GETTERS

    public int getNumero() {
        return numero;
    }

    public AutorizacionIngreso getAutorizacion() {
        return autorizacion;
    }

    public Guardia getGuardia() {
        return guardia;
    }

    public LocalDateTime getFechaHoraIngreso() {
        return fechaHoraIngreso;
    }

    public LocalDateTime getFechaHoraEgreso() {
        return fechaHoraEgreso;
    }
}
