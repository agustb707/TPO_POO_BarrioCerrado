
package Equipos;

import java.time.Duration;
import java.time.LocalDateTime;

import Excepciones.ReglaNegocioException;
import Persona.Persona;
import Persona.Residente;
import Persona.Guardia;
import Enums.EstadoEquipo;

public class Falla {

    private static int secuencia = 0;
    public static final long HORAS_AVISO_ADMINISTRADOR = 48;

    private final int numero;
    private final LocalDateTime fechaRegistro;
    private final String descripcion;
    private final Equipo equipo;
    private final Persona reportante;
    private OrdenMantenimiento orden;

    public Falla(Equipo equipo, Persona reportante, String descripcion) {

        if (equipo == null) {
            throw new IllegalArgumentException("El equipo es obligatorio.");
        }

        if (!(reportante instanceof Residente) && !(reportante instanceof Guardia)) {
            throw new IllegalArgumentException("Solo un residente o guardia puede registrar fallas.");
        }

        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripcion de la falla es obligatoria.");
        }

        this.numero = ++secuencia;
        this.fechaRegistro = LocalDateTime.now();
        this.equipo = equipo;
        this.reportante = reportante;
        this.descripcion = descripcion;
    }

    // ORDEN DE MANTENIMIENTO

    public void asociarOrden(OrdenMantenimiento orden) {

        if (orden == null) {
            throw new IllegalArgumentException("La orden es obligatoria.");
        }

        if (this.orden != null) {
            throw new ReglaNegocioException("La falla ya tiene una orden de mantenimiento.");
        }

        if (orden.getEquipo() != equipo) {
            throw new ReglaNegocioException("La orden pertenece a otro equipo.");
        }

        this.orden = orden;
        equipo.cambiarEstado(EstadoEquipo.EN_REPARACION);
    }

    // AVISO AL ADMINISTRADOR

    public boolean requiereAvisoAdministrador(LocalDateTime fechaActual) {

        if (fechaActual == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        return equipo instanceof Camara
                && equipo.getEstado() != EstadoEquipo.OPERATIVO
                && estaActiva()
                && Duration.between(fechaRegistro, fechaActual)
                .compareTo(Duration.ofHours(HORAS_AVISO_ADMINISTRADOR)) > 0;
    }

    // ESTADO DE LA FALLA

    public boolean estaActiva() {
        return orden == null || orden.estaAbierta();
    }

    // GETTERS

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
