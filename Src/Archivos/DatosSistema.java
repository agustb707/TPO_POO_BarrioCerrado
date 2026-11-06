package Archivos;

import java.util.ArrayList;
import java.util.List;
import Barrio.BarrioCerrado;
import Expensas.GestorExpensas;
import Ingresos.SistemaIngreso;
import Notificaciones.Notificacion;

// Agrupa el estado del programa para guardarlo junto en un solo archivo.
public class DatosSistema {
    private BarrioCerrado barrio;
    private GestorExpensas gestorExpensas;
    private SistemaIngreso sistemaIngreso;
    private List<Notificacion> avisosAdministrador;

    public DatosSistema(BarrioCerrado barrio, GestorExpensas gestorExpensas,
                        SistemaIngreso sistemaIngreso, List<Notificacion> avisosAdministrador) {
        if (barrio == null || gestorExpensas == null || sistemaIngreso == null
                || avisosAdministrador == null) {
            throw new IllegalArgumentException("Faltan datos para guardar.");
        }
        this.barrio = barrio;
        this.gestorExpensas = gestorExpensas;
        this.sistemaIngreso = sistemaIngreso;
        this.avisosAdministrador = new ArrayList<>(avisosAdministrador);
    }

    public BarrioCerrado getBarrio() { return barrio; }
    public GestorExpensas getGestorExpensas() { return gestorExpensas; }
    public SistemaIngreso getSistemaIngreso() { return sistemaIngreso; }
    public List<Notificacion> getAvisosAdministrador() {
        return new ArrayList<>(avisosAdministrador);
    }
}
