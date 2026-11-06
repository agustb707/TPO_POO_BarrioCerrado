package Barrio;

import Persona.Titular;
import Persona.Residente;
import Expensas.Expensa;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Lote {
    private int numeroLote;
    private double superficie;
    private boolean moroso;
    private Titular titular;
    private List<Residente> residentes;
    private List<Residente> autorizadosIngreso;
    private List<Residente> autorizadosReclamos;
    private List<Expensa> expensas;

    public Lote(int numeroLote, double superficie) {
        if (numeroLote <= 0) throw new IllegalArgumentException("Numero de lote invalido.");
        if (!Double.isFinite(superficie) || superficie <= 0) {
            throw new IllegalArgumentException("La superficie debe ser positiva.");
        }
        this.numeroLote = numeroLote;
        this.superficie = superficie;
        residentes = new ArrayList<>();
        autorizadosIngreso = new ArrayList<>();
        autorizadosReclamos = new ArrayList<>();
        expensas = new ArrayList<>();
    }
    public void asignarTitular(Titular titular) {
        if (titular == null) throw new IllegalArgumentException("El titular es obligatorio.");
        this.titular = titular;
        autorizadosIngreso.clear();
        autorizadosReclamos.clear();
        titular.agregarLote(this);
    }
    public void agregarResidente(Residente residente) {
        if (residente == null) throw new IllegalArgumentException("El residente es obligatorio.");
        if (titular != null && titular.getDni() == residente.getDni()) {
            throw new IllegalArgumentException("El titular ya pertenece al lote.");
        }
        for (Residente existente : residentes) {
            if (existente.getDni() == residente.getDni()) {
                throw new IllegalArgumentException("Residente repetido en el lote.");
            }
        }
        residentes.add(residente);
        residente.agregarLote(this);
    }
    public void autorizarReclamos(Residente residente) {
        if (residente == null) throw new IllegalArgumentException("El residente es obligatorio.");
        for (Residente existente : residentes) {
            if (existente.getDni() == residente.getDni()) {
                if (puedePresentarReclamo(existente)) {
                    throw new IllegalArgumentException("El residente ya estaba autorizado para reclamos.");
                }
                autorizadosReclamos.add(existente);
                return;
            }
        }
        throw new IllegalArgumentException("El residente no pertenece al lote.");
    }
    public boolean puedePresentarReclamo(Residente residente) {
        if (residente == null || titular == null) return false;
        if (titular.getDni() == residente.getDni()) return true;
        for (Residente autorizado : autorizadosReclamos) {
            if (autorizado.getDni() == residente.getDni()) return true;
        }
        return false;
    }
    public void autorizarIngresos(Residente residente) {
        if (residente == null) throw new IllegalArgumentException("El residente es obligatorio.");
        for (Residente existente : residentes) {
            if (existente.getDni() == residente.getDni()) {
                if (puedeAutorizarIngreso(existente)) {
                    throw new IllegalArgumentException("El residente ya estaba autorizado para visitas.");
                }
                autorizadosIngreso.add(existente);
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
            throw new IllegalArgumentException("Superficie total invalida.");
        }
        return superficie / superficieTotal;
    }
    public void agregarExpensa(Expensa expensa) {
        if (expensa == null || expensa.getLote() != this) {
            throw new IllegalArgumentException("Expensa de otro lote.");
        }
        for (Expensa existente : expensas) {
            if (existente.getPeriodo().equals(expensa.getPeriodo())) {
                throw new IllegalArgumentException("Ya existe una expensa de ese periodo.");
            }
        }
        expensas.add(expensa);
        actualizarMorosidad();
    }
    public List<Expensa> obtenerExpensasPendientesOrdenadas() {
        List<Expensa> pendientes = new ArrayList<>();
        for (Expensa expensa : expensas) {
            if (expensa.calcularDeuda(LocalDate.now()) > 0.000001) pendientes.add(expensa);
        }
        pendientes.sort(Comparator.comparing(Expensa::getPeriodo));
        return pendientes;
    }
    public int contarPeriodosImpagos() {
        int cantidad = 0;
        for (Expensa expensa : expensas) {
            if (expensa.calcularDeuda(LocalDate.now()) > 0.000001) cantidad++;
        }
        return cantidad;
    }
    public void actualizarMorosidad() { moroso = contarPeriodosImpagos() > 3; }
    public List<Residente> getAutorizadosIngreso() { return new ArrayList<>(autorizadosIngreso); }
    public List<Residente> getAutorizadosReclamos() { return new ArrayList<>(autorizadosReclamos); }
    public int getNumeroLote() { return numeroLote; }
    public double getSuperficie() { return superficie; }
    public Titular getTitular() { return titular; }
    public boolean isMoroso() { return moroso; }
    public List<Residente> getResidentes() { return new ArrayList<>(residentes); }
    public List<Expensa> getExpensas() { return new ArrayList<>(expensas); }
}
