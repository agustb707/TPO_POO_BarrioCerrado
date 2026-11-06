package Expensas;

import java.time.LocalDate;

public class AjustePago {
    private LocalDate fecha;
    private double importe;
    private String motivo;

    public AjustePago(LocalDate fecha, double importe, String motivo) {
        if (fecha == null || !Double.isFinite(importe) || importe == 0 ||
                motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Datos del ajuste invalidos.");
        }
        this.fecha = fecha;
        this.importe = importe;
        this.motivo = motivo;
    }

    public LocalDate getFecha() { return fecha; }
    public double getImporte() { return importe; }
    public String getMotivo() { return motivo; }
}
