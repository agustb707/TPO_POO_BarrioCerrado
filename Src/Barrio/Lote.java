
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
        if (numeroLote <= 0) {
            throw new IllegalArgumentException("El numero debe ser positivo.");
        }

        if (!Double.isFinite(superficie) || superficie <= 0) {
            throw new IllegalArgumentException("La superficie debe ser mayor a cero.");
        }

        this.numeroLote = numeroLote;
        this.superficie = superficie;
        this.moroso = false;
        this.residentes = new ArrayList<>();
        this.autorizadosIngreso = new ArrayList<>();
        this.autorizadosReclamos = new ArrayList<>();
        this.expensas = new ArrayList<>();
    }

    // TITULAR Y RESIDENTES

    public void asignarTitular(Titular titular) {
        if (titular == null) {
            throw new IllegalArgumentException("El titular es obligatorio.");
        }

        this.titular = titular;
        autorizadosIngreso.clear();
        autorizadosReclamos.clear();
    }

    public void agregarResidente(Residente residente) {
        if (residente == null) {
            throw new IllegalArgumentException("El residente es obligatorio.");
        }

        if (titular != null && titular.getDni().equals(residente.getDni())) {
            throw new IllegalArgumentException("El titular ya pertenece al lote.");
        }

        for (Residente existente : residentes) {
            if (existente.getDni().equals(residente.getDni())) {
                throw new IllegalArgumentException("El residente ya pertenece al lote.");
            }
        }

        residentes.add(residente);
    }

    // AUTORIZACIONES PARA RECLAMOS

    public void autorizarReclamos(Residente residente) {
        if (residente == null) {
            throw new IllegalArgumentException("El residente es obligatorio.");
        }

        for (Residente existente : residentes) {
            if (existente.getDni().equals(residente.getDni())) {

                if (!puedePresentarReclamo(residente)) {
                    autorizadosReclamos.add(existente);
                }
                return;
            }
        }

        throw new IllegalArgumentException("El residente no pertenece al lote.");
    }

    public boolean puedePresentarReclamo(Residente residente) {
        if (residente == null || titular == null) {
            return false;
        }

        if (titular.getDni().equals(residente.getDni())) {
            return true;
        }

        for (Residente autorizado : autorizadosReclamos) {
            if (autorizado.getDni().equals(residente.getDni())) {
                return true;
            }
        }

        return false;
    }

    // AUTORIZACIONES PARA INGRESOS

    public void autorizarIngresos(Residente residente) {
        if (residente == null) {
            throw new IllegalArgumentException("El residente es obligatorio.");
        }

        for (Residente existente : residentes) {
            if (existente.getDni().equals(residente.getDni())) {

                if (!puedeAutorizarIngreso(residente)) {
                    autorizadosIngreso.add(existente);
                }
                return;
            }
        }

        throw new IllegalArgumentException("El residente no pertenece al lote.");
    }

    public boolean puedeAutorizarIngreso(Residente residente) {
        if (residente == null || titular == null) {
            return false;
        }

        if (titular.getDni().equals(residente.getDni())) {
            return true;
        }

        for (Residente autorizado : autorizadosIngreso) {
            if (autorizado.getDni().equals(residente.getDni())) {
                return true;
            }
        }

        return false;
    }

    // EXPENSAS

    public double calcularCoeficiente(double superficieTotal) {
        if (!Double.isFinite(superficieTotal) || superficieTotal < superficie) {
            throw new IllegalArgumentException("La superficie total es invalida.");
        }

        return superficie / superficieTotal;
    }

    public void agregarExpensa(Expensa expensa) {
        if (expensa == null) {
            throw new IllegalArgumentException("La expensa es obligatoria.");
        }

        if (expensa.getLote() != this) {
            throw new IllegalArgumentException("La expensa pertenece a otro lote.");
        }

        for (Expensa existente : expensas) {
            if (existente.getPeriodo().equals(expensa.getPeriodo())) {
                throw new IllegalArgumentException("Ya existe una expensa para ese periodo.");
            }
        }

        expensas.add(expensa);
        actualizarMorosidad();
    }

    public List<Expensa> obtenerExpensasPendientesOrdenadas() {
        List<Expensa> pendientes = new ArrayList<>();

        for (Expensa expensa : expensas) {
            if (expensa.calcularDeuda(LocalDate.now()) > 0) {
                pendientes.add(expensa);
            }
        }

        pendientes.sort(Comparator.comparing(Expensa::getPeriodo));

        return pendientes;
    }

    // MOROSIDAD

    public int contarPeriodosImpagos() {
        int cantidad = 0;

        for (Expensa expensa : expensas) {
            if (expensa.calcularDeuda(LocalDate.now()) > 0) {
                cantidad++;
            }
        }

        return cantidad;
    }

    public void actualizarMorosidad() {
        moroso = contarPeriodosImpagos() > 3;
    }

    // GETTERS

    public int getNumeroLote() {
        return numeroLote;
    }

    public double getSuperficie() {
        return superficie;
    }

    public Titular getTitular() {
        return titular;
    }

    public boolean isMoroso() {
        return moroso;
    }

    public List<Residente> getResidentes() {
        return new ArrayList<>(residentes);
    }

    public List<Expensa> getExpensas() {
        return new ArrayList<>(expensas);
    }
}
