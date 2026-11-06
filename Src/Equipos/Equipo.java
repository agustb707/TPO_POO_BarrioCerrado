package Equipos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import Enums.EstadoEquipo;
import Enums.Sector;
import Enums.Prioridad;
import Excepciones.ReglaNegocioException;

public abstract class Equipo {
    private String codigo;
    private Sector sector;
    private LocalDate fechaInstalacion;
    private EstadoEquipo estado;
    private List<Falla> fallas;
    private List<OrdenMantenimiento> ordenes;

    protected Equipo(String codigo, Sector sector, LocalDate fechaInstalacion) {
        if (codigo == null || codigo.isBlank() || sector == null || fechaInstalacion == null) {
            throw new IllegalArgumentException("Datos del equipo invalidos.");
        }
        this.codigo = codigo;
        this.sector = sector;
        this.fechaInstalacion = fechaInstalacion;
        estado = EstadoEquipo.OPERATIVO;
        fallas = new ArrayList<>();
        ordenes = new ArrayList<>();
    }
    public void registrarFalla(Falla falla) {
        if (falla == null || falla.getEquipo() != this) {
            throw new ReglaNegocioException("La falla pertenece a otro equipo.");
        }
        if (fallas.contains(falla)) throw new ReglaNegocioException("La falla ya esta registrada.");
        fallas.add(falla);
        if (estado == EstadoEquipo.OPERATIVO) estado = EstadoEquipo.CON_FALLA;
    }
    public void registrarOrden(OrdenMantenimiento orden) {
        if (orden == null || orden.getEquipo() != this) {
            throw new ReglaNegocioException("La orden pertenece a otro equipo.");
        }
        if (ordenes.contains(orden)) throw new ReglaNegocioException("La orden ya esta registrada.");
        ordenes.add(orden);
    }
    public void cambiarEstado(EstadoEquipo nuevoEstado) {
        if (nuevoEstado == null) throw new IllegalArgumentException("El estado es obligatorio.");
        estado = nuevoEstado;
    }
    public abstract Prioridad determinarPrioridadFalla(int cantidadFallasSector);
    public boolean estaFueraDeServicio() { return estado == EstadoEquipo.FUERA_DE_SERVICIO; }
    public String getCodigo() { return codigo; }
    public Sector getSector() { return sector; }
    public LocalDate getFechaInstalacion() { return fechaInstalacion; }
    public EstadoEquipo getEstado() { return estado; }
    public List<Falla> getFallas() { return new ArrayList<>(fallas); }
    public List<OrdenMantenimiento> getOrdenes() { return new ArrayList<>(ordenes); }
    @Override public String toString() {
        return "Equipo: " + codigo + " | Sector: " + sector + " | Estado: " + estado;
    }
}
