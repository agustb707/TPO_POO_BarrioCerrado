import Persona.Titular;

public class Lote {
    public static final int UMBRAL_MOROSIDAD = 2;

    private final int numeroLote;
    private final double superficie;
    private boolean moroso;
    private Titular titular;

    public Lote(int numeroLote, double superficie) {
        if (numeroLote <= 0) throw new IllegalArgumentException("El número de lote debe ser positivo");
        if (superficie <= 0) throw new IllegalArgumentException("La superficie debe ser mayor a 0");
        this.numeroLote = numeroLote;
        this.superficie = superficie;
    }
}
