package Reclamos;


import Enums.EstadoReclamo;
import java.time.LocalDateTime;

public class HistorialEstado {
    private LocalDateTime fechaCambio;
    private EstadoReclamo estadoNuevo;
    private String observacion;

    public HistorialEstado(LocalDateTime fechaCambio, EstadoReclamo estadoNuevo,
                           String observacion) {
        if (fechaCambio == null || estadoNuevo == null || observacion == null) {
            throw new IllegalArgumentException("Datos del historial invalidos.");
        }
        this.fechaCambio = fechaCambio;
        this.estadoNuevo = estadoNuevo;
        this.observacion = observacion;
    }

    public LocalDateTime getFechaCambio() { return fechaCambio; }
    public EstadoReclamo getEstadoNuevo() { return estadoNuevo; }
    public String getObservacion() { return observacion; }
}
