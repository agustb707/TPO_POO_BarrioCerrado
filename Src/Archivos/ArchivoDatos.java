package Archivos;

import Barrio.BarrioCerrado;
import Barrio.Lote;
import Enums.*;
import Equipos.*;
import Expensas.*;
import Ingresos.*;
import Notificaciones.Notificacion;
import Persona.*;
import Reclamos.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Guarda el estado completo en un archivo de texto legible, sin serializacion binaria.
// Cada renglon tiene un tipo de registro y campos separados por punto y coma.
public class ArchivoDatos {
    private final File archivo;

    public ArchivoDatos(String ruta) {
        if (ruta == null || ruta.isBlank()) throw new IllegalArgumentException("Ruta obligatoria.");
        archivo = new File(ruta);
    }

    public boolean existe() { return archivo.exists(); }

    // Evita romper el formato si el usuario ingresa ;, saltos de linea o % en un texto.
    private String codificar(String texto) {
        if (texto == null) return "";
        return texto.replace("%", "%25").replace(";", "%3B")
                .replace("\n", "%0A").replace("\r", "%0D");
    }

    private String decodificar(String texto) {
        return texto.replace("%0D", "\r").replace("%0A", "\n")
                .replace("%3B", ";").replace("%25", "%");
    }

    private String valor(Object dato) { return dato == null ? "" : dato.toString(); }
    private String dni(Persona p) { return p == null ? "" : valor(p.getDni()); }
    private String dinero(double importe) { return String.format(java.util.Locale.US, "%.2f", importe); }
    private String numero(Lote l) { return l == null ? "" : valor(l.getNumeroLote()); }
    private String legajo(Personal p) { return p == null ? "" : valor(p.getLegajo()); }

    private void escribir(BufferedWriter salida, String... campos) throws IOException {
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) salida.write(';');
            salida.write(codificar(campos[i]));
        }
        salida.newLine();
    }

    private void guardarPersona(Map<String, Persona> personas, Persona p) {
        if (p != null) personas.put(valor(p.getDni()), p);
    }

    public void guardar(DatosSistema datos) throws IOException {
        if (datos == null) throw new IllegalArgumentException("Los datos son obligatorios.");
        File carpeta = archivo.getAbsoluteFile().getParentFile();
        if (carpeta != null && !carpeta.exists() && !carpeta.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de datos.");
        }
        BarrioCerrado barrio = datos.getBarrio();
        GestorExpensas gestor = datos.getGestorExpensas();
        SistemaIngreso ingresos = datos.getSistemaIngreso();

        Map<String, Persona> personas = new LinkedHashMap<>();
        for (Lote lote : barrio.getLotes()) {
            guardarPersona(personas, lote.getTitular());
            for (Residente r : lote.getResidentes()) guardarPersona(personas, r);
        }
        for (Personal p : barrio.getPersonal()) guardarPersona(personas, p);
        guardarPersona(personas, barrio.getAdministrador());
        for (Reclamo r : barrio.getReclamos()) {
            guardarPersona(personas, r.getResidente());
            guardarPersona(personas, r.getResponsable());
            for (Comentario c : r.getComentarios()) guardarPersona(personas, c.getAutor());
        }
        for (Equipo e : barrio.getEquipos()) {
            for (Falla f : e.getFallas()) guardarPersona(personas, f.getReportante());
        }
        for (AutorizacionIngreso a : ingresos.getAutorizaciones()) {
            guardarPersona(personas, a.getVisitante());
            guardarPersona(personas, a.getAutorizante());
        }
        for (Notificacion n : gestor.getAvisos()) guardarPersona(personas, n.getDestinatario());
        for (Notificacion n : datos.getAvisosAdministrador()) guardarPersona(personas, n.getDestinatario());

        try (BufferedWriter salida = new BufferedWriter(new FileWriter(archivo))) {
            escribir(salida, "# Los Aromos - registros de texto - version 1");
            escribir(salida, "BARRIO", barrio.getNombre());
            for (Lote l : barrio.getLotes()) {
                escribir(salida, "LOTE", valor(l.getNumeroLote()), valor(l.getSuperficie()));
            }
            for (Persona p : personas.values()) {
                String tipo;
                if (p instanceof Titular) tipo = "TITULAR";
                else if (p instanceof Familiar) tipo = "FAMILIAR";
                else if (p instanceof Inquilino) tipo = "INQUILINO";
                else if (p instanceof Administrador) tipo = "ADMINISTRADOR";
                else if (p instanceof Guardia) tipo = "GUARDIA";
                else if (p instanceof EmpleadoMantenimiento) tipo = "EMPLEADO";
                else if (p instanceof Proveedor) tipo = "PROVEEDOR";
                else if (p instanceof Visitante) tipo = "VISITANTE";
                else throw new IOException("Tipo de persona desconocido: " + p.getClass().getName());
                String leg = "", fecha = "", extra1 = "", extra2 = "";
                if (p instanceof Personal) {
                    Personal empleado = (Personal) p;
                    leg = valor(empleado.getLegajo());
                    fecha = valor(empleado.getFechaIngreso());
                }
                if (p instanceof Guardia) {
                    extra1 = valor(((Guardia) p).getTurno());
                    extra2 = valor(((Guardia) p).getPuesto());
                } else if (p instanceof EmpleadoMantenimiento) {
                    extra1 = valor(((EmpleadoMantenimiento) p).getEspecialidad());
                }
                escribir(salida, "PERSONA", tipo, dni(p), p.getNombre(), p.getApellido(),
                        valor(p.getTelefono()), valor(p.getEmail()), leg, fecha, extra1, extra2);
            }
            for (Lote l : barrio.getLotes()) {
                if (l.getTitular() != null) escribir(salida, "TITULAR_LOTE", numero(l), dni(l.getTitular()));
                for (Residente r : l.getResidentes()) escribir(salida, "RESIDENTE_LOTE", numero(l), dni(r));
                for (Residente r : l.getAutorizadosReclamos()) escribir(salida, "AUTORIZA_RECLAMO", numero(l), dni(r));
                for (Residente r : l.getAutorizadosIngreso()) escribir(salida, "AUTORIZA_VISITA", numero(l), dni(r));
            }
            for (Personal p : barrio.getPersonal()) escribir(salida, "PERSONAL_BARRIO", dni(p));
            if (barrio.getAdministrador() != null) {
                escribir(salida, "ADMINISTRADOR_BARRIO", dni(barrio.getAdministrador()));
            }
            for (Map.Entry<YearMonth, Double> presupuesto : barrio.getPresupuestos().entrySet()) {
                escribir(salida, "PRESUPUESTO", valor(presupuesto.getKey()), valor(presupuesto.getValue()));
            }
            for (Reclamo r : barrio.getReclamos()) {
                escribir(salida, "RECLAMO", valor(r.getNumero()), numero(r.getLote()),
                        dni(r.getResidente()), valor(r.getTipo()), r.getDescripcion(),
                        valor(r.getFecha()), valor(r.getEstado()), legajo(r.getResponsable()),
                        valor(r.getFechaAtencion()), valor(r.getFechaResolucion()));
                for (HistorialEstado h : r.consultarHistorial()) {
                    escribir(salida, "HISTORIAL", valor(r.getNumero()), valor(h.getFechaCambio()),
                            valor(h.getEstadoNuevo()), h.getObservacion());
                }
                for (Comentario c : r.getComentarios()) {
                    escribir(salida, "COMENTARIO", valor(r.getNumero()), valor(c.getFecha()),
                            dni(c.getAutor()), c.getTexto());
                }
            }
            for (Equipo e : barrio.getEquipos()) {
                if (e instanceof Camara) {
                    Camara c = (Camara) e;
                    escribir(salida, "EQUIPO", "CAMARA", e.getCodigo(), valor(e.getSector()),
                            valor(e.getFechaInstalacion()), valor(e.getEstado()),
                            valor(c.getResolucion()), valor(c.getCantidadDiasGrabacion()));
                } else if (e instanceof Luminaria) {
                    Luminaria l = (Luminaria) e;
                    escribir(salida, "EQUIPO", "LUMINARIA", e.getCodigo(), valor(e.getSector()),
                            valor(e.getFechaInstalacion()), valor(e.getEstado()),
                            valor(l.getTipoLampara()), valor(l.getPotencia()));
                }
                for (Falla f : e.getFallas()) {
                    escribir(salida, "FALLA", valor(f.getNumero()), e.getCodigo(),
                            dni(f.getReportante()), f.getDescripcion(), valor(f.getFechaRegistro()));
                }
                for (OrdenMantenimiento o : e.getOrdenes()) {
                    escribir(salida, "ORDEN", valor(o.getNumero()), valor(o.getFalla().getNumero()),
                            legajo(o.getCreador()), valor(o.getPrioridad()), o.getDescripcionTrabajo(),
                            valor(o.getFechaApertura()), legajo(o.getTecnico()),
                            valor(o.getFechaReparacion()), valor(o.getDescripcionReparacion()),
                            valor(o.getFechaCierre()));
                }
            }
            for (Lote l : barrio.getLotes()) {
                for (Expensa e : l.getExpensas()) {
                    escribir(salida, "EXPENSA", valor(e.getNumero()), numero(l), valor(e.getPeriodo()),
                            dinero(e.getCapital()), valor(e.getEstado()), dinero(e.calcularSaldoCapital()),
                            dinero(e.getInteresPendiente()), valor(e.getFechaUltimoInteres()),
                            valor(e.getFechaUltimaOperacion()));
                }
            }
            for (Pago p : gestor.getPagos()) {
                escribir(salida, "PAGO", valor(p.getNumero()), valor(p.getFecha()),
                        dinero(p.getImporte()), valor(p.getMedioPago()));
                for (ImputacionPago i : p.getImputaciones()) {
                    escribir(salida, "IMPUTACION", valor(p.getNumero()), valor(i.getExpensa().getNumero()),
                            dinero(i.getImporteInteres()), dinero(i.getImporteCapital()),
                            dinero(i.getInteresRevertido()), dinero(i.getCapitalRevertido()));
                }
                for (AjustePago a : p.getAjustes()) {
                    escribir(salida, "AJUSTE", valor(p.getNumero()), valor(a.getFecha()),
                            dinero(a.getImporte()), a.getMotivo());
                }
            }
            for (AutorizacionIngreso a : ingresos.getAutorizaciones()) {
                if (a instanceof AutorizacionRecurrente) {
                    AutorizacionRecurrente rec = (AutorizacionRecurrente) a;
                    escribir(salida, "AUTORIZACION", "RECURRENTE", valor(a.getNumero()),
                            dni(a.getVisitante()), dni(a.getAutorizante()), numero(a.getLote()),
                            valor(rec.getFechaInicio()), valor(rec.getFechaFin()),
                            valor(a.getHoraDesde()), valor(a.getHoraHasta()), "");
                } else {
                    escribir(salida, "AUTORIZACION", "NORMAL", valor(a.getNumero()),
                            dni(a.getVisitante()), dni(a.getAutorizante()), numero(a.getLote()),
                            valor(a.getFechaAutorizada()), "", valor(a.getHoraDesde()),
                            valor(a.getHoraHasta()), "");
                }
            }
            for (RegistroIngreso r : ingresos.getRegistros()) {
                escribir(salida, "INGRESO", valor(r.getNumero()),
                        valor(r.getAutorizacion().getNumero()), legajo(r.getGuardia()),
                        valor(r.getFechaHoraIngreso()), valor(r.getFechaHoraEgreso()));
            }
            for (Notificacion n : gestor.getAvisos()) guardarAviso(salida, "GESTOR", n);
            for (Notificacion n : datos.getAvisosAdministrador()) guardarAviso(salida, "ADMIN", n);
        }
    }

    private void guardarAviso(BufferedWriter salida, String grupo, Notificacion n)
            throws IOException {
        escribir(salida, "AVISO", grupo, valor(n.getFecha()), dni(n.getDestinatario()),
                numero(n.getLote()), n.getMensaje(), valor(n.getMotivo()));
    }

    private Map<String, List<String[]>> leerFilas() throws IOException {
        Map<String, List<String[]>> grupos = new HashMap<>();
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.isBlank() || linea.startsWith("#")) continue;
                String[] columnas = linea.split(";", -1);
                for (int i = 0; i < columnas.length; i++) columnas[i] = decodificar(columnas[i]);
                if (!grupos.containsKey(columnas[0])) grupos.put(columnas[0], new ArrayList<>());
                grupos.get(columnas[0]).add(columnas);
            }
        }
        return grupos;
    }

    private List<String[]> filas(Map<String, List<String[]>> datos, String tipo) {
        List<String[]> grupo = datos.get(tipo);
        return grupo == null ? new ArrayList<>() : grupo;
    }

    private void columnas(String[] c, int cantidad) throws IOException {
        if (c.length != cantidad) throw new IOException("Registro " + c[0] + " tiene campos incorrectos.");
    }
    private int entero(String s) { return Integer.parseInt(s); }
    private double decimal(String s) { return Double.parseDouble(s); }
    private LocalDate fecha(String s) { return s.isEmpty() ? null : LocalDate.parse(s); }
    private LocalDateTime fechaHora(String s) { return s.isEmpty() ? null : LocalDateTime.parse(s); }
    private LocalTime hora(String s) { return LocalTime.parse(s); }
    private YearMonth periodo(String s) { return YearMonth.parse(s); }

    private <T> T buscar(Map<String, T> mapa, String clave) {
        T obj = mapa.get(clave);
        if (obj == null) throw new IllegalArgumentException("Referencia inexistente: " + clave);
        return obj;
    }

    public DatosSistema cargar() throws IOException {
        Map<String, List<String[]>> grupos = leerFilas();
        try {
            if (filas(grupos, "BARRIO").size() != 1) {
                throw new IllegalArgumentException("Falta el nombre del barrio.");
            }
            BarrioCerrado barrio = new BarrioCerrado(filas(grupos, "BARRIO").get(0)[1]);
            SistemaIngreso sistema = new SistemaIngreso(barrio);
            GestorExpensas gestor = new GestorExpensas(barrio);
            List<Notificacion> avisosAdmin = new ArrayList<>();
            Map<String, Lote> lotes = new HashMap<>();
            Map<String, Persona> personas = new HashMap<>();
            Map<String, Personal> personalPorLegajo = new HashMap<>();
            Map<String, Equipo> equipos = new HashMap<>();
            Map<String, Falla> fallas = new HashMap<>();
            Map<String, Expensa> expensas = new HashMap<>();
            Map<String, Pago> pagos = new HashMap<>();
            Map<String, AutorizacionIngreso> autorizaciones = new HashMap<>();

            for (String[] c : filas(grupos, "LOTE")) {
                columnas(c, 3);
                Lote l = new Lote(entero(c[1]), decimal(c[2]));
                barrio.agregarLote(l);
                lotes.put(c[1], l);
            }
            for (String[] c : filas(grupos, "PERSONA")) {
                columnas(c, 11);
                Persona p;
                String t = c[1];
                if (t.equals("TITULAR")) p = new Titular(c[3], c[4], entero(c[2]), entero(c[5]), c[6]);
                else if (t.equals("FAMILIAR")) p = new Familiar(c[3], c[4], entero(c[2]), entero(c[5]), c[6]);
                else if (t.equals("INQUILINO")) p = new Inquilino(c[3], c[4], entero(c[2]), entero(c[5]), c[6]);
                else if (t.equals("VISITANTE")) p = new Visitante(c[3], c[4], entero(c[2]), entero(c[5]), c[6]);
                else if (t.equals("PROVEEDOR")) p = new Proveedor(c[3], c[4], entero(c[2]), entero(c[5]), c[6]);
                else if (t.equals("ADMINISTRADOR")) {
                    p = new Administrador(c[3], c[4], entero(c[2]), entero(c[5]), c[6], entero(c[7]), fecha(c[8]));
                } else if (t.equals("GUARDIA")) {
                    p = new Guardia(c[3], c[4], entero(c[2]), entero(c[5]), c[6], entero(c[7]), fecha(c[8]),
                            Turno.valueOf(c[9]), Puesto.valueOf(c[10]), sistema);
                } else if (t.equals("EMPLEADO")) {
                    p = new EmpleadoMantenimiento(c[3], c[4], entero(c[2]), entero(c[5]), c[6],
                            entero(c[7]), fecha(c[8]), Especialidad.valueOf(c[9]));
                } else throw new IllegalArgumentException("Tipo de persona no valido: " + t);
                if (personas.put(c[2], p) != null) throw new IllegalArgumentException("DNI repetido.");
                if (p instanceof Personal) personalPorLegajo.put(valor(((Personal) p).getLegajo()), (Personal) p);
            }
            for (String[] c : filas(grupos, "TITULAR_LOTE")) {
                columnas(c, 3);
                buscar(lotes, c[1]).asignarTitular((Titular) buscar(personas, c[2]));
            }
            for (String[] c : filas(grupos, "RESIDENTE_LOTE")) {
                columnas(c, 3);
                buscar(lotes, c[1]).agregarResidente((Residente) buscar(personas, c[2]));
            }
            for (String[] c : filas(grupos, "AUTORIZA_RECLAMO")) {
                columnas(c, 3);
                buscar(lotes, c[1]).autorizarReclamos((Residente) buscar(personas, c[2]));
            }
            for (String[] c : filas(grupos, "AUTORIZA_VISITA")) {
                columnas(c, 3);
                buscar(lotes, c[1]).autorizarIngresos((Residente) buscar(personas, c[2]));
            }
            for (String[] c : filas(grupos, "PERSONAL_BARRIO")) {
                columnas(c, 2);
                barrio.agregarPersonal((Personal) buscar(personas, c[1]));
            }
            for (String[] c : filas(grupos, "ADMINISTRADOR_BARRIO")) {
                columnas(c, 2);
                barrio.asignarAdministrador((Administrador) buscar(personas, c[1]));
            }
            for (String[] c : filas(grupos, "PRESUPUESTO")) {
                columnas(c, 3);
                barrio.registrarPresupuesto(periodo(c[1]), decimal(c[2]));
            }
            for (String[] c : filas(grupos, "EQUIPO")) {
                columnas(c, 8);
                Equipo equipo;
                if (c[1].equals("CAMARA")) {
                    equipo = new Camara(c[2], Sector.valueOf(c[3]), fecha(c[4]),
                            Resolucion.valueOf(c[6]), entero(c[7]));
                } else if (c[1].equals("LUMINARIA")) {
                    equipo = new Luminaria(c[2], Sector.valueOf(c[3]), fecha(c[4]),
                            TipoLuminaria.valueOf(c[6]), decimal(c[7]));
                } else throw new IllegalArgumentException("Tipo de equipo desconocido.");
                barrio.registrarEquipo(equipo);
                equipos.put(c[2], equipo);
            }
            Reclamo.restaurarReclamos(new ArrayList<>());
            for (String[] c : filas(grupos, "RECLAMO")) {
                columnas(c, 11);
                Reclamo r = new Reclamo(buscar(lotes, c[2]),
                        (Residente) buscar(personas, c[3]), TipoReclamo.valueOf(c[4]), c[5]);
                Personal responsable = c[8].isEmpty() ? null : buscar(personalPorLegajo, c[8]);
                List<HistorialEstado> historial = new ArrayList<>();
                for (String[] h : filas(grupos, "HISTORIAL")) {
                    columnas(h, 5);
                    if (h[1].equals(c[1])) historial.add(new HistorialEstado(
                            fechaHora(h[2]), EstadoReclamo.valueOf(h[3]), h[4]));
                }
                List<Comentario> comentarios = new ArrayList<>();
                for (String[] co : filas(grupos, "COMENTARIO")) {
                    columnas(co, 5);
                    if (co[1].equals(c[1])) {
                        Comentario comentario = new Comentario(buscar(personas, co[3]), co[4]);
                        comentario.restaurarFecha(fechaHora(co[2]));
                        comentarios.add(comentario);
                    }
                }
                r.restaurarDatos(entero(c[1]), fechaHora(c[6]), EstadoReclamo.valueOf(c[7]),
                        responsable, fechaHora(c[9]), fechaHora(c[10]), historial, comentarios);
                barrio.agregarReclamo(r);
            }
            for (String[] c : filas(grupos, "FALLA")) {
                columnas(c, 6);
                Equipo e = buscar(equipos, c[2]);
                Falla f = new Falla(e, buscar(personas, c[3]), c[4]);
                f.restaurarDatos(entero(c[1]), fechaHora(c[5]));
                e.registrarFalla(f);
                fallas.put(c[1], f);
            }
            for (String[] c : filas(grupos, "ORDEN")) {
                columnas(c, 11);
                Falla f = buscar(fallas, c[2]);
                Administrador creador = (Administrador) buscar(personalPorLegajo, c[3]);
                OrdenMantenimiento o = new OrdenMantenimiento(creador, f,
                        Prioridad.valueOf(c[4]), c[5]);
                if (!c[7].isEmpty()) o.asignarEmpleado(
                        (EmpleadoMantenimiento) buscar(personalPorLegajo, c[7]));
                o.restaurarDatos(entero(c[1]), fecha(c[6]), fecha(c[8]), c[9], fecha(c[10]));
            }
            for (String[] c : filas(grupos, "EQUIPO")) {
                buscar(equipos, c[2]).cambiarEstado(EstadoEquipo.valueOf(c[5]));
            }
            for (String[] c : filas(grupos, "EXPENSA")) {
                columnas(c, 10);
                Lote l = buscar(lotes, c[2]);
                Expensa e = new Expensa(entero(c[1]), periodo(c[3]), decimal(c[4]), l);
                l.agregarExpensa(e);
                expensas.put(c[1], e);
            }
            for (String[] c : filas(grupos, "PAGO")) {
                columnas(c, 5);
                Pago p = new Pago(entero(c[1]), fecha(c[2]), decimal(c[3]), MedioPago.valueOf(c[4]));
                gestor.agregarPagoRestaurado(p);
                pagos.put(c[1], p);
            }
            for (String[] c : filas(grupos, "IMPUTACION")) {
                columnas(c, 7);
                Pago p = buscar(pagos, c[1]);
                Expensa e = buscar(expensas, c[2]);
                ImputacionPago i = new ImputacionPago(e, decimal(c[3]), decimal(c[4]));
                if (decimal(c[5]) > 0 || decimal(c[6]) > 0) {
                    i.registrarReversion(decimal(c[5]), decimal(c[6]));
                }
                p.agregarImputacion(i);
                e.agregarPagoRestaurado(p);
            }
            for (String[] c : filas(grupos, "AJUSTE")) {
                columnas(c, 5);
                buscar(pagos, c[1]).registrarAjuste(new AjustePago(fecha(c[2]), decimal(c[3]), c[4]));
            }
            for (String[] c : filas(grupos, "EXPENSA")) {
                Expensa e = buscar(expensas, c[1]);
                e.restaurarDatos(EstadoExpensa.valueOf(c[5]), decimal(c[6]), decimal(c[7]),
                        fecha(c[8]), fecha(c[9]));
            }
            for (Lote l : barrio.getLotes()) l.actualizarMorosidad();
            for (String[] c : filas(grupos, "AUTORIZACION")) {
                columnas(c, 11);
                Visitante visitante = (Visitante) buscar(personas, c[3]);
                Residente residente = (Residente) buscar(personas, c[4]);
                Lote lote = buscar(lotes, c[5]);
                AutorizacionIngreso a;
                if (c[1].equals("NORMAL")) {
                    a = new AutorizacionIngreso(entero(c[2]), visitante, residente, lote,
                            fecha(c[6]), hora(c[8]), hora(c[9]));
                } else if (c[1].equals("RECURRENTE")) {
                    a = new AutorizacionRecurrente(entero(c[2]), visitante, lote, residente,
                            fecha(c[6]), fecha(c[7]), hora(c[8]), hora(c[9]));
                } else throw new IllegalArgumentException("Tipo de autorizacion desconocido.");
                sistema.agregarAutorizacion(a);
                autorizaciones.put(c[2], a);
            }
            for (String[] c : filas(grupos, "INGRESO")) {
                columnas(c, 6);
                RegistroIngreso r = new RegistroIngreso(entero(c[1]), buscar(autorizaciones, c[2]),
                        (Guardia) buscar(personalPorLegajo, c[3]), fechaHora(c[4]));
                if (!c[5].isEmpty()) r.registrarEgreso(fechaHora(c[5]));
                sistema.agregarRegistroRestaurado(r);
            }
            for (String[] c : filas(grupos, "AVISO")) {
                columnas(c, 7);
                Lote l = c[4].isEmpty() ? null : buscar(lotes, c[4]);
                Notificacion aviso = new Notificacion(buscar(personas, c[3]), l,
                        c[5], TipoNotificacion.valueOf(c[6]));
                aviso.restaurarFecha(fechaHora(c[2]));
                if (c[1].equals("ADMIN")) avisosAdmin.add(aviso);
                else if (c[1].equals("GESTOR")) gestor.agregarAvisoRestaurado(aviso);
                else throw new IllegalArgumentException("Origen de aviso desconocido.");
            }
            Reclamo.restaurarReclamos(barrio.getReclamos());
            Falla.restaurarSecuencia(barrio.getEquipos());
            OrdenMantenimiento.restaurarSecuencia(barrio.getEquipos());
            Residente.restaurarSecuenciaAutorizaciones(sistema.getAutorizaciones());
            return new DatosSistema(barrio, gestor, sistema, avisosAdmin);
        } catch (RuntimeException e) {
            throw new IOException("Los datos del TXT son incorrectos: " + e.getMessage(), e);
        }
    }
}
