package Expensas;


import Barrio.BarrioCerrado;
import Barrio.Lote;
import Enums.MedioPago;
import Enums.TipoNotificacion;
import Excepciones.ReglaNegocioException;
import Notificaciones.Notificacion;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

// GRASP Controlador: coordina pagos; los calculos viven en Expensa y Lote.
public class GestorExpensas {
    private BarrioCerrado barrio;
    private List<Pago> pagos;
    private List<Notificacion> avisos;

    public GestorExpensas(BarrioCerrado barrio) {
        if (barrio == null) {
            throw new IllegalArgumentException("El barrio es obligatorio.");
        }
        this.barrio = barrio;
        this.pagos = new ArrayList<>();
        this.avisos = new ArrayList<>();
    }

    public List<Expensa> emitirExpensas(YearMonth periodo) {
        if (periodo == null) {
            throw new IllegalArgumentException("El periodo es obligatorio.");
        }
        double presupuesto = barrio.obtenerPresupuesto(periodo);
        if (!Double.isFinite(presupuesto) || presupuesto <= 0) {
            throw new ReglaNegocioException("El presupuesto debe ser positivo.");
        }
        List<Lote> lotes = barrio.getLotes();
        double superficieTotal = barrio.calcularSuperficieTotal();
        if (superficieTotal <= 0) {
            throw new ReglaNegocioException("No hay lotes para emitir expensas.");
        }
        // Validar todo antes de empezar a crear expensas.
        for (Lote lote : lotes) {
            if (lote.getTitular() == null) {
                throw new ReglaNegocioException("El lote " + lote.getNumeroLote() + " no tiene titular.");
            }
            for (Expensa expensa : lote.getExpensas()) {
                if (expensa.getPeriodo().equals(periodo)) {
                    throw new ReglaNegocioException("Las expensas del periodo ya fueron emitidas.");
                }
            }
        }
        int siguienteNumero = 1;
        for (Lote lote : lotes) {
            for (Expensa expensa : lote.getExpensas()) {
                if (expensa.getNumero() >= siguienteNumero) {
                    siguienteNumero = expensa.getNumero() + 1;
                }
            }
        }
        List<Expensa> nuevas = new ArrayList<>();
        for (Lote lote : lotes) {
            double importe = presupuesto * lote.calcularCoeficiente(superficieTotal);
            Expensa expensa = new Expensa(siguienteNumero++, periodo, importe, lote);
            lote.agregarExpensa(expensa);
            nuevas.add(expensa);
        }
        return nuevas;
    }

    private void verificarLote(Lote lote) {
        if (lote == null || barrio.buscarLote(lote.getNumeroLote()) != lote) {
            throw new ReglaNegocioException("El lote no pertenece al barrio.");
        }
    }

    private double deudaTotal(Lote lote, LocalDate fecha) {
        double deuda = 0;
        for (Expensa expensa : lote.getExpensas()) {
            deuda += expensa.calcularDeuda(fecha);
        }
        return deuda;
    }

    private void validarFechas(Lote lote, LocalDate fecha) {
        for (Expensa expensa : lote.getExpensas()) {
            if (expensa.getFechaUltimaOperacion() != null &&
                    fecha.isBefore(expensa.getFechaUltimaOperacion())) {
                throw new ReglaNegocioException("La fecha es anterior a un movimiento registrado.");
            }
        }
    }

    public Pago registrarPago(Lote lote, LocalDate fecha,
                              double importe, MedioPago medio) {
        verificarLote(lote);
        if (fecha == null || medio == null || !Double.isFinite(importe) || importe <= 0) {
            throw new IllegalArgumentException("Datos del pago invalidos.");
        }
        validarFechas(lote, fecha);
        double deuda = deudaTotal(lote, fecha);
        if (deuda < 0.000001 || importe > deuda + 0.000001) {
            throw new ReglaNegocioException("No existe deuda suficiente para imputar el pago.");
        }
        int numero = 1;
        for (Pago existente : pagos) {
            if (existente.getNumero() >= numero) numero = existente.getNumero() + 1;
        }
        Pago pago = new Pago(numero, fecha, importe, medio);
        imputarEnLote(pago, lote, fecha, importe);
        pagos.add(pago);
        lote.actualizarMorosidad();
        return pago;
    }

    private void imputarEnLote(Pago pago, Lote lote, LocalDate fecha,
                               double importe) {
        double restante = importe;
        for (Expensa expensa : lote.obtenerExpensasPendientesOrdenadas()) {
            if (restante <= 0.000001) break;
            double interesAntes = expensa.calcularInteresMora(fecha);
            double capitalAntes = expensa.calcularSaldoCapital();
            double aplicado = expensa.imputarPago(restante, fecha);
            if (aplicado > 0.000001) {
                double aInteres = interesAntes - expensa.calcularInteresMora(fecha);
                double aCapital = capitalAntes - expensa.calcularSaldoCapital();
                ImputacionPago imputacion = new ImputacionPago(expensa, aInteres, aCapital);
                pago.agregarImputacion(imputacion);
                expensa.registrarPago(pago);
                restante -= aplicado;
            }
        }
        if (restante > 0.000001) {
            throw new ReglaNegocioException("No pudo imputarse todo el importe.");
        }
    }

    public void registrarAjuste(Pago pago, AjustePago ajuste) {
        if (pago == null || !pagos.contains(pago) || ajuste == null) {
            throw new IllegalArgumentException("Pago o ajuste no registrado.");
        }
        if (ajuste.getFecha().isBefore(pago.getFecha()) ||
                pago.obtenerImporteNeto() + ajuste.getImporte() < -0.000001) {
            throw new ReglaNegocioException("El ajuste excede el pago o tiene fecha invalida.");
        }
        List<ImputacionPago> imputaciones = pago.getImputaciones();
        if (imputaciones.isEmpty()) {
            throw new ReglaNegocioException("El pago no tiene imputaciones.");
        }
        Lote lote = imputaciones.get(0).getExpensa().getLote();
        validarFechas(lote, ajuste.getFecha());

        if (ajuste.getImporte() > 0) {
            if (ajuste.getImporte() > deudaTotal(lote, ajuste.getFecha()) + 0.000001) {
                throw new ReglaNegocioException("El ajuste positivo supera la deuda.");
            }
            imputarEnLote(pago, lote, ajuste.getFecha(), ajuste.getImporte());
        } else {
            double restante = -ajuste.getImporte();
            for (int i = imputaciones.size() - 1; i >= 0 && restante > 0.000001; i--) {
                ImputacionPago imputacion = imputaciones.get(i);
                double capital = Math.min(restante, imputacion.getCapitalSinRevertir());
                restante -= capital;
                double interes = Math.min(restante, imputacion.getInteresSinRevertir());
                restante -= interes;
                if (interes + capital > 0) {
                    imputacion.getExpensa().revertirPago(interes, capital, ajuste.getFecha());
                    imputacion.registrarReversion(interes, capital);
                }
            }
            if (restante > 0.000001) {
                throw new ReglaNegocioException("El ajuste supera las imputaciones vigentes.");
            }
        }
        pago.registrarAjuste(ajuste);
        lote.actualizarMorosidad();
    }

    public List<Notificacion> generarAvisosMorosidad() {
        List<Notificacion> nuevos = new ArrayList<>();
        for (Lote lote : barrio.getLotes()) {
            lote.actualizarMorosidad();
            if (lote.isMoroso() && lote.getTitular() != null) {
                boolean existeAviso = false;
                for (Notificacion aviso : avisos) {
                    if (aviso.getLote() == lote &&
                            aviso.getMotivo() == TipoNotificacion.MOROSIDAD) {
                        existeAviso = true;
                    }
                }
                if (!existeAviso) {
                    Notificacion aviso = new Notificacion(lote.getTitular(), lote,
                            "El lote tiene mas de tres periodos impagos.",
                            TipoNotificacion.MOROSIDAD);
                    avisos.add(aviso);
                    nuevos.add(aviso);
                }
            }
        }
        return nuevos;
    }

    public void agregarPagoRestaurado(Pago pago) { pagos.add(pago); }
    public void agregarAvisoRestaurado(Notificacion aviso) { avisos.add(aviso); }
    public List<Pago> getPagos() { return new ArrayList<>(pagos); }
    public List<Notificacion> getAvisos() { return new ArrayList<>(avisos); }
}
