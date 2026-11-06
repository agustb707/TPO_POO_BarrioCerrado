package Ingresos;

import Barrio.BarrioCerrado;
import Persona.Guardia;
import Persona.Visitante;
import Excepciones.ReglaNegocioException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        if (autorizacion == null) throw new IllegalArgumentException("La autorizacion es obligatoria.");
        if (barrio.buscarLote(autorizacion.getLote().getNumeroLote()) != autorizacion.getLote()) {
            throw new ReglaNegocioException("La autorizacion debe pertenecer al barrio.");
        }
        for (AutorizacionIngreso existente : autorizaciones) {
            if (existente.getNumero() == autorizacion.getNumero()) {
                throw new ReglaNegocioException("Numero de autorizacion repetido.");
            }
        }
        autorizaciones.add(autorizacion);
    }
    public AutorizacionIngreso buscarAutorizacionVigente(Visitante visitante, LocalDateTime fechaHora) {
        if (visitante == null || fechaHora == null) return null;
        for (AutorizacionIngreso autorizacion : autorizaciones) {
            if (autorizacion.correspondeAVisitante(visitante) && autorizacion.esValida(fechaHora)) {
                return autorizacion;
            }
        }
        return null;
    }
    public RegistroIngreso buscarIngresoAbierto(Visitante visitante) {
        if (visitante == null) return null;
        for (RegistroIngreso registro : registros) {
            if (registro.estaDentro() && registro.getAutorizacion().correspondeAVisitante(visitante)) {
                return registro;
            }
        }
        return null;
    }
    public RegistroIngreso registrarIngreso(Visitante visitante, Guardia guardia, LocalDateTime fechaHora) {
        if (visitante == null || guardia == null || fechaHora == null) {
            throw new IllegalArgumentException("Faltan datos del ingreso.");
        }
        if (buscarIngresoAbierto(visitante) != null) {
            throw new ReglaNegocioException("El visitante ya esta dentro del barrio.");
        }
        AutorizacionIngreso autorizacion = buscarAutorizacionVigente(visitante, fechaHora);
        if (autorizacion == null) throw new ReglaNegocioException("No hay autorizacion vigente.");
        for (RegistroIngreso registro : registros) {
            if (registro.getAutorizacion().correspondeAVisitante(visitante)
                    && !registro.estaDentro() && fechaHora.isBefore(registro.getFechaHoraEgreso())) {
                throw new ReglaNegocioException("Ingreso anterior a un egreso registrado.");
            }
        }
        int siguiente = 1;
        for (RegistroIngreso registro : registros) {
            if (registro.getNumero() >= siguiente) siguiente = registro.getNumero() + 1;
        }
        RegistroIngreso nuevo = new RegistroIngreso(siguiente, autorizacion, guardia, fechaHora);
        registros.add(nuevo);
        return nuevo;
    }
    public void registrarEgreso(RegistroIngreso registro, LocalDateTime fechaHora) {
        if (registro == null || !registros.contains(registro)) {
            throw new ReglaNegocioException("El registro no pertenece al sistema.");
        }
        registro.registrarEgreso(fechaHora);
    }
    public void agregarRegistroRestaurado(RegistroIngreso registro) {
        registros.add(registro);
    }
    public List<AutorizacionIngreso> getAutorizaciones() { return new ArrayList<>(autorizaciones); }
    public List<RegistroIngreso> getRegistros() { return new ArrayList<>(registros); }
}
