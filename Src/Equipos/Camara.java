package Equipos;

import Enums.Prioridad;
import Enums.Resolucion;
import Enums.Sector;
import java.time.LocalDate;

public class Camara extends Equipo {
    private Resolucion resolucion;
    private int cantidadDiasGrabacion;

    public Camara(String codigo, Sector sector, LocalDate fechaInstalacion,
                  Resolucion resolucion, int cantidadDiasGrabacion) {
        super(codigo, sector, fechaInstalacion);
        if (resolucion == null || cantidadDiasGrabacion <= 0) {
            throw new IllegalArgumentException("Datos de camara invalidos.");
        }
        this.resolucion = resolucion;
        this.cantidadDiasGrabacion = cantidadDiasGrabacion;
    }

    @Override
    public Prioridad determinarPrioridadFalla(int cantidadFallasSector) {
        return Prioridad.ALTA;
    }

    public Resolucion getResolucion() {
        return resolucion;
    }

    public int getCantidadDiasGrabacion() {
        return cantidadDiasGrabacion;
    }
}
