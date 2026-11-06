package Ingresos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import Barrio.Lote;
import Persona.Residente;
import Persona.Visitante;
import Persona.Proveedor;
import Enums.DiaSemana;
import Excepciones.ReglaNegocioException;

public class AutorizacionRecurrente extends AutorizacionIngreso {
    public static final int LIMITE_DIAS = 30;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final Set<DiaSemana> diasPermitidos;

    public AutorizacionRecurrente(int numero, Visitante visitante, Lote lote,
                                  Residente autorizante, LocalDate fechaInicio,
                                  LocalDate fechaFin, Set<DiaSemana> diasPermitidos,
                                  LocalTime horaDesde, LocalTime horaHasta) {
        super(numero, visitante, autorizante, lote, fechaInicio, horaDesde, horaHasta);
        if (!(visitante instanceof Proveedor)) {
            throw new ReglaNegocioException("Solo proveedores tienen autorizaciones recurrentes.");
        }
        if (fechaFin == null || fechaFin.isBefore(fechaInicio)
                || fechaFin.isAfter(fechaInicio.plusDays(LIMITE_DIAS - 1))) {
            throw new ReglaNegocioException("La autorizacion no puede superar los 30 dias.");
        }
        if (diasPermitidos == null || diasPermitidos.isEmpty()) {
            throw new ReglaNegocioException("Debe indicar dias permitidos.");
        }
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.diasPermitidos = new HashSet<>(diasPermitidos);
    }
    @Override public boolean esValida(LocalDateTime fechaHora) {
        if (fechaHora == null) return false;
        LocalDate fecha = fechaHora.toLocalDate();
        return !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin)
                && diasPermitidos.contains(DiaSemana.desde(fecha.getDayOfWeek()))
                && dentroDeFranja(fechaHora.toLocalTime());
    }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public Set<DiaSemana> getDiasPermitidos() { return new HashSet<>(diasPermitidos); }
}
