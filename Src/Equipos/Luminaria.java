package Equipos;

import Enums.Prioridad;
import Enums.Sector;
import Enums.TipoLuminaria;
import java.time.LocalDate;

public class Luminaria extends Equipo {
    private TipoLuminaria tipoLampara;
    private double potencia;

    public Luminaria(String codigo, Sector sector, LocalDate fechaInstalacion,
                     TipoLuminaria tipoLampara, double potencia) {
        super(codigo, sector, fechaInstalacion);
        if (tipoLampara == null || !Double.isFinite(potencia) || potencia <= 0) {
            throw new IllegalArgumentException("Datos de luminaria invalidos.");
        }
        this.tipoLampara = tipoLampara;
        this.potencia = potencia;
    }

    @Override
    public Prioridad determinarPrioridadFalla(int cantidadFallasSector) {
        if (cantidadFallasSector > 3) {
            return Prioridad.ALTA;
        }
        return Prioridad.MEDIA;
    }

    public TipoLuminaria getTipoLampara() {
        return tipoLampara;
    }

    public double getPotencia() {
        return potencia;
    }
}