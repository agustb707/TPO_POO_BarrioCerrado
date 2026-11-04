package Ingresos;

import Barrio.Lote;
import Persona.Residente;
import Persona.Visitante;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class AutorizacionIngreso {
    private int numero;
    private LocalDate fechaAutorizada;
    private LocalTime horaDesde;
    private LocalTime horaHasta;
    private Visitante visitante;
    private Lote lote;
    private Residente autorizante;

    public AutorizacionIngreso(int numero, Visitante visitante, Residente autorizante,
                               Lote lote, LocalDate fecha, LocalTime desde, LocalTime hasta) {
        if (numero <= 0 || visitante == null || autorizante == null || lote == null
                || fecha == null || desde == null || hasta == null) {
            throw new IllegalArgumentException("Faltan datos de la autorizacion.");
        }
        if (hasta.isBefore(desde)) throw new IllegalArgumentException("El horario es invalido.");
        if (!lote.puedeAutorizarIngreso(autorizante)) {
            throw new IllegalArgumentException("El residente no esta habilitado para este lote.");
        }
        this.numero = numero;
        this.visitante = visitante;
        this.autorizante = autorizante;
        this.lote = lote;
        this.fechaAutorizada = fecha;
        this.horaDesde = desde;
        this.horaHasta = hasta;
    }

    public boolean esValida(LocalDateTime fechaHora) {
        return fechaHora != null && fechaAutorizada.equals(fechaHora.toLocalDate())
                && !fechaHora.toLocalTime().isBefore(horaDesde)
                && !fechaHora.toLocalTime().isAfter(horaHasta);
    }

    public boolean correspondeAVisitante(Visitante otro) {
        return otro != null && visitante.getDni() == otro.getDni();
    }

    public int getNumero() { return numero; }
    public Visitante getVisitante() { return visitante; }
    public Lote getLote() { return lote; }
    public Residente getAutorizante() { return autorizante; }
}
