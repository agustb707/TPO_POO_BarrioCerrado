import Archivos.*;
import Barrio.*;
import Enums.*;
import Equipos.*;
import Expensas.*;
import Ingresos.*;
import Notificaciones.*;
import Persona.*;
import Reclamos.*;
import Reportes.*;
import Excepciones.*;

import java.io.IOException;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MainConsola {

    private static final Scanner teclado = new Scanner(System.in);
    private static BarrioCerrado barrio;
    private static SistemaIngreso sistemaIngreso;
    private static GestorExpensas gestorExpensas;
    private static final Reporte reporte = new Reporte();
    private static final ArchivoLotes archivoLotes = new ArchivoLotes("Datos/Lotes.txt");
    private static List<Notificacion> avisosAdministrador;
    private static final ArchivoDatos archivoDatos = new ArchivoDatos("Datos/LosAromos.txt");

    public static void main(String[] args) {
        if (!cargarDatos()) {
            teclado.close();
            return;
        }
        int opcion;
        do {
            System.out.println("\n========== BARRIO LOS AROMOS ==========");
            System.out.println("1. Administrar lotes");
            System.out.println("2. Administrar reclamos");
            System.out.println("3. Administrar expensas");
            System.out.println("4. Administrar equipos");
            System.out.println("5. Administrar visitas");
            System.out.println("6. Ver reportes");
            System.out.println("0. Salir");
            opcion = leerEntero("Opcion: ");
            try {
                switch (opcion) {
                    case 1: menuLotes(); break;
                    case 2: menuReclamos(); break;
                    case 3: menuExpensas(); break;
                    case 4: menuEquipos(); break;
                    case 5: menuVisitas(); break;
                    case 6: menuReportes(); break;
                    case 0: guardarDatos(); System.out.println("Hasta luego."); break;
                    default: System.out.println("Opcion incorrecta.");
                }
            } catch (ReglaNegocioException e) {
                System.out.println("Regla de negocio: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Dato incorrecto: " + e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println("Operacion no permitida: " + e.getMessage());
            } catch (DateTimeParseException e) {
                System.out.println("Fecha u hora incorrecta. Use el formato solicitado.");
            }
        } while (opcion != 0);
        teclado.close();
    }

    // ==================== 1. LOTES Y PERSONAL ====================

    private static void menuLotes() {
        int opcion;
        do {
            System.out.println("\n--- ADMINISTRAR LOTES ---");
            System.out.println("1. Registrar lote con titular y residentes");
            System.out.println("2. Agregar familiar o inquilino a un lote");
            System.out.println("3. Autorizar residente para reclamos o visitas");
            System.out.println("4. Registrar personal del barrio");
            System.out.println("5. Consultar lotes");
            System.out.println("0. Volver");
            opcion = leerEntero("Opcion: ");
            try {
                switch (opcion) {
                    case 1: registrarLote(); break;
                    case 2: agregarResidente(); break;
                    case 3: autorizarResidente(); break;
                    case 4: registrarPersonal(); break;
                    case 5: mostrarLotes(); break;
                    case 0: break;
                    default: System.out.println("Opcion incorrecta.");
                }
                if (opcion != 0) guardarDatos();
            } catch (ReglaNegocioException e) {
                System.out.println("Regla de negocio: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Dato incorrecto: " + e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println("Operacion no permitida: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void registrarLote() {
        int numero = leerEntero("Numero de lote: ");
        Lote lote = barrio.buscarLote(numero);
        boolean nuevo = lote == null;
        if (nuevo) {
            double superficie = leerDouble("Superficie en m2: ");
            lote = new Lote(numero, superficie);
        } else if (lote.getTitular() != null) {
            System.out.println("El lote ya existe y tiene titular. Use 'Agregar residente'.");
            return;
        } else {
            System.out.println("El lote estaba cargado sin titular. Se completaran sus datos.");
        }

        System.out.println("--- Datos del titular ---");
        Titular titular = crearTitular();
        lote.asignarTitular(titular);
        titular.agregarLote(lote);

        while (leerSiNo("¿Agregar familiar o inquilino? (s/n): ")) {
            Residente residente = crearFamiliarOInquilino();
            lote.agregarResidente(residente);
            residente.agregarLote(lote);
            if (leerSiNo("¿Autorizarlo para presentar reclamos? (s/n): ")) {
                lote.autorizarReclamos(residente);
            }
            if (leerSiNo("¿Autorizarlo para permitir visitas? (s/n): ")) {
                lote.autorizarIngresos(residente);
            }
        }

        if (nuevo) {
            barrio.agregarLote(lote);
        }
        System.out.println("Lote registrado correctamente.");
    }

    private static void agregarResidente() {
        Lote lote = pedirLote();
        if (lote == null) return;
        if (lote.getTitular() == null) {
            System.out.println("Primero debe asignar un titular al lote.");
            return;
        }
        Residente residente = crearFamiliarOInquilino();
        lote.agregarResidente(residente);
        residente.agregarLote(lote);
        if (leerSiNo("¿Autorizar reclamos? (s/n): ")) lote.autorizarReclamos(residente);
        if (leerSiNo("¿Autorizar visitas? (s/n): ")) lote.autorizarIngresos(residente);
        System.out.println("Residente registrado en el lote.");
    }

    private static void autorizarResidente() {
        Lote lote = pedirLote();
        if (lote == null) return;
        List<Residente> residentes = lote.getResidentes();
        if (residentes.isEmpty()) {
            System.out.println("El lote no tiene familiares ni inquilinos registrados.");
            return;
        }
        for (int i = 0; i < residentes.size(); i++) {
            System.out.println((i + 1) + ". " + residentes.get(i).obtenerNombreCompleto());
        }
        int indice = leerEntero("Elegir residente: ") - 1;
        if (indice < 0 || indice >= residentes.size()) {
            System.out.println("Residente inexistente.");
            return;
        }
        int tipo = leerEntero("1. Autorizar reclamos  2. Autorizar visitas: ");
        if (tipo == 1) lote.autorizarReclamos(residentes.get(indice));
        else if (tipo == 2) lote.autorizarIngresos(residentes.get(indice));
        else { System.out.println("Opcion incorrecta."); return; }
        System.out.println("Autorizacion registrada.");
    }

    private static Titular crearTitular() {
        int dni = leerDni("DNI del titular: ");
        Persona existente = buscarPersonaPorDni(dni);
        if (existente instanceof Titular) {
            System.out.println("Titular existente: " + existente.obtenerNombreCompleto());
            return (Titular) existente;
        }
        if (existente != null) {
            throw new IllegalArgumentException("El DNI ya pertenece a una persona de otro tipo.");
        }
        String nombre = leerTexto("Nombre: ");
        String apellido = leerTexto("Apellido: ");
        int telefono = leerTelefono("Telefono (8 digitos, sin codigo de area): ");
        String email = leerEmail("Email: ");
        return new Titular(nombre, apellido, dni, telefono, email);
    }

    private static Residente crearFamiliarOInquilino() {
        int tipo = leerEntero("1. Familiar  2. Inquilino: ");
        if (tipo != 1 && tipo != 2) throw new IllegalArgumentException("Tipo de residente incorrecto.");
        int dni = leerDni("DNI: ");
        Persona existente = buscarPersonaPorDni(dni);
        if ((tipo == 1 && existente instanceof Familiar) ||
                (tipo == 2 && existente instanceof Inquilino)) {
            System.out.println("Residente existente: " + existente.obtenerNombreCompleto());
            return (Residente) existente;
        }
        if (existente != null) {
            throw new IllegalArgumentException("El DNI ya pertenece a una persona de otro tipo.");
        }
        String nombre = leerTexto("Nombre: ");
        String apellido = leerTexto("Apellido: ");
        int telefono = leerTelefono("Telefono (8 digitos, sin codigo de area): ");
        String email = leerEmail("Email: ");
        if (tipo == 1) return new Familiar(nombre, apellido, dni, telefono, email);
        return new Inquilino(nombre, apellido, dni, telefono, email);
    }

    private static void registrarPersonal() {
        System.out.println("\n--- REGISTRAR PERSONAL ---");
        System.out.println("1. Administrador");
        System.out.println("2. Guardia");
        System.out.println("3. Empleado de mantenimiento");
        int tipo = leerEntero("Opcion: ");
        if (tipo < 1 || tipo > 3) { System.out.println("Opcion incorrecta."); return; }
        if (tipo == 1 && barrio.getAdministrador() != null) {
            System.out.println("Ya hay un administrador registrado.");
            return;
        }
        String nombre = leerTexto("Nombre: ");
        String apellido = leerTexto("Apellido: ");
        int dni = leerDni("DNI: ");
        if (buscarPersonaPorDni(dni) != null) {
            throw new IllegalArgumentException("Ya existe una persona con ese DNI.");
        }
        int telefono = leerTelefono("Telefono (8 digitos, sin codigo de area): ");
        String email = leerEmail("Email: ");
        int legajo = leerEntero("Legajo: ");
        LocalDate fecha = leerFechaIngresoPersonal();

        Personal personal;
        if (tipo == 1) {
            personal = new Administrador(nombre, apellido, dni, telefono, email, legajo, fecha);
        } else if (tipo == 2) {
            Turno turno = elegirEnum(Turno.values(), "Turno");
            Puesto puesto = elegirEnum(Puesto.values(), "Puesto");
            personal = new Guardia(nombre, apellido, dni, telefono, email,
                    legajo, fecha, turno, puesto, sistemaIngreso);
        } else {
            Especialidad especialidad = elegirEnum(Especialidad.values(), "Especialidad");
            personal = new EmpleadoMantenimiento(nombre, apellido, dni,
                    telefono, email, legajo, fecha, especialidad);
        }
        barrio.agregarPersonal(personal);
        if (personal instanceof Administrador) {
            barrio.asignarAdministrador((Administrador) personal);
            ((Administrador) personal).setBarrio(barrio);
        }
        System.out.println("Personal registrado correctamente.");
    }

    private static void mostrarLotes() {
        if (barrio.getLotes().isEmpty()) { System.out.println("No hay lotes."); return; }
        for (Lote lote : barrio.getLotes()) {
            String nombreTitular = lote.getTitular() == null ? "Sin titular"
                    : lote.getTitular().obtenerNombreCompleto();
            System.out.println("Lote " + lote.getNumeroLote() + " | " + lote.getSuperficie()
                    + " m2 | Titular: " + nombreTitular + " | Residentes: "
                    + lote.getResidentes().size());
        }
    }

    // ==================== 2. RECLAMOS ====================

    private static void menuReclamos() {
        int opcion;
        do {
            System.out.println("\n--- ADMINISTRAR RECLAMOS ---");
            System.out.println("1. Registrar reclamo (con empleado y comentario)");
            System.out.println("2. Consultar y gestionar reclamo");
            System.out.println("3. Listar reclamos");
            System.out.println("0. Volver");
            opcion = leerEntero("Opcion: ");
            try {
                switch (opcion) {
                    case 1: registrarReclamo(); break;
                    case 2: gestionarReclamo(); break;
                    case 3: listarReclamos(); break;
                    case 0: break;
                    default: System.out.println("Opcion incorrecta.");
                }
                if (opcion != 0) guardarDatos();
            } catch (ReglaNegocioException e) {
                System.out.println("Regla de negocio: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Dato incorrecto: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void registrarReclamo() {
        if (barrio.getAdministrador() == null) {
            System.out.println("No hay administradores existentes, registre al menos uno para continuar");
            return;
        }
        Lote lote = pedirLote();
        if (lote == null) return;
        Residente residente = elegirResidente(lote);
        if (residente == null) return;
        TipoReclamo tipo = elegirEnum(TipoReclamo.values(), "Tipo de reclamo");
        String descripcion = leerTexto("Descripcion del reclamo: ");
        Personal empleado = elegirEmpleadoReclamo();
        if (empleado == null) return;
        Reclamo reclamo = residente.presentarReclamo(lote, tipo, descripcion);
        barrio.getAdministrador().asignarReclamo(reclamo, empleado);
        barrio.agregarReclamo(reclamo);
        System.out.println("Reclamo " + reclamo.getNumero() + " | Prioridad: " + reclamo.getPrioridad());
        System.out.println("Asignado a: " + empleado.obtenerNombreCompleto());
        if (leerSiNo("¿Poner el reclamo EN_CURSO? (s/n): ")) {
            reclamo.cambiarEstado(EstadoReclamo.EN_CURSO, leerTexto("Observacion: "));
        }
        if (leerSiNo("¿Agregar comentario del vecino? (s/n): ")) {
            residente.comentarReclamo(reclamo, leerTexto("Comentario: "));
        }
        System.out.println("Reclamo registrado.");
    }

    private static void gestionarReclamo() {
        Reclamo reclamo = pedirReclamo();
        if (reclamo == null) return;
        reclamo.cerrarSiVencioPlazo(LocalDateTime.now());
        mostrarDetalleReclamo(reclamo);
        System.out.println("1. Cambiar estado");
        System.out.println("2. Asignar o cambiar empleado");
        System.out.println("3. Agregar comentario");
        System.out.println("4. Reabrir reclamo resuelto");
        System.out.println("5. Ver historial de estados");
        System.out.println("0. Volver");
        int opcion = leerEntero("Opcion: ");
        switch (opcion) {
            case 1:
                EstadoReclamo estado = elegirEnum(EstadoReclamo.values(), "Nuevo estado");
                reclamo.cambiarEstado(estado, leerTexto("Observacion: "));
                System.out.println("Estado actualizado.");
                break;
            case 2:
                if (barrio.getAdministrador() == null) { System.out.println("Falta el administrador."); return; }
                Personal empleado = elegirEmpleadoReclamo();
                if (empleado != null) barrio.getAdministrador().asignarReclamo(reclamo, empleado);
                break;
            case 3:
                System.out.println("1. Comentario del vecino  2. Comentario del personal");
                int autor = leerEntero("Opcion: ");
                if (autor == 1) {
                    Residente residente = elegirResidente(reclamo.getLote());
                    if (residente != null) residente.comentarReclamo(reclamo, leerTexto("Comentario: "));
                } else if (autor == 2) {
                    Personal personal = elegirPersonal();
                    if (personal != null) personal.comentarReclamo(reclamo, leerTexto("Comentario: "));
                } else System.out.println("Opcion incorrecta.");
                break;
            case 4:
                Residente solicitante = elegirResidente(reclamo.getLote());
                if (solicitante != null) {
                    reclamo.reabrir(solicitante, LocalDateTime.now());
                    System.out.println("Reclamo reabierto.");
                }
                break;
            case 5:
                for (HistorialEstado historial : reclamo.consultarHistorial()) {
                    System.out.println(historial.getFechaCambio() + " | "
                            + historial.getEstadoNuevo() + " | " + historial.getObservacion());
                }
                break;
            case 0: break;
            default: System.out.println("Opcion incorrecta.");
        }
    }

    private static void mostrarDetalleReclamo(Reclamo reclamo) {
        System.out.println("Reclamo: " + reclamo.getNumero() + " | Lote: "
                + reclamo.getLote().getNumeroLote() + " | Tipo: " + reclamo.getTipo()
                + " | Prioridad: " + reclamo.getPrioridad() + " | Estado: " + reclamo.getEstado());
        System.out.println("Descripcion: " + reclamo.getDescripcion());
        System.out.println("Responsable: " + (reclamo.getResponsable() == null ? "Sin asignar"
                : reclamo.getResponsable().obtenerNombreCompleto()));
        for (Comentario comentario : reclamo.getComentarios()) {
            System.out.println("Comentario de " + comentario.getAutor().obtenerNombreCompleto()
                    + ": " + comentario.getTexto());
        }
    }

    private static void listarReclamos() {
        if (barrio.getReclamos().isEmpty()) { System.out.println("No hay reclamos."); return; }
        for (Reclamo reclamo : barrio.getReclamos()) mostrarDetalleReclamo(reclamo);
    }

    // ==================== 3. EXPENSAS ====================

    private static void menuExpensas() {
        int opcion;
        do {
            System.out.println("\n--- ADMINISTRAR EXPENSAS ---");
            System.out.println("1. Registrar presupuesto y emitir expensas del mes");
            System.out.println("2. Consultar expensas por lote");
            System.out.println("3. Registrar pago total o parcial");
            System.out.println("4. Corregir pago mediante ajuste");
            System.out.println("5. Consultar deudas");
            System.out.println("0. Volver");
            opcion = leerEntero("Opcion: ");
            try {
                switch (opcion) {
                    case 1: emitirExpensas(); break;
                    case 2: consultarExpensas(); break;
                    case 3: registrarPago(); break;
                    case 4: corregirPago(); break;
                    case 5: consultarDeudas(); break;
                    case 0: break;
                    default: System.out.println("Opcion incorrecta.");
                }
                if (opcion != 0) guardarDatos();
            } catch (ReglaNegocioException e) {
                System.out.println("Regla de negocio: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Dato incorrecto: " + e.getMessage());
            } catch (DateTimeParseException e) {
                System.out.println("Fecha incorrecta.");
            }
        } while (opcion != 0);
    }

    private static void emitirExpensas() {
        YearMonth periodo = leerPeriodo("Periodo (AAAA-MM): ");
        double importe = leerDouble("Presupuesto total del mes: ");
        barrio.registrarPresupuesto(periodo, importe);
        List<Expensa> nuevas = gestorExpensas.emitirExpensas(periodo);
        System.out.println("Se generaron " + nuevas.size() + " expensas.");
        for (Expensa expensa : nuevas) {
            System.out.println("Lote " + expensa.getLote().getNumeroLote()
                    + " | Importe: $" + dinero(expensa.getCapital()));
        }
    }

    private static void consultarExpensas() {
        Lote lote = pedirLote();
        if (lote == null) return;
        if (lote.getExpensas().isEmpty()) {
            System.out.println("El lote no tiene expensas.");
            return;
        }
        for (Expensa expensa : lote.getExpensas()) {
            expensa.actualizarEstado(LocalDate.now());
            System.out.println("Expensa " + expensa.getNumero() + " | Periodo " + expensa.getPeriodo()
                    + " | Capital $" + dinero(expensa.getCapital()) + " | Interes $"
                    + dinero(expensa.calcularInteresMora(LocalDate.now())) + " | Deuda total $"
                    + dinero(expensa.calcularDeuda(LocalDate.now())) + " | Estado " + expensa.getEstado());
        }
    }

    private static void registrarPago() {
        Lote lote = pedirLote();
        if (lote == null) return;
        consultarDeudaLote(lote);
        double importe = leerDouble("Importe a pagar: ");
        MedioPago medio = elegirEnum(MedioPago.values(), "Medio de pago");
        LocalDate fecha = leerFecha("Fecha de pago (AAAA-MM-DD): ");
        Pago pago = gestorExpensas.registrarPago(lote, fecha, importe, medio);
        System.out.println("Pago " + pago.getNumero() + " registrado. Importe: $" + dinero(pago.getImporte()));
    }

    private static void corregirPago() {
        List<Pago> pagos = gestorExpensas.getPagos();
        if (pagos.isEmpty()) { System.out.println("No hay pagos registrados."); return; }
        for (Pago pago : pagos) {
            System.out.println("Pago " + pago.getNumero() + " | Fecha: " + pago.getFecha()
                    + " | Importe original: $" + dinero(pago.getImporte())
                    + " | Neto: $" + dinero(pago.obtenerImporteNeto()));
        }
        int numero = leerEntero("Numero de pago a corregir: ");
        Pago elegido = null;
        for (Pago pago : pagos) if (pago.getNumero() == numero) elegido = pago;
        if (elegido == null) { System.out.println("Pago inexistente."); return; }
        System.out.println("Ingrese importe negativo para revertir o positivo para agregar.");
        double importe = leerDouble("Importe del ajuste (con signo): ");
        String motivo = leerTexto("Motivo: ");
        LocalDate fecha = leerFecha("Fecha del ajuste (AAAA-MM-DD): ");
        gestorExpensas.registrarAjuste(elegido, new AjustePago(fecha, importe, motivo));
        System.out.println("Ajuste registrado. Nuevo importe neto: $" + dinero(elegido.obtenerImporteNeto()));
    }

    private static void consultarDeudas() {
        Map<Lote, Double> deudas = reporte.generarMorosidadPorLote(barrio.getLotes());
        if (deudas.isEmpty()) System.out.println("No hay deudas.");
        for (Map.Entry<Lote, Double> entrada : deudas.entrySet()) {
            Lote lote = entrada.getKey();
            lote.actualizarMorosidad();
            System.out.println("Lote " + lote.getNumeroLote() + " | Deuda: $"
                    + dinero(entrada.getValue()) + " | Moroso: " + (lote.isMoroso() ? "SI" : "NO"));
        }
    }

    private static void consultarDeudaLote(Lote lote) {
        double total = 0;
        for (Expensa expensa : lote.getExpensas()) total += expensa.calcularDeuda(LocalDate.now());
        System.out.println("Deuda actual aproximada del lote: $" + dinero(total));
    }

    // ==================== 4. EQUIPOS ====================

    private static void menuEquipos() {
        int opcion;
        do {
            System.out.println("\n--- ADMINISTRAR EQUIPOS ---");
            System.out.println("1. Registrar camara o luminaria");
            System.out.println("2. Registrar falla y crear orden de mantenimiento");
            System.out.println("3. Consultar o cambiar estado de equipo");
            System.out.println("4. Registrar reparacion y cerrar orden");
            System.out.println("5. Consultar avisos al administrador");
            System.out.println("0. Volver");
            opcion = leerEntero("Opcion: ");
            try {
                switch (opcion) {
                    case 1: registrarEquipo(); break;
                    case 2: registrarFallaYOrden(); break;
                    case 3: gestionarEquipo(); break;
                    case 4: registrarReparacion(); break;
                    case 5: mostrarAvisosAdministrador(); break;
                    case 0: break;
                    default: System.out.println("Opcion incorrecta.");
                }
                if (opcion != 0) guardarDatos();
            } catch (ReglaNegocioException e) {
                System.out.println("Regla de negocio: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Dato incorrecto: " + e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println("Operacion no permitida: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void registrarEquipo() {
        int tipo = leerEntero("1. Camara  2. Luminaria: ");
        if (tipo != 1 && tipo != 2) { System.out.println("Tipo incorrecto."); return; }
        String codigo = leerTexto("Codigo del equipo: ");
        Sector sector = elegirEnum(Sector.values(), "Sector");
        LocalDate fecha = leerFecha("Fecha de instalacion (AAAA-MM-DD): ");
        Equipo equipo;
        if (tipo == 1) {
            Resolucion resolucion = elegirEnum(Resolucion.values(), "Resolucion");
            int dias = leerEntero("Dias de grabacion: ");
            equipo = new Camara(codigo, sector, fecha, resolucion, dias);
        } else {
            TipoLuminaria lampara = elegirEnum(TipoLuminaria.values(), "Tipo de lampara");
            double potencia = leerDouble("Potencia: ");
            equipo = new Luminaria(codigo, sector, fecha, lampara, potencia);
        }
        barrio.registrarEquipo(equipo);
        System.out.println("Equipo registrado.");
    }

    private static void registrarFallaYOrden() {
        Equipo equipo = pedirEquipo();
        if (equipo == null) return;
        System.out.println("1. Reporta un residente  2. Reporta un guardia");
        int tipo = leerEntero("Opcion: ");
        Falla falla;
        String descripcion = leerTexto("Descripcion de la falla: ");
        if (tipo == 1) {
            Lote lote = pedirLote();
            if (lote == null) return;
            Residente residente = elegirResidente(lote);
            if (residente == null) return;
            falla = residente.registrarFalla(equipo, descripcion);
        } else if (tipo == 2) {
            Guardia guardia = elegirGuardia();
            if (guardia == null) return;
            falla = guardia.registrarFalla(equipo, descripcion);
        } else { System.out.println("Opcion incorrecta."); return; }
        System.out.println("Falla " + falla.getNumero() + " registrada.");
        if (leerSiNo("¿Crear orden de mantenimiento ahora? (s/n): ")) {
            crearOrdenParaFalla(falla);
        }
    }

    private static void crearOrdenParaFalla(Falla falla) {
        if (barrio.getAdministrador() == null) {
            System.out.println("No hay administradores existentes, registre al menos uno para continuar");
            return;
        }
        EmpleadoMantenimiento tecnico = elegirTecnico();
        if (tecnico == null) return;
        String trabajo = leerTexto("Descripcion del trabajo: ");
        OrdenMantenimiento orden = barrio.getAdministrador()
                .generarOrdenMantenimiento(falla, tecnico, trabajo);
        System.out.println("Orden " + orden.getNumero() + " creada | Prioridad " + orden.getPrioridad());
    }

    private static void gestionarEquipo() {
        Equipo equipo = pedirEquipo();
        if (equipo == null) return;
        System.out.println(equipo);
        System.out.println("Fallas: " + equipo.getFallas().size()
                + " | Ordenes: " + equipo.getOrdenes().size());
        boolean reparacionCerrada = false;
        for (OrdenMantenimiento orden : equipo.getOrdenes()) {
            if (!orden.estaAbierta() && orden.getFechaReparacion() != null) {
                reparacionCerrada = true;
            }
        }
        if (reparacionCerrada) {
            System.out.println("Este equipo tiene una reparacion cerrada. No se puede cambiar manualmente su estado.");
        } else if (leerSiNo("¿Cambiar estado? (s/n): ")) {
            EstadoEquipo estado = elegirEnum(EstadoEquipo.values(), "Estado");
            equipo.cambiarEstado(estado);
            System.out.println("Estado actualizado a " + equipo.getEstado());
        }
        for (Falla falla : equipo.getFallas()) {
            if (falla.getOrden() == null && leerSiNo(
                    "Falla " + falla.getNumero() + " sin orden. ¿Crear una? (s/n): ")) {
                crearOrdenParaFalla(falla);
            }
        }
    }

    private static void registrarReparacion() {
        List<OrdenMantenimiento> ordenes = new ArrayList<>();
        for (Equipo equipo : barrio.getEquipos()) {
            for (OrdenMantenimiento orden : equipo.getOrdenes()) {
                if (orden.estaAbierta()) ordenes.add(orden);
            }
        }
        if (ordenes.isEmpty()) { System.out.println("No hay ordenes abiertas."); return; }
        for (OrdenMantenimiento orden : ordenes) {
            System.out.println("Orden " + orden.getNumero() + " | Equipo " + orden.getEquipo().getCodigo()
                    + " | Tecnico " + (orden.getTecnico() == null ? "Sin asignar"
                    : orden.getTecnico().obtenerNombreCompleto()));
        }
        int numero = leerEntero("Numero de orden: ");
        OrdenMantenimiento elegida = null;
        for (OrdenMantenimiento orden : ordenes) if (orden.getNumero() == numero) elegida = orden;
        if (elegida == null) { System.out.println("Orden inexistente."); return; }
        EmpleadoMantenimiento tecnico = elegida.getTecnico();
        if (tecnico == null) { System.out.println("La orden necesita un tecnico asignado."); return; }
        String descripcion = leerTexto("Descripcion de la reparacion: ");
        LocalDate fecha = leerFecha("Fecha de reparacion (AAAA-MM-DD): ");
        tecnico.registrarReparacion(elegida, descripcion, fecha);
        elegida.cerrarOrden(fecha);
        System.out.println("Reparacion registrada y orden cerrada. Estado del equipo: "
                + elegida.getEquipo().getEstado());
    }

    private static void mostrarAvisosAdministrador() {
        if (barrio.getAdministrador() == null) { System.out.println("Falta el administrador."); return; }
        List<Notificacion> nuevos = Notificacion.generarAvisosAdministrador(barrio);
        for (Notificacion aviso : nuevos) {
            boolean repetido = false;
            for (Notificacion anterior : avisosAdministrador) {
                if (anterior.getMotivo() == aviso.getMotivo()
                        && anterior.getMensaje().equals(aviso.getMensaje())) repetido = true;
            }
            if (!repetido) avisosAdministrador.add(aviso);
        }
        if (avisosAdministrador.isEmpty()) System.out.println("No hay avisos al administrador.");
        for (Notificacion aviso : avisosAdministrador) System.out.println(aviso.generarAviso());
    }

    // ==================== 5. VISITAS ====================

    private static void menuVisitas() {
        int opcion;
        do {
            System.out.println("\n--- ADMINISTRAR VISITAS ---");
            System.out.println("1. Registrar permiso (visitante o proveedor)");
            System.out.println("2. Registrar ingreso");
            System.out.println("3. Registrar egreso");
            System.out.println("4. Consultar ingresos y egresos");
            System.out.println("0. Volver");
            opcion = leerEntero("Opcion: ");
            try {
                switch (opcion) {
                    case 1: registrarPermiso(); break;
                    case 2: registrarIngreso(); break;
                    case 3: registrarEgreso(); break;
                    case 4: listarIngresos(); break;
                    case 0: break;
                    default: System.out.println("Opcion incorrecta.");
                }
                if (opcion != 0) guardarDatos();
            } catch (ReglaNegocioException e) {
                System.out.println("Regla de negocio: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Dato incorrecto: " + e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println("Operacion no permitida: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private static void registrarPermiso() {
        Lote lote = pedirLote();
        if (lote == null) return;
        Residente autorizante = elegirResidente(lote);
        if (autorizante == null) return;
        boolean proveedor = leerSiNo("¿La visita es un proveedor? (s/n): ");
        int dni = leerDni("DNI del visitante: ");
        Persona existente = buscarPersonaPorDni(dni);
        Visitante visitante;
        if (existente != null) {
            if (!(existente instanceof Visitante) ||
                    proveedor != (existente instanceof Proveedor)) {
                throw new IllegalArgumentException("El DNI ya esta registrado con otro tipo de persona.");
            }
            visitante = (Visitante) existente;
            System.out.println("Visitante existente: " + visitante.obtenerNombreCompleto());
        } else {
            String nombre = leerTexto("Nombre del visitante: ");
            String apellido = leerTexto("Apellido: ");
            int telefono = leerTelefono("Telefono (8 digitos, sin codigo de area): ");
            String email = leerEmail("Email: ");
            visitante = proveedor
                    ? new Proveedor(nombre, apellido, dni, telefono, email)
                    : new Visitante(nombre, apellido, dni, telefono, email);
        }

        int numero = siguienteAutorizacion();
        LocalTime desde = leerHora("Hora desde (HH:MM): ");
        LocalTime hasta = leerHora("Hora hasta (HH:MM): ");
        if (!lote.puedeAutorizarIngreso(autorizante)) {
            throw new ReglaNegocioException("El residente elegido no esta autorizado para permitir visitas.");
        }
        if (!hasta.isAfter(desde)) {
            throw new IllegalArgumentException("La hora de salida debe ser posterior a la hora de ingreso.");
        }
        AutorizacionIngreso autorizacion;
        if (proveedor && leerSiNo("¿Permiso recurrente por varios dias? (s/n): ")) {
            LocalDate inicio = leerFecha("Fecha inicio (AAAA-MM-DD): ");
            LocalDate fin = leerFecha("Fecha fin (AAAA-MM-DD): ");
            if (fin.isBefore(inicio)) {
                throw new IllegalArgumentException("La fecha de fin debe ser posterior o igual a la fecha de inicio.");
            }
            autorizacion = new AutorizacionRecurrente(numero, visitante, lote,
                    autorizante, inicio, fin, desde, hasta);
        } else {
            LocalDate fecha = leerFecha("Fecha autorizada (AAAA-MM-DD): ");
            autorizacion = new AutorizacionIngreso(numero, visitante, autorizante,
                    lote, fecha, desde, hasta);
        }
        sistemaIngreso.agregarAutorizacion(autorizacion);
        System.out.println("Permiso " + autorizacion.getNumero() + " registrado.");
    }

    private static void registrarIngreso() {
        Guardia guardia = elegirGuardia();
        if (guardia == null) return;
        int dni = leerDni("DNI del visitante: ");
        Visitante visitante = buscarVisitantePorDni(dni);
        if (visitante == null) { System.out.println("El visitante no tiene permisos registrados."); return; }
        RegistroIngreso registro = guardia.registrarIngreso(visitante, LocalDateTime.now());
        System.out.println("Ingreso " + registro.getNumero() + " registrado a las "
                + registro.getFechaHoraIngreso());
    }

    private static void registrarEgreso() {
        List<RegistroIngreso> abiertos = new ArrayList<>();
        for (RegistroIngreso registro : sistemaIngreso.getRegistros()) {
            if (registro.estaDentro()) {
                abiertos.add(registro);
                System.out.println("Registro " + registro.getNumero() + " | DNI "
                        + registro.getAutorizacion().getVisitante().getDni()
                        + " | Ingreso " + registro.getFechaHoraIngreso());
            }
        }
        if (abiertos.isEmpty()) { System.out.println("No hay visitantes dentro."); return; }
        int numero = leerEntero("Numero de registro para egreso: ");
        for (RegistroIngreso registro : abiertos) {
            if (registro.getNumero() == numero) {
                sistemaIngreso.registrarEgreso(registro, LocalDateTime.now());
                System.out.println("Egreso registrado.");
                return;
            }
        }
        System.out.println("Registro inexistente.");
    }

    private static void listarIngresos() {
        if (sistemaIngreso.getRegistros().isEmpty()) {
            System.out.println("No hay ingresos."); return;
        }
        for (RegistroIngreso registro : sistemaIngreso.getRegistros()) {
            System.out.println("Registro " + registro.getNumero() + " | Visitante: "
                    + registro.getAutorizacion().getVisitante().obtenerNombreCompleto()
                    + " | Ingreso: " + registro.getFechaHoraIngreso()
                    + " | Egreso: " + (registro.estaDentro() ? "Aun dentro" : registro.getFechaHoraEgreso()));
        }
    }

    // ==================== 6. REPORTES ====================

    private static void menuReportes() {
        int opcion;
        do {
            System.out.println("\n--- REPORTES ---");
            System.out.println("1. Ver solicitudes y reclamos pendientes");
            System.out.println("2. Ver lotes con deudas");
            System.out.println("3. Equipos fuera de servicio");
            System.out.println("0. Volver");
            opcion = leerEntero("Opcion: ");
            switch (opcion) {
                case 1:
                    List<Reclamo> reclamos = reporte.generarReclamosPendientes(barrio.getReclamos());
                    if (reclamos.isEmpty()) System.out.println("No hay reclamos pendientes.");
                    for (Reclamo reclamo : reclamos) mostrarDetalleReclamo(reclamo);
                    break;
                case 2:
                    Map<Lote, Double> deudas = reporte.generarMorosidadPorLote(barrio.getLotes());
                    if (deudas.isEmpty()) System.out.println("No hay lotes con deuda.");
                    for (Map.Entry<Lote, Double> entrada : deudas.entrySet()) {
                        System.out.println("Lote " + entrada.getKey().getNumeroLote()
                                + " | Deuda $" + dinero(entrada.getValue()));
                    }
                    break;
                case 3:
                    List<Equipo> equipos = reporte.generarEquiposFueraServicio(barrio.getEquipos());
                    if (equipos.isEmpty()) System.out.println("No hay equipos fuera de servicio.");
                    for (Equipo equipo : equipos) System.out.println(equipo);
                    break;
                case 0: break;
                default: System.out.println("Opcion incorrecta.");
            }
        } while (opcion != 0);
    }

    // ==================== BUSQUEDAS ====================

    private static Persona buscarPersonaPorDni(int dni) {
        for (Lote lote : barrio.getLotes()) {
            if (lote.getTitular() != null && lote.getTitular().getDni() == dni) {
                return lote.getTitular();
            }
            for (Residente residente : lote.getResidentes()) {
                if (residente.getDni() == dni) return residente;
            }
        }
        for (Personal personal : barrio.getPersonal()) {
            if (personal.getDni() == dni) return personal;
        }
        for (AutorizacionIngreso autorizacion : sistemaIngreso.getAutorizaciones()) {
            if (autorizacion.getVisitante().getDni() == dni) {
                return autorizacion.getVisitante();
            }
        }
        return null;
    }

    private static Lote pedirLote() {
        int numero = leerEntero("Numero de lote: ");
        Lote lote = barrio.buscarLote(numero);
        if (lote == null) System.out.println("No existe el lote.");
        return lote;
    }

    private static Residente elegirResidente(Lote lote) {
        List<Residente> personas = new ArrayList<>();
        if (lote.getTitular() != null) personas.add(lote.getTitular());
        personas.addAll(lote.getResidentes());
        if (personas.isEmpty()) { System.out.println("El lote no tiene residentes."); return null; }
        for (int i = 0; i < personas.size(); i++) {
            System.out.println((i + 1) + ". " + personas.get(i).obtenerNombreCompleto());
        }
        int indice = leerEntero("Seleccione residente: ") - 1;
        if (indice < 0 || indice >= personas.size()) {
            System.out.println("Residente inexistente."); return null;
        }
        return personas.get(indice);
    }

    private static Personal elegirPersonal() {
        List<Personal> personal = barrio.getPersonal();
        if (personal.isEmpty()) { System.out.println("No hay personal registrado."); return null; }
        for (int i = 0; i < personal.size(); i++) {
            System.out.println((i + 1) + ". " + personal.get(i).obtenerNombreCompleto()
                    + " | Legajo " + personal.get(i).getLegajo());
        }
        int indice = leerEntero("Seleccione personal: ") - 1;
        if (indice < 0 || indice >= personal.size()) {
            System.out.println("Personal inexistente."); return null;
        }
        return personal.get(indice);
    }

    private static Personal elegirEmpleadoReclamo() {
        List<Personal> empleados = new ArrayList<>();
        for (Personal personal : barrio.getPersonal()) {
            if (!(personal instanceof Administrador)) empleados.add(personal);
        }
        if (empleados.isEmpty()) { System.out.println("No hay empleados. Registrelos en Administrar lotes."); return null; }
        for (int i = 0; i < empleados.size(); i++) {
            System.out.println((i + 1) + ". " + empleados.get(i).obtenerNombreCompleto()
                    + " | Legajo " + empleados.get(i).getLegajo());
        }
        int indice = leerEntero("Seleccione empleado: ") - 1;
        if (indice < 0 || indice >= empleados.size()) {
            System.out.println("Empleado inexistente."); return null;
        }
        return empleados.get(indice);
    }

    private static Guardia elegirGuardia() {
        List<Guardia> guardias = new ArrayList<>();
        for (Personal personal : barrio.getPersonal()) {
            if (personal instanceof Guardia) guardias.add((Guardia) personal);
        }
        if (guardias.isEmpty()) { System.out.println("No hay guardias. Registre uno en Administrar lotes."); return null; }
        for (int i = 0; i < guardias.size(); i++) {
            System.out.println((i + 1) + ". " + guardias.get(i).obtenerNombreCompleto());
        }
        int indice = leerEntero("Seleccione guardia: ") - 1;
        if (indice < 0 || indice >= guardias.size()) {
            System.out.println("Guardia inexistente."); return null;
        }
        return guardias.get(indice);
    }

    private static EmpleadoMantenimiento elegirTecnico() {
        List<EmpleadoMantenimiento> tecnicos = new ArrayList<>();
        for (Personal personal : barrio.getPersonal()) {
            if (personal instanceof EmpleadoMantenimiento) tecnicos.add((EmpleadoMantenimiento) personal);
        }
        if (tecnicos.isEmpty()) { System.out.println("No hay tecnicos. Registre uno en Administrar lotes."); return null; }
        for (int i = 0; i < tecnicos.size(); i++) {
            System.out.println((i + 1) + ". " + tecnicos.get(i).obtenerNombreCompleto());
        }
        int indice = leerEntero("Seleccione tecnico: ") - 1;
        if (indice < 0 || indice >= tecnicos.size()) {
            System.out.println("Tecnico inexistente."); return null;
        }
        return tecnicos.get(indice);
    }

    private static Equipo pedirEquipo() {
        String codigo = leerTexto("Codigo de equipo: ");
        for (Equipo equipo : barrio.getEquipos()) {
            if (equipo.getCodigo().equals(codigo)) return equipo;
        }
        System.out.println("No existe el equipo.");
        return null;
    }

    private static Reclamo pedirReclamo() {
        int numero = leerEntero("Numero de reclamo: ");
        for (Reclamo reclamo : barrio.getReclamos()) if (reclamo.getNumero() == numero) return reclamo;
        System.out.println("No existe el reclamo.");
        return null;
    }

    private static Visitante buscarVisitantePorDni(int dni) {
        for (AutorizacionIngreso autorizacion : sistemaIngreso.getAutorizaciones()) {
            if (autorizacion.getVisitante().getDni() == dni) return autorizacion.getVisitante();
        }
        return null;
    }

    private static int siguienteAutorizacion() {
        int siguiente = 1;
        for (AutorizacionIngreso autorizacion : sistemaIngreso.getAutorizaciones()) {
            if (autorizacion.getNumero() >= siguiente) siguiente = autorizacion.getNumero() + 1;
        }
        return siguiente;
    }

    // ==================== ENTRADA POR TECLADO ====================

    private static String dinero(double importe) {
        return String.format(java.util.Locale.US, "%.2f", importe);
    }

    private static String leerEmail(String mensaje) {
        while (true) {
            String email = leerTexto(mensaje);
            int arroba = email.indexOf('@');
            int punto = email.indexOf('.', arroba + 1);
            if (arroba > 0 && punto > arroba + 1 && punto < email.length() - 1
                    && email.indexOf('@', arroba + 1) == -1) return email;
            System.out.println("El email debe contener un @ y un punto despues.");
        }
    }

    private static int leerDni(String mensaje) {
        while (true) {
            int numero = leerEntero(mensaje);
            if (numero >= 1000000 && numero <= 999999999) return numero;
            System.out.println("El DNI debe tener de 7 a 9 digitos.");
        }
    }

    private static int leerTelefono(String mensaje) {
        while (true) {
            int numero = leerEntero(mensaje);
            if (numero >= 10000000 && numero <= 99999999) return numero;
            System.out.println("El telefono debe tener exactamente 8 digitos, sin codigo de area.");
        }
    }

    private static LocalDate leerFechaIngresoPersonal() {
        while (true) {
            LocalDate fecha = leerFecha("Fecha de ingreso laboral (AAAA-MM-DD): ");
            if (fecha.isBefore(LocalDate.now())) return fecha;
            System.out.println("La fecha de ingreso debe ser anterior a la fecha actual.");
        }
    }

    private static String leerTexto(String mensaje) {
        String texto;
        do {
            System.out.print(mensaje);
            texto = teclado.nextLine().trim();
            if (texto.isEmpty()) System.out.println("Este dato es obligatorio.");
        } while (texto.isEmpty());
        return texto;
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return Integer.parseInt(teclado.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero entero.");
            }
        }
    }

    private static double leerDouble(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                String texto = teclado.nextLine().trim().replace(',', '.');
                if (!texto.matches("-?[0-9]+(\\.[0-9]{1,2})?")) {
                    System.out.println("Ingrese un numero con maximo 2 decimales.");
                    continue;
                }
                double numero = Double.parseDouble(texto);
                if (!Double.isFinite(numero)) {
                    System.out.println("Numero invalido.");
                    continue;
                }
                return numero;
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero valido.");
            }
        }
    }

    private static LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return LocalDate.parse(teclado.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Use AAAA-MM-DD. Ejemplo: 2026-11-05");
            }
        }
    }

    private static LocalTime leerHora(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return LocalTime.parse(teclado.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Use HH:MM. Ejemplo: 14:30");
            }
        }
    }

    private static YearMonth leerPeriodo(String mensaje) {
        while (true) {
            try {
                System.out.print(mensaje);
                return YearMonth.parse(teclado.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Use AAAA-MM. Ejemplo: 2026-11");
            }
        }
    }

    private static boolean leerSiNo(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = teclado.nextLine().trim();
            if (texto.equalsIgnoreCase("s") || texto.equalsIgnoreCase("si")) return true;
            if (texto.equalsIgnoreCase("n") || texto.equalsIgnoreCase("no")) return false;
            System.out.println("Responda s o n.");
        }
    }

    private static <T extends Enum<T>> T elegirEnum(T[] valores, String titulo) {
        System.out.println(titulo + ":");
        for (int i = 0; i < valores.length; i++) {
            System.out.println((i + 1) + ". " + valores[i]);
        }
        while (true) {
            int opcion = leerEntero("Opcion: ");
            if (opcion >= 1 && opcion <= valores.length) return valores[opcion - 1];
            System.out.println("Opcion incorrecta.");
        }
    }

    // ==================== PERSISTENCIA COMPLETA ====================

    private static boolean cargarDatos() {
        try {
            if (archivoDatos.existe()) {
                DatosSistema datos = archivoDatos.cargar();
                barrio = datos.getBarrio();
                gestorExpensas = datos.getGestorExpensas();
                sistemaIngreso = datos.getSistemaIngreso();
                avisosAdministrador = datos.getAvisosAdministrador();

                // Las listas y contadores static se reconstruyen tras leer el archivo.
                Reclamo.restaurarReclamos(barrio.getReclamos());
                Falla.restaurarSecuencia(barrio.getEquipos());
                OrdenMantenimiento.restaurarSecuencia(barrio.getEquipos());
                Residente.restaurarSecuenciaAutorizaciones(sistemaIngreso.getAutorizaciones());

                System.out.println("Datos cargados correctamente.");
            } else {
                if (new File("Datos/LosAromos.dat").exists()) {
                    System.out.println("AVISO: existe un archivo .dat anterior.");
                    System.out.println("El nuevo TXT comienza con datos nuevos y no convierte el DAT.");
                    System.out.println("Conserve una copia del archivo anterior.");
                }
                barrio = new BarrioCerrado("Los Aromos");
                sistemaIngreso = new SistemaIngreso(barrio);
                gestorExpensas = new GestorExpensas(barrio);
                avisosAdministrador = new ArrayList<>();

                // Migrar una sola vez el archivo viejo, si existe.
                if (new File("Datos/Lotes.txt").exists()) {
                    for (Lote lote : archivoLotes.leerLotes()) barrio.agregarLote(lote);
                    System.out.println("Lotes importados del archivo anterior: "
                            + barrio.getLotes().size());
                }
                if (!guardarDatos()) return false;
            }
            return true;
        } catch (IOException e) {
            System.out.println("ERROR: no se pudieron recuperar los datos: " + e.getMessage());
            System.out.println("No se iniciara el sistema para no sobrescribir el archivo existente.");
            return false;
        }
    }

    private static boolean guardarDatos() {
        try {
            DatosSistema datos = new DatosSistema(
                    barrio, gestorExpensas, sistemaIngreso, avisosAdministrador);
            archivoDatos.guardar(datos);
            return true;
        } catch (IOException e) {
            System.out.println("ERROR: no se pudieron guardar los datos: " + e.getMessage());
            return false;
        }
    }
}

