
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

    public Equipo(String codigo, Sector sector, LocalDate fechaInstalacion) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo es obligatorio.");
        }

        if (sector == null || fechaInstalacion == null) {
            throw new IllegalArgumentException("El sector y la fecha son obligatorios.");
        }

        this.codigo = codigo;
        this.sector = sector;
        this.fechaInstalacion = fechaInstalacion;
        this.estado = EstadoEquipo.OPERATIVO;
        this.fallas = new ArrayList<>();
        this.ordenes = new ArrayList<>();
    }

    // FALLAS

    public void registrarFalla(Falla falla) {
        if (falla == null) {
            throw new IllegalArgumentException("La falla es obligatoria.");
        }

        if (falla.getEquipo() != this) {
            throw new ReglaNegocioException("La falla pertenece a otro equipo.");
        }

        if (fallas.contains(falla)) {
            throw new IllegalArgumentException("La falla ya esta registrada.");
        }

        fallas.add(falla);

        if (estado == EstadoEquipo.OPERATIVO) {
            estado = EstadoEquipo.CON_FALLA;
        }
    }

    // ORDENES DE MANTENIMIENTO

    public void registrarOrden(OrdenMantenimiento orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La orden es obligatoria.");
        }

        if (orden.getEquipo() != this) {
            throw new ReglaNegocioException("La orden pertenece a otro equipo.");
        }

        if (ordenes.contains(orden)) {
            throw new IllegalArgumentException("La orden ya esta registrada.");
        }

        ordenes.add(orden);
    }

    // ESTADO DEL EQUIPO

    public void cambiarEstado(EstadoEquipo nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }

        this.estado = nuevoEstado;
    }

    public boolean estaFueraDeServicio() {
        return estado == EstadoEquipo.FUERA_DE_SERVICIO;
    }

    // POLIMORFISMO
    // Cada tipo de equipo determina la prioridad de sus fallas

    public abstract Prioridad determinarPrioridadFalla(int cantidadFallasSector);

    // GETTERS

    public String getCodigo() {
        return codigo;
    }

    public Sector getSector() {
        return sector;
    }

    public LocalDate getFechaInstalacion() {
        return fechaInstalacion;
    }

    public EstadoEquipo getEstado() {
        return estado;
    }

    public List<Falla> getFallas() {
        return new ArrayList<>(fallas);
    }

    public List<OrdenMantenimiento> getOrdenes() {
        return new ArrayList<>(ordenes);
    }

    @Override
    public String toString() {
        return "Equipo: " + codigo +
                " | Sector: " + sector +
                " | Estado: " + estado;
    }
}
