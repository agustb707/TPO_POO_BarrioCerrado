package Equipos;

import java.time.Duration;
import java.time.LocalDateTime;
import Excepciones.ReglaNegocioException;
import Persona.Persona;
import Enums.EstadoEquipo;

public class Falla {
    private static int secuencia = 0;
    public static final long HORAS_AVISO_ADMINISTRADOR = 24;

    private final int numero;
    private final LocalDateTime fechaRegistro;
    private final String descripcion;
    private final Equipo equipo;
    private final Persona reportante;
    private OrdenMantenimiento orden;

    public Falla(Equipo equipo, Persona reportante, String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción de la falla es obligatoria");
        }
        this.numero = ++secuencia;
        this.fechaRegistro = LocalDateTime.now();
        this.equipo = equipo;
        this.reportante = reportante;
        this.descripcion = descripcion;
    }

    public void asociarOrden(OrdenMantenimiento orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La orden es obligatoria");
        }
        if (this.orden != null && this.orden.estaAbierta()) {
            throw new ReglaNegocioException("La falla ya tiene una orden de mantenimiento abierta");
        }
        this.orden = orden;
        equipo.cambiarEstado(EstadoEquipo.EN_REPARACION);
    }

    /** Hay que avisar al administrador si pasaron 24 hs o más sin orden asociada. */
    public boolean requiereAvisoAdministrador(LocalDateTime fechaActual) {
        return orden == null
                && Duration.between(fechaRegistro, fechaActual).toHours() >= HORAS_AVISO_ADMINISTRADOR;
    }

    /** Una falla está activa mientras no tenga una orden ya cerrada. */
    public boolean estaActiva() {
        return orden == null || orden.estaAbierta();
    }

    public int getNumero() { 
        return numero;
     }
    public LocalDateTime getFechaRegistro() { 
        return fechaRegistro; 
    }
    public String getDescripcion() { 
        return descripcion; 
    }
    public Equipo getEquipo() {
        return equipo; 
    }
    public Persona getReportante() { 
        return reportante; 
    }
    public OrdenMantenimiento getOrden() { 
        return orden; 
    }
}
