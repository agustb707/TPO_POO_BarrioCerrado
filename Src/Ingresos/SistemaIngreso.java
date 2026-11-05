package Ingresos;

import Barrio.BarrioCerrado;
import Persona.Guardia;
import Persona.Visitante;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// GRASP: controlador
public class SistemaIngreso {
    private BarrioCerrado barrio;
    private List<AutorizacionIngreso> autorizaciones;
    private List<RegistroIngreso> registros;

    public SistemaIngreso(BarrioCerrado barrio) {
        if (barrio == null) throw new IllegalArgumentException("El barrio es obligatorio.");
        this.barrio = barrio;
        autorizaciones = new ArrayList<>();
        registros = new ArrayList<>();
    }

    public void agregarAutorizacion(AutorizacionIngreso autorizacion) {
        if (autorizacion == null || barrio.buscarLote(autorizacion.getLote().getNumeroLote())
                != autorizacion.getLote()) {
            throw new IllegalArgumentException("La autorizacion debe pertenecer al barrio.");
        }
        for (AutorizacionIngreso existente : autorizaciones) {
            if (existente.getNumero() == autorizacion.getNumero()) {
                throw new IllegalArgumentException("El numero de autorizacion ya existe.");
            }
        }
        autorizaciones.add(autorizacion);
    }

    public AutorizacionIngreso buscarAutorizacionVigente(Visitante visitante, LocalDateTime fechaHora) {
        for (AutorizacionIngreso autorizacion : autorizaciones) {
            if (autorizacion.correspondeAVisitante(visitante) && autorizacion.esValida(fechaHora)) {
                return autorizacion;
            }
        }
        return null;
    }

    public RegistroIngreso registrarIngreso(Visitante visitante, Guardia guardia, LocalDateTime fechaHora) {
        AutorizacionIngreso autorizacion = buscarAutorizacionVigente(visitante, fechaHora);
        if (autorizacion == null) throw new IllegalStateException("No hay autorizacion vigente.");
        for (RegistroIngreso registro : registros) {
            if (registro.getAutorizacion().correspondeAVisitante(visitante)) {
                if (registro.estaDentro()) throw new IllegalStateException("El visitante ya esta dentro.");
                if (fechaHora.isBefore(registro.getFechaHoraEgreso())) {
                    throw new IllegalArgumentException("El nuevo ingreso es anterior al ultimo egreso.");
                }
            }
        }
        RegistroIngreso nuevo = new RegistroIngreso(registros.size() + 1, autorizacion, guardia, fechaHora);
        registros.add(nuevo);
        return nuevo;
    }

    public void registrarEgreso(RegistroIngreso registro, LocalDateTime fechaHora) {
        if (registro == null || !registros.contains(registro)) {
            throw new IllegalArgumentException("El registro no pertenece al sistema.");
        }
        registro.registrarEgreso(fechaHora);
    }

    public List<RegistroIngreso> getRegistros() {
         return new ArrayList<>(registros); 
        }
}
