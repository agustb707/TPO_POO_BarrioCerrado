package Expensas;

import Enums.MedioPago;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Pago {
    private int numero;
    private LocalDate fecha;
    private double importe;
    private MedioPago medioPago;
    private List<ImputacionPago> imputaciones;
    private List<AjustePago> ajustes;

    public Pago(int numero, LocalDate fecha, double importe, MedioPago medioPago) {
        if (numero <= 0 || fecha == null || medioPago == null ||
                !Double.isFinite(importe) || importe <= 0) {
            throw new IllegalArgumentException("Datos del pago invalidos.");
        }
        this.numero = numero;
        this.fecha = fecha;
        this.importe = importe;
        this.medioPago = medioPago;
        this.imputaciones = new ArrayList<>();
        this.ajustes = new ArrayList<>();
    }

    public void agregarImputacion(ImputacionPago imputacion) {
        if (imputacion == null) {
            throw new IllegalArgumentException("La imputacion es obligatoria.");
        }
        imputaciones.add(imputacion);
    }

    // Los ajustes se registran, nunca se borra el pago original.
    // El saldo de las expensas se actualiza desde GestorExpensas.
    public void registrarAjuste(AjustePago ajuste) {
        if (ajuste == null || ajuste.getFecha().isBefore(fecha)) {
            throw new IllegalArgumentException("Fecha del ajuste invalida.");
        }
        if (obtenerImporteNeto() + ajuste.getImporte() < -0.000001) {
            throw new IllegalArgumentException("El pago no puede tener importe neto negativo.");
        }
        ajustes.add(ajuste);
    }

    public double obtenerImporteNeto() {
        double total = importe;
        for (AjustePago ajuste : ajustes) {
            total += ajuste.getImporte();
        }
        return total;
    }

    public int getNumero() { return numero; }
    public LocalDate getFecha() { return fecha; }
    public double getImporte() { return importe; }
    public MedioPago getMedioPago() { return medioPago; }
    public List<ImputacionPago> getImputaciones() {
        return new ArrayList<>(imputaciones);
    }
    public List<AjustePago> getAjustes() {
        return new ArrayList<>(ajustes);
    }
}
