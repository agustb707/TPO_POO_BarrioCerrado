
package Ingresos;

import Barrio.Lote;
import Persona.Residente;
import Persona.Visitante;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import Excepciones.ReglaNegocioException;

public class AutorizacionIngreso {

    private int numero;
    private LocalDate fechaAutorizada;
    private LocalTime horaDesde;
    private LocalTime horaHasta;
    private Visitante visitante;
    private Lote lote;
    private Residente autorizante;

    public AutorizacionIngreso(int numero, Visitante visitante,
                               Residente autorizante, Lote lote,
                               LocalDate fecha, LocalTime desde,
                               LocalTime hasta) {

        if (numero <= 0 || visitante == null || autorizante == null ||
                lote == null || fecha == null || desde == null || hasta == null) {
            throw new ReglaNegocioException("Faltan datos de la autorizacion.");
        }

        if (hasta.isBefore(desde)) {
            throw new ReglaNegocioException("El horario es invalido.");
        }

        if (!lote.puedeAutorizarIngreso(autorizante)) {
            throw new ReglaNegocioException("El residente no esta habilitado para autorizar ingresos.");
        }

        this.numero = numero;
        this.visitante = visitante;
        this.autorizante = autorizante;
        this.lote = lote;
        this.fechaAutorizada = fecha;
        this.horaDesde = desde;
        this.horaHasta = hasta;
    }

    // VALIDAR AUTORIZACION

    public boolean esValida(LocalDateTime fechaHora) {

        if (fechaHora == null) {
            return false;
        }

        return fechaAutorizada.equals(fechaHora.toLocalDate())
                && dentroDeFranja(fechaHora.toLocalTime());
    }

    // VERIFICAR VISITANTE

    public boolean correspondeAVisitante(Visitante otro) {

        if (otro == null) {
            return false;
        }

        return visitante.getDni().equals(otro.getDni());
    }

    // VERIFICAR HORARIO

    protected boolean dentroDeFranja(LocalTime hora) {
        return hora != null
                && !hora.isBefore(horaDesde)
                && !hora.isAfter(horaHasta);
    }

    // GETTERS

    public int getNumero() {
        return numero;
    }

    public Visitante getVisitante() {
        return visitante;
    }

    public Lote getLote() {
        return lote;
    }

    public Residente getAutorizante() {
        return autorizante;
    }

    public LocalDate getFechaAutorizada() {
        return fechaAutorizada;
    }

    public LocalTime getHoraDesde() {
        return horaDesde;
    }

    public LocalTime getHoraHasta() {
        return horaHasta;
    }
}
