package Ingresos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import Barrio.Lote;
import Persona.Residente;
import Persona.Visitante;
import Enums.DiaSemana;
import Excepciones.ReglaNegocioException;




public class AutorizacionRecurrente extends AutorizacionIngreso {
    public static final int LIMITE_DIAS = 30;

    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final Set<DiaSemana> diasPermitidos;

    public AutorizacionRecurrente(int numero, Visitante visitante, Lote lote, Residente autorizante, LocalDate fechaInicio, LocalDate fechaFin, Set<DiaSemana> diasPermitidos, LocalTime horaDesde, LocalTime horaHasta) {
        super(numero, visitante, autorizante, lote, fechaInicio, horaDesde, horaHasta);
        if (fechaFin == null || fechaFin.isBefore(fechaInicio)) {
            throw new ReglaNegocioException("La fecha fin no puede ser anterior a la fecha de inicio");
        }
        if (ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1 > LIMITE_DIAS) {
            throw new ReglaNegocioException("La autorización recurrente no puede superar los " + LIMITE_DIAS + " días");
        }
        if (diasPermitidos == null || diasPermitidos.isEmpty()) {
            throw new ReglaNegocioException("Debe indicar al menos un día de la semana permitido");
        }
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.diasPermitidos = EnumSet.copyOf(diasPermitidos);
    }

    @Override
    public boolean esValida(LocalDateTime fechaHora) {
        LocalDate dia = fechaHora.toLocalDate();
        return !dia.isBefore(fechaInicio) && !dia.isAfter(fechaFin)
                && diasPermitidos.contains(DiaSemana.desde(dia.getDayOfWeek()))
                && dentroDeFranja(fechaHora.toLocalTime());
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public Set<DiaSemana> getDiasPermitidos() { return Collections.unmodifiableSet(diasPermitidos); }
}


