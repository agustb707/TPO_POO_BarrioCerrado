package Equipos;

import java.time.Duration;
import java.time.LocalDateTime;
import Excepciones.ReglaNegocioException;
import Persona.Persona;
import Persona.Residente;
import Persona.Guardia;
import Enums.EstadoEquipo;
import java.util.List;

public class Falla {
    private static int secuencia = 0;
    public static final long HORAS_AVISO_ADMINISTRADOR = 48;
    private int numero;
    private LocalDateTime fechaRegistro;
    private final String descripcion;
    private final Equipo equipo;
    private final Persona reportante;
    private OrdenMantenimiento orden;

    public Falla(Equipo equipo, Persona reportante, String descripcion) {
        if (equipo == null) throw new IllegalArgumentException("El equipo es obligatorio.");
        if (!(reportante instanceof Residente) && !(reportante instanceof Guardia)) {
            throw new IllegalArgumentException("Solo un residente o guardia puede registrar fallas.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripcion de la falla es obligatoria.");
        }
        numero = ++secuencia;
        fechaRegistro = LocalDateTime.now();
        this.equipo = equipo;
        this.reportante = reportante;
        this.descripcion = descripcion;
    }
    public void asociarOrden(OrdenMantenimiento orden) {
        if (orden == null) throw new IllegalArgumentException("La orden es obligatoria.");
        if (this.orden != null) throw new ReglaNegocioException("La falla ya tiene orden.");
        if (orden.getEquipo() != equipo) throw new ReglaNegocioException("Orden de otro equipo.");
        this.orden = orden;
        equipo.cambiarEstado(EstadoEquipo.EN_REPARACION);
    }
    public boolean requiereAvisoAdministrador(LocalDateTime fechaActual) {
        if (fechaActual == null) throw new IllegalArgumentException("La fecha es obligatoria.");
        return equipo instanceof Camara && equipo.getEstado() != EstadoEquipo.OPERATIVO
                && estaActiva() && Duration.between(fechaRegistro, fechaActual)
                .compareTo(Duration.ofHours(HORAS_AVISO_ADMINISTRADOR)) > 0;
    }
    public boolean estaActiva() { return orden == null || orden.estaAbierta(); }
    // Al recuperar el archivo, los contadores static se reconstruyen.
    public static void restaurarSecuencia(List<Equipo> equipos) {
        secuencia = 0;
        for (Equipo equipo : equipos) {
            for (Falla falla : equipo.getFallas()) {
                if (falla.getNumero() > secuencia) secuencia = falla.getNumero();
            }
        }
    }
    public void restaurarDatos(int numero, LocalDateTime fechaRegistro) {
        this.numero = numero;
        this.fechaRegistro = fechaRegistro;
    }
    public int getNumero() { return numero; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public String getDescripcion() { return descripcion; }
    public Equipo getEquipo() { return equipo; }
    public Persona getReportante() { return reportante; }
    public OrdenMantenimiento getOrden() { return orden; }
}
