package Equipos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;  
import Enums.EstadoEquipo;
import Excepciones.ReglaNegocioException;
import Enums.Sector;
import Enums.Prioridad;

//Experto en información (GRASP) sobre un equipo del barrio.
//OCP + Polimorfismo: cada subtipo define su regla de prioridad sin modificar el comportamiento general.

public abstract class Equipo {
    private final String codigo;
    private final Sector sector;
    private final LocalDate fechaInstalacion;
    private EstadoEquipo estado = EstadoEquipo.OPERATIVO;
    private final List<Falla> fallas = new ArrayList<>();

    protected Equipo(String codigo, Sector sector, LocalDate fechaInstalacion) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del equipo es obligatorio");
        }
        this.codigo = codigo;
        this.sector = sector;
        this.fechaInstalacion = fechaInstalacion ;
    }

    public void registrarFalla(Falla falla) {
        if (falla == null) {
            throw new IllegalArgumentException("La falla es obligatoria");
        }
        if (falla.getEquipo() != this) {
            throw new ReglaNegocioException("La falla pertenece a otro equipo");
        }
        fallas.add(falla);
        if (estado == EstadoEquipo.OPERATIVO) {
            estado = EstadoEquipo.CON_FALLA;
        }
    }

    public void cambiarEstado(EstadoEquipo nuevoEstado) {
        this.estado = nuevoEstado;
    }

    // Cada subtipo define cómo prioriza una falla según la cantidad de fallas activas en su sector
    public abstract Prioridad determinarPrioridadFalla(int cantidadFallasSector);

    public boolean estaFueraDeServicio() {
        return estado == EstadoEquipo.FUERA_DE_SERVICIO;
    }

    public String getCodigo() { return codigo; }
    public Sector getSector() { return sector; }
    public LocalDate getFechaInstalacion() { return fechaInstalacion; }
    public EstadoEquipo getEstado() { return estado; }
    public List<Falla> getFallas() { return Collections.unmodifiableList(fallas); }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + codigo + ", " + sector + ", " + estado + "]";
    }
}

