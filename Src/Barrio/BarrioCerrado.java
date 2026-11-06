package Barrio;

import Enums.EstadoEquipo;
import Enums.Sector;
import Equipos.Equipo;
import Equipos.Luminaria;
import Persona.Administrador;
import Persona.Personal;
import Reclamos.Reclamo;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarrioCerrado {
    private String nombre;
    private List<Lote> lotes;
    private List<Personal> personal;
    private List<Reclamo> reclamos;
    private List<Equipo> equipos;
    private Administrador administrador;
    private Map<YearMonth, Double> presupuestoMensual;

    public BarrioCerrado(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        this.nombre = nombre;
        lotes = new ArrayList<>();
        personal = new ArrayList<>();
        reclamos = new ArrayList<>();
        equipos = new ArrayList<>();
        presupuestoMensual = new HashMap<>();
    }

    public void agregarLote(Lote lote) {
        if (lote == null) throw new IllegalArgumentException("El lote es obligatorio.");
        if (lotes.size() >= 150) throw new IllegalArgumentException("El barrio admite 150 lotes.");
        if (buscarLote(lote.getNumeroLote()) != null) {
            throw new IllegalArgumentException("Ya existe un lote con ese numero.");
        }
        lotes.add(lote);
    }
    public Lote buscarLote(int numero) {
        for (Lote lote : lotes) if (lote.getNumeroLote() == numero) return lote;
        return null;
    }
    public double calcularSuperficieTotal() {
        double total = 0;
        for (Lote lote : lotes) total += lote.getSuperficie();
        return total;
    }
    public void agregarPersonal(Personal empleado) {
        if (empleado == null) throw new IllegalArgumentException("El personal es obligatorio.");
        for (Personal existente : personal) {
            if (existente.getLegajo() == empleado.getLegajo()) {
                throw new IllegalArgumentException("Ya existe ese legajo.");
            }
        }
        personal.add(empleado);
    }
    public void asignarAdministrador(Administrador administrador) {
        if (administrador == null) throw new IllegalArgumentException("El administrador es obligatorio.");
        this.administrador = administrador;
        administrador.setBarrio(this);
    }
    public void agregarReclamo(Reclamo reclamo) {
        if (reclamo == null) throw new IllegalArgumentException("El reclamo es obligatorio.");
        for (Reclamo existente : reclamos) {
            if (existente.getNumero() == reclamo.getNumero()) {
                throw new IllegalArgumentException("Ya existe ese reclamo.");
            }
        }
        reclamos.add(reclamo);
    }
    public void registrarEquipo(Equipo equipo) {
        if (equipo == null) throw new IllegalArgumentException("El equipo es obligatorio.");
        for (Equipo existente : equipos) {
            if (existente.getCodigo().equals(equipo.getCodigo())) {
                throw new IllegalArgumentException("Ya existe ese equipo.");
            }
        }
        equipos.add(equipo);
    }
    public int contarFallasSector(Sector sector) {
        int cantidad = 0;
        for (Equipo equipo : equipos) {
            if (equipo instanceof Luminaria && equipo.getSector() == sector
                    && equipo.getEstado() != EstadoEquipo.OPERATIVO) cantidad++;
        }
        return cantidad;
    }
    public void registrarPresupuesto(YearMonth periodo, double importe) {
        if (periodo == null || !Double.isFinite(importe) || importe <= 0) {
            throw new IllegalArgumentException("Presupuesto invalido.");
        }
        presupuestoMensual.put(periodo, importe);
    }
    public double obtenerPresupuesto(YearMonth periodo) {
        if (!presupuestoMensual.containsKey(periodo)) {
            throw new IllegalArgumentException("No existe presupuesto para ese periodo.");
        }
        return presupuestoMensual.get(periodo);
    }
    public Map<YearMonth, Double> getPresupuestos() { return new HashMap<>(presupuestoMensual); }
    public String getNombre() { return nombre; }
    public List<Lote> getLotes() { return new ArrayList<>(lotes); }
    public List<Personal> getPersonal() { return new ArrayList<>(personal); }
    public List<Reclamo> getReclamos() { return new ArrayList<>(reclamos); }
    public List<Equipo> getEquipos() { return new ArrayList<>(equipos); }
    public Administrador getAdministrador() { return administrador; }
}
