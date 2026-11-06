package Vista;

import Controlador.ControladorBarrio;
import Barrio.Lote;
import Enums.*;
import Equipos.*;
import Expensas.*;
import Ingresos.*;
import Notificaciones.Notificacion;
import Persona.*;
import Reclamos.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.List;

// Vista MVC. Solo muestra formularios y llama al controlador y al modelo.
public class VentanaPrincipal extends JFrame {
    // Colores de la interfaz.
    private static final Color FONDO = new Color(240, 243, 239);
    private static final Color VERDE = new Color(46, 91, 63);
    private static final Color AZUL = VERDE;
    private static final Color OSCURO = new Color(38, 56, 43);
    private static final Color ACENTO = new Color(61, 112, 79);
    private static final Color LINEA = new Color(210, 219, 210);
    private static final Color LATERAL = new Color(38, 77, 53);
    private static final Color TEXTO_SUAVE = new Color(103, 118, 106);
    private final ControladorBarrio c;
    private final JPanel contenido = new JPanel(new BorderLayout(0, 18));
    private final JPanel botones = new JPanel(new GridLayout(0, 3, 13, 13));
    private final JTextArea listado = new JTextArea();
    private final JLabel titulo = new JLabel();
    private final JLabel descripcion = new JLabel();
    private final JLabel pie = new JLabel();
    private final JButton[] navegacion = new JButton[6];
    private int seccion = 0;

    private static class Cancelada extends RuntimeException { }
    private interface Operacion { void ejecutar(); }

    public VentanaPrincipal(ControladorBarrio controlador) {
        super("Los Aromos | Administración");
        c = controlador;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1050, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);
        add(crearMenu(), BorderLayout.WEST);
        add(crearContenido(), BorderLayout.CENTER);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { salir(); }
        });
        mostrarSeccion(0);
    }

    private JPanel crearMenu() {
        JPanel menu = new JPanel(new BorderLayout());
        menu.setBackground(LATERAL);
        menu.setPreferredSize(new Dimension(232, 0));
        menu.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, LINEA));

        JPanel superior = new JPanel(new GridLayout(0, 1, 0, 8));
        superior.setOpaque(false);
        superior.setBorder(new EmptyBorder(31, 22, 20, 16));
        JLabel simbolo = new JLabel("LOS AROMOS");
        simbolo.setForeground(Color.WHITE);
        simbolo.setFont(new Font("SansSerif", Font.BOLD, 19));
        superior.add(simbolo);
        JLabel subtitulo = new JLabel("Administración del barrio");
        subtitulo.setForeground(new Color(204, 224, 208));
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        superior.add(subtitulo);

        JPanel enlaces = new JPanel(new GridLayout(0, 1, 0, 8));
        enlaces.setOpaque(false);
        enlaces.setBorder(new EmptyBorder(28, 13, 10, 13));
        String[] nombres = {"Lotes y personal", "Reclamos", "Expensas", "Equipos", "Visitas", "Ver reportes"};
        for (int i = 0; i < nombres.length; i++) {
            final int indice = i;
            JButton b = new JButton(nombres[i]);
            b.setFont(new Font("SansSerif", Font.PLAIN, 13));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setContentAreaFilled(true);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(),
                    new EmptyBorder(12, 13, 12, 8)));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> mostrarSeccion(indice));
            navegacion[i] = b;
            enlaces.add(b);
        }
        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(enlaces, BorderLayout.NORTH);

        JPanel inferior = new JPanel(new BorderLayout(0, 8));
        inferior.setOpaque(false);
        inferior.setBorder(new EmptyBorder(16, 16, 25, 16));
        JButton guardar = new JButton("Guardar cambios");
        guardar.setFont(new Font("SansSerif", Font.BOLD, 12));
        guardar.setForeground(OSCURO);
        guardar.setBackground(new Color(214, 230, 216));
        guardar.setOpaque(true);
        guardar.setFocusPainted(false);
        guardar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 230, 216), 1, true),
                new EmptyBorder(13, 10, 13, 10)));
        guardar.addActionListener(e -> ejecutar(() -> { guardar(); mensaje("Los datos se guardaron en Datos/LosAromos.txt"); }));
        inferior.add(guardar, BorderLayout.NORTH);
        JLabel archivo = new JLabel("Datos/LosAromos.txt");
        archivo.setForeground(new Color(204, 224, 208));
        archivo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        archivo.setBorder(new EmptyBorder(7, 4, 0, 0));
        inferior.add(archivo, BorderLayout.SOUTH);
        menu.add(superior, BorderLayout.NORTH);
        menu.add(centro, BorderLayout.CENTER);
        menu.add(inferior, BorderLayout.SOUTH);
        return menu;
    }

    private JPanel crearContenido() {
        contenido.setBackground(FONDO);
        contenido.setBorder(new EmptyBorder(25, 28, 20, 28));

        JPanel encabezado = new JPanel(new BorderLayout(0, 13));
        encabezado.setOpaque(false);
        JLabel marca = new JLabel("ADMINISTRACIÓN DEL BARRIO");
        marca.setFont(new Font("SansSerif", Font.BOLD, 10));
        marca.setForeground(TEXTO_SUAVE);
        encabezado.add(marca, BorderLayout.NORTH);
        JPanel titulos = new JPanel(new GridLayout(0, 1, 0, 5));
        titulos.setOpaque(false);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        titulo.setForeground(OSCURO);
        descripcion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descripcion.setForeground(TEXTO_SUAVE);
        titulos.add(titulo);
        titulos.add(descripcion);
        encabezado.add(titulos, BorderLayout.CENTER);
        encabezado.setBorder(new EmptyBorder(0, 0, 25, 0));
        contenido.add(encabezado, BorderLayout.NORTH);

        JPanel central = new JPanel(new BorderLayout(0, 23));
        central.setOpaque(false);
        JPanel acciones = new JPanel(new BorderLayout(0, 12));
        acciones.setOpaque(false);
        JLabel lblAcciones = new JLabel("Opciones");
        lblAcciones.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblAcciones.setForeground(VERDE);
        acciones.add(lblAcciones, BorderLayout.NORTH);
        botones.setOpaque(false);
        acciones.add(botones, BorderLayout.CENTER);
        central.add(acciones, BorderLayout.NORTH);

        JPanel tarjeta = new JPanel(new BorderLayout(0, 12));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINEA, 1, true), new EmptyBorder(16, 18, 16, 18)));
        JLabel etiqueta = new JLabel("Registros");
        etiqueta.setForeground(VERDE);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 11));
        tarjeta.add(etiqueta, BorderLayout.NORTH);
        listado.setFont(new Font("SansSerif", Font.PLAIN, 13));
        listado.setForeground(new Color(66, 83, 71));
        listado.setBackground(Color.WHITE);
        listado.setEditable(false);
        listado.setMargin(new Insets(11, 2, 10, 10));
        listado.setLineWrap(true);
        listado.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(listado, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tarjeta.add(scroll, BorderLayout.CENTER);
        central.add(tarjeta, BorderLayout.CENTER);
        contenido.add(central, BorderLayout.CENTER);

        pie.setForeground(TEXTO_SUAVE);
        pie.setFont(new Font("SansSerif", Font.PLAIN, 11));
        pie.setBorder(new EmptyBorder(17, 0, 0, 0));
        contenido.add(pie, BorderLayout.SOUTH);
        return contenido;
    }

    private void agregarAccion(String nombre, Operacion operacion) {
        JButton b = new JButton(nombre);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setForeground(OSCURO);
        b.setBackground(Color.WHITE);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINEA, 1, true),
                new EmptyBorder(13, 12, 13, 6)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> ejecutar(operacion));
        botones.add(b);
    }
    private void ejecutar(Operacion accion) {
        try {
            accion.ejecutar();
            c.guardar();
            actualizarListado();
        } catch (Cancelada ignored) {
            // El usuario cerró un formulario.
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se pudo completar la operación", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void guardar() {
        try { c.guardar(); }
        catch (IOException e) { throw new IllegalStateException("No se pudo guardar el TXT: " + e.getMessage()); }
    }
    private void mensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Los Aromos", JOptionPane.INFORMATION_MESSAGE);
    }
    private void mostrarSeccion(int indice) {
        seccion = indice;
        String[] nombres = {"Administrar lotes", "Administrar reclamos", "Administrar expensas",
                "Administrar equipos", "Administrar visitas", "Ver reportes"};
        String[] textos = {"Titulares, familias, inquilinos y personal del barrio.",
                "Solicitudes, asignaciones, comentarios e historial de estados.",
                "Presupuestos, liquidaciones, pagos y deudas.",
                "Cámaras, luminarias, fallas y reparaciones.",
                "Permisos de ingreso, proveedores y movimientos de portería.",
                "Consultá la información ya registrada, sin generar reportes nuevos."};
        titulo.setText(nombres[indice]);
        descripcion.setText(textos[indice]);
        for (int i = 0; i < navegacion.length; i++) {
            navegacion[i].setBackground(i == indice ? new Color(215, 233, 218) : LATERAL);
            navegacion[i].setForeground(i == indice ? OSCURO : Color.WHITE);
            navegacion[i].setFont(new Font("SansSerif", i == indice ? Font.BOLD : Font.PLAIN, 13));
        }
        botones.removeAll();
        switch (indice) {
            case 0:
                agregarAccion("Registrar lote y titular", this::altaLote);
                agregarAccion("Agregar residente", this::altaResidente);
                agregarAccion("Autorizar residente", this::autorizarResidente);
                agregarAccion("Registrar personal", this::altaPersonal);
                break;
            case 1:
                agregarAccion("Nuevo reclamo", this::altaReclamo);
                agregarAccion("Cambiar estado", this::estadoReclamo);
                agregarAccion("Asignar responsable", this::asignarReclamo);
                agregarAccion("Agregar comentario", this::comentarioReclamo);
                agregarAccion("Reabrir reclamo", this::reabrirReclamo);
                agregarAccion("Ver historial", this::historialReclamo);
                break;
            case 2:
                agregarAccion("Emitir expensas", this::emitirExpensas);
                agregarAccion("Registrar pago", this::registrarPago);
                agregarAccion("Ajustar un pago", this::ajustarPago);
                agregarAccion("Consultar lote", this::consultarExpensas);
                agregarAccion("Consultar deudas", this::consultarDeudas);
                break;
            case 3:
                agregarAccion("Registrar equipo", this::altaEquipo);
                agregarAccion("Registrar falla", this::registrarFalla);
                agregarAccion("Crear orden", this::crearOrden);
                agregarAccion("Cambiar estado", this::cambiarEstadoEquipo);
                agregarAccion("Registrar reparación", this::registrarReparacion);
                agregarAccion("Avisos al administrador", this::avisosEquipo);
                break;
            case 4:
                agregarAccion("Autorizar visitante", this::autorizarVisita);
                agregarAccion("Registrar ingreso", this::registrarIngreso);
                agregarAccion("Registrar egreso", this::registrarEgreso);
                agregarAccion("Consultar permisos", this::consultarPermisos);
                break;
            default:
                agregarAccion("Reclamos pendientes", () -> mostrarTexto(pendientes()));
                agregarAccion("Lotes con deuda", () -> mostrarTexto(deudas()));
                agregarAccion("Equipos fuera de servicio", () -> mostrarTexto(fueraServicio()));
        }
        botones.revalidate();
        botones.repaint();
        actualizarListado();
    }
    private void actualizarListado() {
        try {
            c.actualizarYGuardar();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron actualizar los datos: " + e.getMessage(),
                    "Error al guardar", JOptionPane.ERROR_MESSAGE);
        }
        StringBuilder s = new StringBuilder();
        switch (seccion) {
            case 0:
                for (Lote l : c.barrio().getLotes()) {
                    s.append("LOTE ").append(l.getNumeroLote()).append("   •   ")
                            .append(dinero(l.getSuperficie())).append(" m²   •   ")
                            .append(l.getTitular() == null ? "Sin titular" : l.getTitular().obtenerNombreCompleto())
                            .append("   •   ").append(l.getResidentes().size()).append(" residentes\n\n");
                }
                s.append("PERSONAL\n");
                for (Personal p : c.barrio().getPersonal()) s.append(p.getLegajo()).append(" · ")
                        .append(p.obtenerNombreCompleto()).append(" · ").append(p.getClass().getSimpleName()).append("\n");
                break;
            case 1:
                for (Reclamo r : c.barrio().getReclamos()) s.append("#").append(r.getNumero())
                        .append("  ·  Lote ").append(r.getLote().getNumeroLote()).append("  ·  ")
                        .append(r.getPrioridad()).append("  ·  ").append(r.getEstado()).append("\n")
                        .append(r.getDescripcion()).append("\nResponsable: ")
                        .append(r.getResponsable() == null ? "Sin asignar" : r.getResponsable().obtenerNombreCompleto())
                        .append("\n\n");
                break;
            case 2:
                for (Lote l : c.barrio().getLotes()) {
                    for (Expensa x : l.getExpensas()) s.append("LOTE ").append(l.getNumeroLote())
                            .append("  ·  ").append(x.getPeriodo()).append("  ·  $").append(dinero(x.getCapital()))
                            .append("  ·  ").append(x.getEstado()).append("  ·  deuda $")
                            .append(dinero(x.calcularDeuda(LocalDate.now()))).append("\n\n");
                }
                break;
            case 3:
                for (Equipo e : c.barrio().getEquipos()) {
                    s.append(e.getCodigo()).append("  ·  ").append(e.getClass().getSimpleName())
                            .append("  ·  ").append(e.getSector()).append("  ·  ").append(e.getEstado())
                            .append("\n  Fallas: ").append(e.getFallas().size())
                            .append("   Órdenes: ").append(e.getOrdenes().size()).append("\n\n");
                }
                break;
            case 4:
                s.append("AUTORIZACIONES\n");
                for (AutorizacionIngreso a : c.ingresos().getAutorizaciones()) {
                    s.append("#").append(a.getNumero()).append("  ·  ")
                            .append(a.getVisitante().obtenerNombreCompleto()).append(" · Lote ")
                            .append(a.getLote().getNumeroLote()).append(" · ")
                            .append(a instanceof AutorizacionRecurrente ? "Proveedor recurrente" : "Visita puntual")
                            .append("\n");
                }
                s.append("\nINGRESOS Y EGRESOS\n");
                for (RegistroIngreso r : c.ingresos().getRegistros())
                    s.append("#").append(r.getNumero()).append("  ·  ")
                            .append(r.getAutorizacion().getVisitante().obtenerNombreCompleto())
                            .append("  ·  Entrada: ").append(r.getFechaHoraIngreso())
                            .append("  ·  Salida: ").append(r.estaDentro() ? "Aún dentro" : r.getFechaHoraEgreso())
                            .append("\n");
                break;
            default:
                s.append("RECLAMOS PENDIENTES\n").append(pendientes())
                        .append("\nLOTES CON DEUDAS\n").append(deudas())
                        .append("\nEQUIPOS FUERA DE SERVICIO\n").append(fueraServicio());
        }
        listado.setText(s.length() == 0 ? "No hay registros para mostrar todavía." : s.toString());
        listado.setCaretPosition(0);
        pie.setText("Lotes: " + c.barrio().getLotes().size() + "    •    Reclamos: "
                + c.barrio().getReclamos().size() + "    •    Equipos: " + c.barrio().getEquipos().size()
                + "    •    Archivo: Datos/LosAromos.txt");
    }

    private String[] formulario(String nombre, String... campos) {
        JPanel p = new JPanel(new GridLayout(0, 2, 12, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        JTextField[] editores = new JTextField[campos.length / 2];
        for (int i = 0; i < editores.length; i++) {
            JLabel etiquetaCampo = new JLabel(campos[2 * i]);
            etiquetaCampo.setForeground(OSCURO);
            p.add(etiquetaCampo);
            editores[i] = new JTextField(campos[2 * i + 1], 18);
            editores[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINEA, 1, true), new EmptyBorder(7, 9, 7, 9)));
            p.add(editores[i]);
        }
        int r = JOptionPane.showConfirmDialog(this, p, nombre, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) throw new Cancelada();
        String[] resultado = new String[editores.length];
        for (int i = 0; i < editores.length; i++) resultado[i] = editores[i].getText().trim();
        return resultado;
    }
    private String pedir(String titulo, String ejemplo) {
        String v = JOptionPane.showInputDialog(this, titulo, ejemplo);
        if (v == null) throw new Cancelada();
        if (v.isBlank()) throw new IllegalArgumentException("El dato es obligatorio.");
        return v.trim();
    }
    private int entero(String s) { return Integer.parseInt(s.trim()); }
    private double decimal(String s) {
        String t = s.trim().replace(',', '.');
        if (!t.matches("-?[0-9]+(\\.[0-9]{1,2})?"))
            throw new IllegalArgumentException("Ingresá un importe con máximo dos decimales.");
        return Double.parseDouble(t);
    }
    private int dni(String s) {
        int n = entero(s);
        if (n < 1000000 || n > 999999999) throw new IllegalArgumentException("El DNI debe tener entre 7 y 9 dígitos.");
        return n;
    }
    private int telefono(String s) {
        int n = entero(s);
        if (n < 10000000 || n > 99999999) throw new IllegalArgumentException("Teléfono: exactamente 8 dígitos sin código de área.");
        return n;
    }
    private String correo(String s) {
        int a = s.indexOf('@'), punto = s.indexOf('.', a + 1);
        if (a <= 0 || punto <= a + 1 || punto >= s.length() - 1 || s.indexOf('@', a + 1) != -1)
            throw new IllegalArgumentException("El email debe contener @ y un punto posterior.");
        return s;
    }
    private LocalDate fecha(String s) { return LocalDate.parse(s); }
    private LocalTime hora(String s) { return LocalTime.parse(s); }
    private String dinero(double n) { return String.format(java.util.Locale.US, "%.2f", n); }
    private <T extends Enum<T>> T elegir(String titulo, T[] valores) {
        Object obj = JOptionPane.showInputDialog(this, titulo, "Seleccionar", JOptionPane.QUESTION_MESSAGE,
                null, valores, valores[0]);
        if (obj == null) throw new Cancelada();
        return (T) obj;
    }
    private boolean confirmar(String pregunta) {
        return JOptionPane.showConfirmDialog(this, pregunta, "Los Aromos",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    private void mostrarTexto(String texto) {
        JTextArea area = new JTextArea(texto, 18, 65);
        area.setEditable(false);
        area.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Consulta", JOptionPane.PLAIN_MESSAGE);
    }
    private String[] datosPersona(String titulo) {
        return formulario(titulo, "Nombre", "", "Apellido", "", "DNI (7 a 9 dígitos)", "",
                "Teléfono (8 dígitos, sin código de área)", "", "Email", "");
    }
    private Titular crearTitular(String[] d) {
        int id = dni(d[2]);
        Persona p = c.persona(id);
        if (p instanceof Titular) return (Titular) p;
        if (p != null) throw new IllegalArgumentException("El DNI pertenece a otro tipo de persona.");
        return new Titular(d[0], d[1], id, telefono(d[3]), correo(d[4]));
    }
    private void altaLote() {
        String[] d = formulario("Registrar lote", "Número de lote", "", "Superficie (m²)", "400");
        int n = entero(d[0]);
        double superficie = decimal(d[1]);
        c.registrarLote(n, superficie, crearTitular(datosPersona("Datos del titular")));
        mensaje("Lote registrado. Ahora podés agregar familiares o inquilinos.");
    }
    private void altaResidente() {
        int n = entero(pedir("Número de lote", "1"));
        Lote l = c.lote(n);
        if (l.getTitular() == null) throw new IllegalArgumentException("Primero registrá un titular.");
        String tipo = elegir("Tipo de residente", new TipoPersona[]{TipoPersona.FAMILIAR, TipoPersona.INQUILINO}).name();
        String[] d = datosPersona("Datos del " + tipo.toLowerCase());
        int id = dni(d[2]);
        Persona p = c.persona(id);
        Residente r;
        if (p != null) {
            if (!(p instanceof Residente) || (tipo.equals("FAMILIAR") != (p instanceof Familiar)))
                throw new IllegalArgumentException("El DNI está registrado con otro tipo.");
            r = (Residente) p;
        } else {
            r = tipo.equals("FAMILIAR") ? new Familiar(d[0], d[1], id, telefono(d[3]), correo(d[4]))
                    : new Inquilino(d[0], d[1], id, telefono(d[3]), correo(d[4]));
        }
        c.agregarResidente(n, r);
        if (confirmar("¿Autorizar para presentar reclamos?")) l.autorizarReclamos(r);
        if (confirmar("¿Autorizar para permitir visitas?")) l.autorizarIngresos(r);
        mensaje("Residente agregado.");
    }
    private enum TipoPersona { FAMILIAR, INQUILINO }
    private void autorizarResidente() {
        String[] d = formulario("Autorizar residente", "Número de lote", "1", "DNI del residente", "");
        Lote l = c.lote(entero(d[0]));
        Residente r = c.residente(l, dni(d[1]));
        if (r == l.getTitular()) throw new IllegalArgumentException("El titular ya está autorizado.");
        String opcion = elegir("¿Qué querés autorizar?", new PermisoTipo[]{PermisoTipo.RECLAMOS, PermisoTipo.VISITAS}).name();
        if (opcion.equals("RECLAMOS")) l.autorizarReclamos(r); else l.autorizarIngresos(r);
        mensaje("Autorización registrada.");
    }
    private enum PermisoTipo { RECLAMOS, VISITAS }
    private void altaPersonal() {
        Cargo cargo = elegir("Tipo de personal", Cargo.values());
        if (cargo == Cargo.ADMINISTRADOR && c.barrio().getAdministrador() != null)
            throw new IllegalArgumentException("Ya hay un administrador registrado.");
        String[] d = formulario("Registrar personal", "Nombre", "", "Apellido", "", "DNI", "",
                "Teléfono (8 dígitos)", "", "Email", "", "Legajo", "", "Ingreso laboral (AAAA-MM-DD)", "2024-03-15");
        int id = dni(d[2]);
        if (c.persona(id) != null) throw new IllegalArgumentException("El DNI ya está registrado.");
        LocalDate ingreso = fecha(d[6]);
        if (!ingreso.isBefore(LocalDate.now())) throw new IllegalArgumentException("La fecha de ingreso debe ser anterior a hoy.");
        Personal p;
        if (cargo == Cargo.ADMINISTRADOR)
            p = new Administrador(d[0], d[1], id, telefono(d[3]), correo(d[4]), entero(d[5]), ingreso);
        else if (cargo == Cargo.GUARDIA)
            p = new Guardia(d[0], d[1], id, telefono(d[3]), correo(d[4]), entero(d[5]), ingreso,
                    elegir("Turno", Turno.values()), elegir("Puesto", Puesto.values()), c.ingresos());
        else p = new EmpleadoMantenimiento(d[0], d[1], id, telefono(d[3]), correo(d[4]),
                    entero(d[5]), ingreso, elegir("Especialidad", Especialidad.values()));
        c.registrarPersonal(p);
        mensaje("Personal registrado correctamente.");
    }
    private enum Cargo { ADMINISTRADOR, GUARDIA, MANTENIMIENTO }
    private void altaReclamo() {
        String[] d = formulario("Nuevo reclamo", "Lote", "1", "DNI del residente autorizado", "",
                "Descripción", "", "Legajo del empleado responsable", "", "Comentario inicial (opcional)", "");
        Lote l = c.lote(entero(d[0]));
        Reclamo r = c.crearReclamo(l, c.residente(l, dni(d[1])),
                elegir("Tipo de reclamo", TipoReclamo.values()), d[2], c.empleado(entero(d[3])), d[4]);
        mensaje("Reclamo #" + r.getNumero() + " registrado. Prioridad: " + r.getPrioridad());
    }
    private void estadoReclamo() {
        String[] d = formulario("Cambiar estado", "Número de reclamo", "", "Observación", "");
        Reclamo r = c.reclamo(entero(d[0]));
        r.cambiarEstado(elegir("Nuevo estado", EstadoReclamo.values()), d[1]);
        mensaje("Estado actualizado.");
    }
    private void asignarReclamo() {
        String[] d = formulario("Asignar responsable", "Número de reclamo", "", "Legajo de empleado", "");
        if (c.barrio().getAdministrador() == null) throw new IllegalArgumentException("No hay administradores existentes, registre al menos uno para continuar");
        c.barrio().getAdministrador().asignarReclamo(c.reclamo(entero(d[0])), c.empleado(entero(d[1])));
        mensaje("Responsable asignado.");
    }
    private void comentarioReclamo() {
        String[] d = formulario("Agregar comentario", "Número de reclamo", "", "DNI o legajo del autor", "", "Comentario", "");
        Reclamo r = c.reclamo(entero(d[0]));
        int numero = entero(d[1]);
        Persona autor = c.persona(numero);
        if (autor == null) { try { autor = c.empleado(numero); } catch (IllegalArgumentException ignored) { } }
        if (autor == null) throw new IllegalArgumentException("No se encontró al autor.");
        r.agregarComentario(autor, d[2]);
        mensaje("Comentario agregado.");
    }
    private void reabrirReclamo() {
        String[] d = formulario("Reabrir reclamo", "Número de reclamo", "", "DNI del residente", "");
        Reclamo r = c.reclamo(entero(d[0]));
        r.reabrir(c.residente(r.getLote(), dni(d[1])), LocalDateTime.now());
        mensaje("Reclamo reabierto.");
    }
    private void historialReclamo() {
        Reclamo r = c.reclamo(entero(pedir("Número de reclamo", "1")));
        StringBuilder s = new StringBuilder();
        for (HistorialEstado h : r.consultarHistorial())
            s.append(h.getFechaCambio()).append(" | ").append(h.getEstadoNuevo())
                    .append(" | ").append(h.getObservacion()).append("\n");
        for (Comentario co : r.getComentarios())
            s.append("Comentario de ").append(co.getAutor().obtenerNombreCompleto()).append(": ")
                    .append(co.getTexto()).append("\n");
        mostrarTexto(s.toString());
    }
    private void emitirExpensas() {
        String[] d = formulario("Emitir expensas", "Período (AAAA-MM)", YearMonth.now().toString(),
                "Presupuesto total ($)", "420000.00");
        int n = c.emitir(YearMonth.parse(d[0]), decimal(d[1])).size();
        mensaje("Se emitieron " + n + " expensas.");
    }
    private void registrarPago() {
        String[] d = formulario("Registrar pago", "Número de lote", "1", "Importe ($)", "",
                "Fecha (AAAA-MM-DD)", LocalDate.now().toString());
        Pago pago = c.expensas().registrarPago(c.lote(entero(d[0])), fecha(d[2]), decimal(d[1]),
                elegir("Medio de pago", MedioPago.values()));
        mensaje("Pago #" + pago.getNumero() + " registrado.");
    }
    private void ajustarPago() {
        String[] d = formulario("Ajuste de pago", "Número de pago", "", "Importe (+ o -)", "",
                "Fecha (AAAA-MM-DD)", LocalDate.now().toString(), "Motivo", "");
        c.expensas().registrarAjuste(c.pago(entero(d[0])), new AjustePago(fecha(d[2]), decimal(d[1]), d[3]));
        mensaje("Ajuste registrado sin borrar el pago original.");
    }
    private void consultarExpensas() {
        Lote l = c.lote(entero(pedir("Número de lote", "1")));
        StringBuilder s = new StringBuilder();
        for (Expensa e : l.getExpensas()) s.append(e.getPeriodo()).append(" | Capital: $")
                .append(dinero(e.getCapital())).append(" | Deuda: $")
                .append(dinero(e.calcularDeuda(LocalDate.now()))).append(" | ").append(e.getEstado()).append("\n");
        mostrarTexto(s.length() == 0 ? "El lote no tiene expensas." : s.toString());
    }
    private String deudas() {
        StringBuilder s = new StringBuilder();
        for (Map.Entry<Lote, Double> par : c.reportes().generarMorosidadPorLote(c.barrio().getLotes()).entrySet())
            s.append("Lote ").append(par.getKey().getNumeroLote()).append("  |  $")
                    .append(dinero(par.getValue())).append("\n");
        return s.length() == 0 ? "No hay deudas registradas.\n" : s.toString();
    }
    private void consultarDeudas() { mostrarTexto(deudas()); }
    private void altaEquipo() {
        boolean camara = elegir("Tipo de equipo", new EquipoTipo[]{EquipoTipo.CAMARA, EquipoTipo.LUMINARIA}) == EquipoTipo.CAMARA;
        String[] d = formulario("Registrar equipo", "Código", "", "Instalación (AAAA-MM-DD)", "2024-05-01");
        Sector sector = elegir("Sector", Sector.values());
        Equipo e = camara ? new Camara(d[0], sector, fecha(d[1]), elegir("Resolución", Resolucion.values()),
                entero(pedir("Días de grabación", "30")))
                : new Luminaria(d[0], sector, fecha(d[1]), elegir("Tipo de lámpara", TipoLuminaria.values()),
                decimal(pedir("Potencia en watts", "80")));
        c.barrio().registrarEquipo(e);
        mensaje("Equipo registrado.");
    }
    private enum EquipoTipo { CAMARA, LUMINARIA }
    private void registrarFalla() {
        String[] d = formulario("Registrar falla", "Código de equipo", "", "Descripción de la falla", "",
                "DNI de residente o legajo del guardia", "");
        Equipo equipo = c.equipo(d[0]);
        int id = entero(d[2]);
        Persona p = c.persona(id);
        Falla falla;
        if (p instanceof Residente) falla = ((Residente) p).registrarFalla(equipo, d[1]);
        else falla = c.guardia(id).registrarFalla(equipo, d[1]);
        if (confirmar("Falla #" + falla.getNumero() + " registrada. ¿Crear orden ahora?")) {
            int legajo = entero(pedir("Legajo del técnico", ""));
            c.crearOrden(falla, c.tecnico(legajo), pedir("Trabajo a realizar", "Revisar equipo"));
        }
        mensaje("Falla registrada.");
    }
    private void crearOrden() {
        String[] d = formulario("Crear orden", "Código de equipo", "", "Número de falla", "",
                "Legajo del técnico", "", "Descripción del trabajo", "");
        Falla elegida = null;
        for (Falla f : c.equipo(d[0]).getFallas()) if (f.getNumero() == entero(d[1])) elegida = f;
        if (elegida == null) throw new IllegalArgumentException("La falla no existe en el equipo.");
        OrdenMantenimiento o = c.crearOrden(elegida, c.tecnico(entero(d[2])), d[3]);
        mensaje("Orden #" + o.getNumero() + " creada. Prioridad " + o.getPrioridad());
    }
    private void cambiarEstadoEquipo() {
        Equipo e = c.equipo(pedir("Código del equipo", "CAM-001"));
        e.cambiarEstado(elegir("Nuevo estado", EstadoEquipo.values()));
        mensaje("Estado actualizado.");
    }
    private void registrarReparacion() {
        String[] d = formulario("Reparar y cerrar orden", "Número de orden", "",
                "Descripción de la reparación", "", "Fecha (AAAA-MM-DD)", LocalDate.now().toString());
        OrdenMantenimiento o = c.orden(entero(d[0]));
        if (o.getTecnico() == null) throw new IllegalArgumentException("Primero debe asignarse un técnico.");
        o.getTecnico().registrarReparacion(o, d[1], fecha(d[2]));
        o.getTecnico().cerrarOrden(o);
        mensaje("Reparación registrada y orden cerrada.");
    }
    private void avisosEquipo() {
        c.registrarAvisos();
        StringBuilder s = new StringBuilder();
        for (Notificacion a : c.avisos()) s.append(a.generarAviso()).append("\n");
        mostrarTexto(s.length() == 0 ? "No hay avisos pendientes." : s.toString());
    }
    private void autorizarVisita() {
        String[] d = formulario("Permiso de visita", "Lote que visita", "1", "DNI del residente que autoriza", "",
                "DNI del visitante", "", "Hora desde (HH:MM)", "09:00", "Hora hasta (HH:MM)", "18:00");
        Lote l = c.lote(entero(d[0]));
        Residente r = c.residente(l, dni(d[1]));
        if (!l.puedeAutorizarIngreso(r)) throw new IllegalArgumentException("El residente no puede autorizar visitas.");
        int id = dni(d[2]);
        boolean proveedor = confirmar("¿La visita es un proveedor?");
        Persona existente = c.persona(id);
        Visitante v;
        if (existente != null) {
            if (!(existente instanceof Visitante) || (existente instanceof Proveedor) != proveedor)
                throw new IllegalArgumentException("El DNI ya existe con otro tipo de persona.");
            v = (Visitante) existente;
        } else {
            String[] p = formulario("Datos de la visita", "Nombre", "", "Apellido", "",
                    "Teléfono (8 dígitos)", "", "Email", "");
            v = proveedor ? new Proveedor(p[0], p[1], id, telefono(p[2]), correo(p[3]))
                    : new Visitante(p[0], p[1], id, telefono(p[2]), correo(p[3]));
        }
        LocalTime desde = hora(d[3]), hasta = hora(d[4]);
        if (!hasta.isAfter(desde)) throw new IllegalArgumentException("El egreso debe ser posterior al ingreso.");
        AutorizacionIngreso a;
        if (proveedor && confirmar("¿Permiso recurrente por varios días (máximo 30)?")) {
            String[] fechas = formulario("Vigencia del proveedor", "Fecha desde (AAAA-MM-DD)", LocalDate.now().toString(),
                    "Fecha hasta (AAAA-MM-DD)", LocalDate.now().plusDays(6).toString());
            a = new AutorizacionRecurrente(c.siguientePermiso(), v, l, r,
                    fecha(fechas[0]), fecha(fechas[1]), desde, hasta);
        } else {
            String f = pedir("Fecha autorizada (AAAA-MM-DD)", LocalDate.now().toString());
            a = new AutorizacionIngreso(c.siguientePermiso(), v, r, l, fecha(f), desde, hasta);
        }
        c.ingresos().agregarAutorizacion(a);
        mensaje("Autorización #" + a.getNumero() + " registrada.");
    }
    private void registrarIngreso() {
        String[] d = formulario("Registrar ingreso", "DNI del visitante", "", "Legajo del guardia", "");
        RegistroIngreso r = c.ingresos().registrarIngreso(c.visitante(dni(d[0])), c.guardia(entero(d[1])), LocalDateTime.now());
        mensaje("Ingreso #" + r.getNumero() + " registrado.");
    }
    private void registrarEgreso() {
        RegistroIngreso r = c.registro(entero(pedir("Número del registro de ingreso", "1")));
        c.ingresos().registrarEgreso(r, LocalDateTime.now());
        mensaje("Egreso registrado.");
    }
    private void consultarPermisos() { mostrarTexto(listado.getText()); }
    private String pendientes() {
        StringBuilder s = new StringBuilder();
        for (Reclamo r : c.reportes().generarReclamosPendientes(c.barrio().getReclamos()))
            s.append("#").append(r.getNumero()).append("  |  Lote ").append(r.getLote().getNumeroLote())
                    .append("  |  ").append(r.getEstado()).append("  |  ").append(r.getDescripcion()).append("\n");
        return s.length() == 0 ? "No hay reclamos pendientes.\n" : s.toString();
    }
    private String fueraServicio() {
        StringBuilder s = new StringBuilder();
        for (Equipo e : c.reportes().generarEquiposFueraServicio(c.barrio().getEquipos()))
            s.append(e.getCodigo()).append("  |  ").append(e.getSector()).append("\n");
        return s.length() == 0 ? "No hay equipos fuera de servicio.\n" : s.toString();
    }
    private void salir() {
        try {
            c.guardar();
            dispose();
        } catch (IOException e) {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "No se pudieron guardar los datos. ¿Salir de todos modos?\n" + e.getMessage(),
                    "Error al guardar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (respuesta == JOptionPane.YES_OPTION) dispose();
        }
    }
}
