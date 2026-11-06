package Ingresos;

import Persona.Guardia;
import java.time.LocalDateTime;

public class RegistroIngreso {
    private int numero;
    private LocalDateTime fechaHoraIngreso;
    private LocalDateTime fechaHoraEgreso;
    private AutorizacionIngreso autorizacion;
    private Guardia guardia;

    public RegistroIngreso(int numero, AutorizacionIngreso autorizacion,
                           Guardia guardia, LocalDateTime fechaHora) {
        if (numero <= 0 || autorizacion == null || guardia == null || fechaHora == null) {
            throw new IllegalArgumentException("Datos del ingreso invalidos.");
        }
        if (!autorizacion.esValida(fechaHora)) {
            throw new IllegalArgumentException("No hay autorizacion valida.");
        }
        this.numero = numero;
        this.autorizacion = autorizacion;
        this.guardia = guardia;
        fechaHoraIngreso = fechaHora;
    }
    public void registrarEgreso(LocalDateTime fechaHora) {
        if (!estaDentro()) throw new IllegalStateException("El egreso ya fue registrado.");
        if (fechaHora == null || fechaHora.isBefore(fechaHoraIngreso)) {
            throw new IllegalArgumentException("El egreso no puede ser anterior al ingreso.");
        }
        fechaHoraEgreso = fechaHora;
    }
    public boolean estaDentro() { return fechaHoraEgreso == null; }
    public int getNumero() { return numero; }
    public AutorizacionIngreso getAutorizacion() { return autorizacion; }
    public Guardia getGuardia() { return guardia; }
    public LocalDateTime getFechaHoraIngreso() { return fechaHoraIngreso; }
    public LocalDateTime getFechaHoraEgreso() { return fechaHoraEgreso; }
}
