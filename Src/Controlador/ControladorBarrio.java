package Controlador;

import Archivos.ArchivoDatos;
import Archivos.ArchivoLotes;
import Archivos.DatosSistema;
import Barrio.BarrioCerrado;
import Barrio.Lote;
import Enums.*;
import Equipos.*;
import Expensas.*;
import Ingresos.*;
import Notificaciones.Notificacion;
import Persona.*;
import Reclamos.Reclamo;
import Reportes.Reporte;

import java.io.File;
import java.io.IOException;
import java.time.*;
import java.util.*;

// Controlador MVC: coordina la vista, el modelo y los archivos.
public class ControladorBarrio {
    private final ArchivoDatos archivo = new ArchivoDatos("Datos/LosAromos.txt");
    private BarrioCerrado barrio;
    private GestorExpensas expensas;
    private SistemaIngreso ingresos;
    private List<Notificacion> avisos;
    private final Reporte reportes = new Reporte();

    public ControladorBarrio() throws IOException {
        if (archivo.existe()) {
            DatosSistema datos = archivo.cargar();
            barrio = datos.getBarrio();
            expensas = datos.getGestorExpensas();
            ingresos = datos.getSistemaIngreso();
            avisos = datos.getAvisosAdministrador();
            Reclamo.restaurarReclamos(barrio.getReclamos());
            Falla.restaurarSecuencia(barrio.getEquipos());
            OrdenMantenimiento.restaurarSecuencia(barrio.getEquipos());
            Residente.restaurarSecuenciaAutorizaciones(ingresos.getAutorizaciones());
            // Actualizar reglas dependientes del tiempo al abrir el sistema.
            if (actualizarReglas()) guardar();
        } else {
            barrio = new BarrioCerrado("Los Aromos");
            ingresos = new SistemaIngreso(barrio);
            expensas = new GestorExpensas(barrio);
            avisos = new ArrayList<>();
            if (new File("Datos/Lotes.txt").exists()) {
                for (Lote lote : new ArchivoLotes("Datos/Lotes.txt").leerLotes()) barrio.agregarLote(lote);
            }
            guardar();
        }
    }
    public void guardar() throws IOException {
        actualizarReglas();
        archivo.guardar(new DatosSistema(barrio, expensas, ingresos, avisos));
    }

    // Actualiza reglas que pueden cumplirse con el paso del tiempo.
    // Devuelve true si se modificó algo para que pueda guardarse en TXT.
    public boolean actualizarReglas() {
        boolean cambios = false;
        LocalDateTime ahora = LocalDateTime.now();

        for (Reclamo reclamo : barrio.getReclamos()) {
            EstadoReclamo anterior = reclamo.getEstado();
            reclamo.cerrarSiVencioPlazo(ahora);
            if (reclamo.getEstado() != anterior) cambios = true;
        }

        // Los avisos de morosidad se guardan en GestorExpensas.
        if (!expensas.generarAvisosMorosidad().isEmpty()) cambios = true;

        // Los avisos de reclamos y cámaras se guardan para el administrador.
        if (barrio.getAdministrador() != null) {
            for (Notificacion nuevo : Notificacion.generarAvisosAdministrador(barrio)) {
                boolean existe = false;
                for (Notificacion anterior : avisos) {
                    if (anterior.getMotivo() == nuevo.getMotivo()
                            && anterior.getMensaje().equals(nuevo.getMensaje())) {
                        existe = true;
                        break;
                    }
                }
                if (!existe) {
                    avisos.add(nuevo);
                    cambios = true;
                }
            }
        }
        return cambios;
    }

    // Para refrescar las reglas desde la ventana, aunque no se realice un alta.
    public void actualizarYGuardar() throws IOException {
        if (actualizarReglas()) guardar();
    }
    public BarrioCerrado barrio() { return barrio; }
    public GestorExpensas expensas() { return expensas; }
    public SistemaIngreso ingresos() { return ingresos; }
    public Reporte reportes() { return reportes; }
    public Lote lote(int n) {
        Lote l = barrio.buscarLote(n);
        if (l == null) throw new IllegalArgumentException("No existe el lote " + n);
        return l;
    }
    public Persona persona(int dni) {
        for (Lote l : barrio.getLotes()) {
            if (l.getTitular() != null && l.getTitular().getDni() == dni) return l.getTitular();
            for (Residente r : l.getResidentes()) if (r.getDni() == dni) return r;
        }
        for (Personal p : barrio.getPersonal()) if (p.getDni() == dni) return p;
        for (AutorizacionIngreso a : ingresos.getAutorizaciones())
            if (a.getVisitante().getDni() == dni) return a.getVisitante();
        return null;
    }
    public Residente residente(Lote lote, int dni) {
        if (lote.getTitular() != null && lote.getTitular().getDni() == dni) return lote.getTitular();
        for (Residente r : lote.getResidentes()) if (r.getDni() == dni) return r;
        throw new IllegalArgumentException("Ese DNI no es residente del lote.");
    }
    public Personal empleado(int legajo) {
        for (Personal p : barrio.getPersonal()) if (p.getLegajo() == legajo) return p;
        throw new IllegalArgumentException("No existe el legajo " + legajo);
    }
    public EmpleadoMantenimiento tecnico(int legajo) {
        Personal p = empleado(legajo);
        if (!(p instanceof EmpleadoMantenimiento)) throw new IllegalArgumentException("El legajo no es de mantenimiento.");
        return (EmpleadoMantenimiento) p;
    }
    public Guardia guardia(int legajo) {
        Personal p = empleado(legajo);
        if (!(p instanceof Guardia)) throw new IllegalArgumentException("El legajo no corresponde a un guardia.");
        return (Guardia) p;
    }
    public Equipo equipo(String codigo) {
        for (Equipo e : barrio.getEquipos()) if (e.getCodigo().equalsIgnoreCase(codigo)) return e;
        throw new IllegalArgumentException("No se encontró el equipo.");
    }
    public Reclamo reclamo(int numero) {
        for (Reclamo r : barrio.getReclamos()) if (r.getNumero() == numero) return r;
        throw new IllegalArgumentException("No existe el reclamo.");
    }
    public OrdenMantenimiento orden(int numero) {
        for (Equipo e : barrio.getEquipos())
            for (OrdenMantenimiento o : e.getOrdenes()) if (o.getNumero() == numero) return o;
        throw new IllegalArgumentException("No existe la orden.");
    }
    public Pago pago(int numero) {
        for (Pago p : expensas.getPagos()) if (p.getNumero() == numero) return p;
        throw new IllegalArgumentException("No existe el pago.");
    }
    public RegistroIngreso registro(int numero) {
        for (RegistroIngreso r : ingresos.getRegistros()) if (r.getNumero() == numero) return r;
        throw new IllegalArgumentException("No existe el registro.");
    }
    public Visitante visitante(int dni) {
        Persona p = persona(dni);
        if (!(p instanceof Visitante)) throw new IllegalArgumentException("El visitante debe tener un permiso registrado.");
        return (Visitante) p;
    }
    public int siguientePermiso() {
        int n = 1;
        for (AutorizacionIngreso a : ingresos.getAutorizaciones()) n = Math.max(n, a.getNumero() + 1);
        return n;
    }
    public void registrarLote(int numero, double superficie, Titular titular) {
        Lote l = barrio.buscarLote(numero);
        if (l == null) { l = new Lote(numero, superficie); barrio.agregarLote(l); }
        if (l.getTitular() != null) throw new IllegalArgumentException("El lote ya tiene titular.");
        l.asignarTitular(titular);
        titular.agregarLote(l);
    }
    public void agregarResidente(int numero, Residente residente) {
        Lote l = lote(numero);
        l.agregarResidente(residente);
        residente.agregarLote(l);
    }
    public void registrarPersonal(Personal personal) {
        barrio.agregarPersonal(personal);
        if (personal instanceof Administrador) {
            barrio.asignarAdministrador((Administrador) personal);
            ((Administrador) personal).setBarrio(barrio);
        }
    }
    public Reclamo crearReclamo(Lote l, Residente r, TipoReclamo tipo, String descripcion,
                                Personal empleado, String comentario) {
        if (barrio.getAdministrador() == null) throw new IllegalArgumentException("No hay administradores existentes, registre al menos uno para continuar");
        Reclamo reclamo = r.presentarReclamo(l, tipo, descripcion);
        barrio.getAdministrador().asignarReclamo(reclamo, empleado);
        barrio.agregarReclamo(reclamo);
        if (!comentario.isBlank()) r.comentarReclamo(reclamo, comentario);
        return reclamo;
    }
    public List<Expensa> emitir(YearMonth mes, double presupuesto) {
        barrio.registrarPresupuesto(mes, presupuesto);
        return expensas.emitirExpensas(mes);
    }
    public OrdenMantenimiento crearOrden(Falla falla, EmpleadoMantenimiento tecnico, String trabajo) {
        if (barrio.getAdministrador() == null) throw new IllegalArgumentException("No hay administradores existentes, registre al menos uno para continuar");
        return barrio.getAdministrador().generarOrdenMantenimiento(falla, tecnico, trabajo);
    }
    public void registrarAvisos() {
        if (barrio.getAdministrador() == null) {
            throw new IllegalArgumentException("No hay administradores existentes, registre al menos uno para continuar");
        }
        actualizarReglas();
    }
    public List<Notificacion> avisos() { return new ArrayList<>(avisos); }
}

