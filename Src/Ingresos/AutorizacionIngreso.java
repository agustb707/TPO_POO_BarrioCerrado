package Ingresos;

import java.time.LocalDate;
import java.time.LocalTime;

import Persona.Residente;

//solid lsp, polimorfismo
public class AutorizacionIngreso {
    private final int numero;
    private final LocalDate fechaAutorizada;
    private final LocalTime horaDesde;
    private final LocalTime horaHasta;
    private final Visitante visitante;
    private final Lote lote;
    private final Residente autorizante;
}
