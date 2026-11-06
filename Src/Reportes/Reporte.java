package Reportes;

import Barrio.Lote;
import Enums.EstadoReclamo;
import Equipos.Equipo;
import Reclamos.Reclamo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reporte {

    public List<Reclamo> generarReclamosPendientes(List<Reclamo> reclamos) {
        if (reclamos == null) {
            throw new IllegalArgumentException("La lista de reclamos es obligatoria.");
        }
        List<Reclamo> pendientes = new ArrayList<>();
        for (Reclamo reclamo : reclamos) {
            if (reclamo.getEstado() != EstadoReclamo.RESUELTO &&
                    reclamo.getEstado() != EstadoReclamo.CERRADO) {
                pendientes.add(reclamo);
            }
        }
        return pendientes;
    }

    // Incluye todos los lotes con deuda, tengan o no marca de moroso.
    public Map<Lote, Double> generarMorosidadPorLote(List<Lote> lotes) {
        if (lotes == null) {
            throw new IllegalArgumentException("La lista de lotes es obligatoria.");
        }
        Map<Lote, Double> deudas = new HashMap<>();
        for (Lote lote : lotes) {
            double deuda = 0;
            for (Expensas.Expensa expensa : lote.getExpensas()) {
                deuda += expensa.calcularDeuda(LocalDate.now());
            }
            if (deuda > 0.000001) {
                deudas.put(lote, deuda);
            }
        }
        return deudas;
    }

    public List<Equipo> generarEquiposFueraServicio(List<Equipo> equipos) {
        if (equipos == null) {
            throw new IllegalArgumentException("La lista de equipos es obligatoria.");
        }
        List<Equipo> fueraDeServicio = new ArrayList<>();
        for (Equipo equipo : equipos) {
            if (equipo.estaFueraDeServicio()) {
                fueraDeServicio.add(equipo);
            }
        }
        return fueraDeServicio;
    }
}
