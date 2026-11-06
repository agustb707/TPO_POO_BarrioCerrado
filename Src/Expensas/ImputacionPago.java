package Expensas;

public class ImputacionPago {
    private Expensa expensa;
    private double importeInteres;
    private double importeCapital;
    private double interesRevertido;
    private double capitalRevertido;

    public ImputacionPago(Expensa expensa, double importeInteres,
                          double importeCapital) {
        if (expensa == null || !Double.isFinite(importeInteres) ||
                !Double.isFinite(importeCapital) || importeInteres < 0 ||
                importeCapital < 0 || importeInteres + importeCapital <= 0) {
            throw new IllegalArgumentException("Imputacion invalida.");
        }
        this.expensa = expensa;
        this.importeInteres = importeInteres;
        this.importeCapital = importeCapital;
    }

    public double calcularTotalImputado() {
        return importeInteres + importeCapital;
    }

    // Se conserva la imputacion original; solo se registra cuanto se revirtio.
    public void registrarReversion(double interes, double capital) {
        if (interes < 0 || capital < 0 ||
                interes > getInteresSinRevertir() + 0.000001 ||
                capital > getCapitalSinRevertir() + 0.000001) {
            throw new IllegalArgumentException("Reversion de imputacion invalida.");
        }
        interesRevertido += interes;
        capitalRevertido += capital;
    }

    public double getInteresSinRevertir() {
        return importeInteres - interesRevertido;
    }

    public double getCapitalSinRevertir() {
        return importeCapital - capitalRevertido;
    }

    public Expensa getExpensa() { return expensa; }
    public double getImporteInteres() { return importeInteres; }
    public double getImporteCapital() { return importeCapital; }
}
