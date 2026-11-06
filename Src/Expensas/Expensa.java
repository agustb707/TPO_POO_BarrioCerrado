package Expensas;


import Barrio.Lote;
import Enums.EstadoExpensa;
import Excepciones.ReglaNegocioException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class Expensa {
    private int numero;
    private YearMonth periodo;
    private double capital;
    private LocalDate fechaVencimiento;
    private EstadoExpensa estado;
    private Lote lote;
    private List<Pago> pagos;

    // Para no volver a cobrar intereses de dias ya calculados.
    private double saldoCapital;
    private double interesPendiente;
    private LocalDate fechaUltimoInteres;
    private LocalDate fechaUltimaOperacion;

    public Expensa(int numero, YearMonth periodo, double capital, Lote lote) {
        if (numero <= 0 || periodo == null || lote == null ||
                !Double.isFinite(capital) || capital <= 0) {
            throw new IllegalArgumentException("Datos de la expensa invalidos.");
        }
        this.numero = numero;
        this.periodo = periodo;
        this.capital = capital;
        this.lote = lote;
        this.fechaVencimiento = periodo.atDay(10);
        this.estado = EstadoExpensa.PENDIENTE;
        this.saldoCapital = capital;
        this.interesPendiente = 0;
        this.fechaUltimoInteres = fechaVencimiento;
        this.pagos = new ArrayList<>();
    }

    // Guarda el pago asociado, sin volver a descontarlo.
    public void registrarPago(Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago es obligatorio.");
        }
        boolean imputado = false;
        for (ImputacionPago imputacion : pago.getImputaciones()) {
            if (imputacion.getExpensa() == this) {
                imputado = true;
            }
        }
        if (!imputado) {
            throw new ReglaNegocioException("El pago no tiene imputacion a esta expensa.");
        }
        if (!pagos.contains(pago)) {
            pagos.add(pago);
        }
        actualizarEstado(fechaUltimaOperacion == null ? pago.getFecha() : fechaUltimaOperacion);
    }

    public double calcularSaldoCapital() {
        return saldoCapital;
    }

    public double calcularInteresMora(LocalDate fechaPago) {
        if (fechaPago == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        double interes = interesPendiente;
        if (fechaPago.isAfter(fechaUltimoInteres) && saldoCapital > 0) {
            long dias = ChronoUnit.DAYS.between(fechaUltimoInteres, fechaPago);
            interes += saldoCapital * 0.03 * dias / 30.0;
        }
        return interes;
    }

    public double calcularDeuda(LocalDate fecha) {
        return calcularSaldoCapital() + calcularInteresMora(fecha);
    }

    private void calcularInteresHasta(LocalDate fecha) {
        interesPendiente = calcularInteresMora(fecha);
        if (fecha.isAfter(fechaUltimoInteres)) {
            fechaUltimoInteres = fecha;
        }
    }

    private void validarFechaOperacion(LocalDate fecha) {
        if (fecha == null || (fechaUltimaOperacion != null &&
                fecha.isBefore(fechaUltimaOperacion))) {
            throw new ReglaNegocioException("La operacion no puede ser anterior a otra ya registrada.");
        }
    }

    // Imputa primero intereses y despues capital. Devuelve el importe aplicado.
    public double imputarPago(double importe, LocalDate fecha) {
        if (!Double.isFinite(importe) || importe <= 0) {
            throw new IllegalArgumentException("El importe debe ser positivo.");
        }
        validarFechaOperacion(fecha);
        calcularInteresHasta(fecha);
        double aplicado = Math.min(importe, saldoCapital + interesPendiente);
        double aInteres = Math.min(aplicado, interesPendiente);
        interesPendiente -= aInteres;
        saldoCapital -= aplicado - aInteres;
        if (Math.abs(saldoCapital) < 0.000001) saldoCapital = 0;
        if (Math.abs(interesPendiente) < 0.000001) interesPendiente = 0;
        fechaUltimaOperacion = fecha;
        actualizarEstado(fecha);
        return aplicado;
    }

    // Una correccion negativa devuelve deuda, sin borrar el pago original.
    void revertirPago(double interes, double capitalDevuelto, LocalDate fecha) {
        validarFechaOperacion(fecha);
        if (interes < 0 || capitalDevuelto < 0) {
            throw new IllegalArgumentException("Importes de reversion invalidos.");
        }
        calcularInteresHasta(fecha);
        saldoCapital += capitalDevuelto;
        interesPendiente += interes;
        fechaUltimaOperacion = fecha;
        actualizarEstado(fecha);
    }

    public void actualizarEstado(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (calcularDeuda(fecha) < 0.000001) {
            estado = EstadoExpensa.PAGADA;
        } else if (fecha.isAfter(fechaVencimiento)) {
            estado = EstadoExpensa.VENCIDA;
        } else if (!pagos.isEmpty() || saldoCapital < capital) {
            estado = EstadoExpensa.PARCIALMENTE_PAGADA;
        } else {
            estado = EstadoExpensa.PENDIENTE;
        }
    }

    public void restaurarDatos(EstadoExpensa estado, double saldoCapital,
                               double interesPendiente, LocalDate fechaUltimoInteres, LocalDate fechaUltimaOperacion) {
        this.estado = estado;
        this.saldoCapital = saldoCapital;
        this.interesPendiente = interesPendiente;
        this.fechaUltimoInteres = fechaUltimoInteres;
        this.fechaUltimaOperacion = fechaUltimaOperacion;
    }
    public void agregarPagoRestaurado(Pago pago) {
        if (!pagos.contains(pago)) pagos.add(pago);
    }
    public double getInteresPendiente() { return interesPendiente; }
    public LocalDate getFechaUltimoInteres() { return fechaUltimoInteres; }
    public int getNumero() { return numero; }
    public YearMonth getPeriodo() { return periodo; }
    public double getCapital() { return capital; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public EstadoExpensa getEstado() { return estado; }
    public Lote getLote() { return lote; }
    public List<Pago> getPagos() { return new ArrayList<>(pagos); }
    public LocalDate getFechaUltimaOperacion() { return fechaUltimaOperacion; }
}
