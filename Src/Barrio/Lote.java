package Barrio;

import Persona.Titular;
import Persona.Residente;
import java.util.ArrayList;
import java.util.List;

public class Lote {
    private int numeroLote;
    private double superficie;
    private boolean moroso;
    private Titular titular;
    private List<Residente> residentes = new ArrayList<>();
    private List<Residente> autorizadosIngreso = new ArrayList<>();

    public Lote(int numeroLote, double superficie) {
        if (numeroLote <= 0) throw new IllegalArgumentException("El numero debe ser positivo.");
        if (!Double.isFinite(superficie) || superficie <= 0) {
            throw new IllegalArgumentException("La superficie debe ser mayor a cero.");
        }
        this.numeroLote = numeroLote;
        this.superficie = superficie;
    }

    public void asignarTitular(Titular titular) {
        if (titular == null) throw new IllegalArgumentException("El titular es obligatorio.");
        this.titular = titular;
        autorizadosIngreso.clear();
    }

    public void agregarResidente(Residente residente) {
        if (residente == null) throw new IllegalArgumentException("El residente es obligatorio.");
        for (Residente existente : residentes) {
            if (existente.getDni() == residente.getDni()) {
                throw new IllegalArgumentException("El residente ya pertenece al lote.");
            }
        }
        residentes.add(residente);
    }

    public void autorizarIngresos(Residente residente) {
        if (residente == null) throw new IllegalArgumentException("El residente es obligatorio.");
        for (Residente existente : residentes) {
            if (existente.getDni() == residente.getDni()) {
                if (!puedeAutorizarIngreso(existente)) autorizadosIngreso.add(existente);
                return;
            }
        }
        throw new IllegalArgumentException("El residente no pertenece al lote.");
    }

    public boolean puedeAutorizarIngreso(Residente residente) {
        if (residente == null || titular == null) return false;
        if (titular.getDni() == residente.getDni()) return true;
        for (Residente autorizado : autorizadosIngreso) {
            if (autorizado.getDni() == residente.getDni()) return true;
        }
        return false;
    }

    public double calcularCoeficiente(double superficieTotal) {
        if (!Double.isFinite(superficieTotal) || superficieTotal < superficie) {
            throw new IllegalArgumentException("La superficie total es invalida.");
        }
        return superficie / superficieTotal;
    }

    public int getNumeroLote() { return numeroLote; }
    public double getSuperficie() { return superficie; }
    public Titular getTitular() { return titular; }
    public boolean isMoroso() { return moroso; }
    public List<Residente> getResidentes() { return new ArrayList<>(residentes); }
}
