package Reclamos;


import Barrio.Lote;
import Enums.EstadoReclamo;
import Enums.Prioridad;
import Enums.TipoReclamo;
import Excepciones.ReglaNegocioException;
import Persona.Persona;
import Persona.Personal;
import Persona.Residente;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Reclamo {
    private static int secuencia = 0;
    // Permite controlar los cinco reclamos EN_CURSO por empleado.
    private static List<Reclamo> reclamosCreados = new ArrayList<>();

    private int numero;
    private LocalDateTime fecha;
    private TipoReclamo tipo;
    private String descripcion;
    private Prioridad prioridad;
    private EstadoReclamo estado;
    private LocalDateTime fechaResolucion;
    private LocalDateTime fechaAtencion;
    private Lote lote;
    private Residente residente;
    private Personal responsable;
    private List<HistorialEstado> historial;
    private List<Comentario> comentarios;

    public Reclamo(Lote lote, Residente residente, TipoReclamo tipo,
                   String descripcion) {
        if (lote == null || residente == null || tipo == null ||
                descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("Datos del reclamo invalidos.");
        }
        if (!lote.puedePresentarReclamo(residente)) {
            throw new ReglaNegocioException("El residente no puede reclamar por este lote.");
        }
        this.numero = ++secuencia;
        this.fecha = LocalDateTime.now();
        this.tipo = tipo;
        this.descripcion = descripcion;
        if (tipo == TipoReclamo.SEGURIDAD) {
            this.prioridad = Prioridad.ALTA;
        } else if (tipo == TipoReclamo.MANTENIMIENTO) {
            this.prioridad = Prioridad.MEDIA;
        } else {
            this.prioridad = Prioridad.BAJA;
        }
        this.estado = EstadoReclamo.ABIERTO;
        this.lote = lote;
        this.residente = residente;
        this.historial = new ArrayList<>();
        this.comentarios = new ArrayList<>();
        historial.add(new HistorialEstado(fecha, estado, "Reclamo registrado"));
        reclamosCreados.add(this);
    }

    public void asignarEmpleado(Personal empleado) {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado es obligatorio.");
        }
        if (estado != EstadoReclamo.ABIERTO && estado != EstadoReclamo.ASIGNADO) {
            throw new ReglaNegocioException("No puede asignarse en el estado actual.");
        }
        this.responsable = empleado;
        if (estado == EstadoReclamo.ABIERTO) {
            cambiarEstado(EstadoReclamo.ASIGNADO, "Empleado asignado");
        }
    }

    public void cambiarEstado(EstadoReclamo nuevoEstado, String observacion) {
        if (nuevoEstado == null || observacion == null || observacion.isBlank()) {
            throw new IllegalArgumentException("Estado y observacion obligatorios.");
        }
        boolean pasoValido =
                (estado == EstadoReclamo.ABIERTO && nuevoEstado == EstadoReclamo.ASIGNADO) ||
                        (estado == EstadoReclamo.ASIGNADO && nuevoEstado == EstadoReclamo.EN_CURSO) ||
                        (estado == EstadoReclamo.EN_CURSO && nuevoEstado == EstadoReclamo.RESUELTO) ||
                        (estado == EstadoReclamo.RESUELTO && nuevoEstado == EstadoReclamo.CERRADO);
        if (!pasoValido) {
            throw new ReglaNegocioException("Solo se puede avanzar al siguiente estado.");
        }
        if (nuevoEstado == EstadoReclamo.ASIGNADO && responsable == null) {
            throw new ReglaNegocioException("Primero debe asignar un empleado.");
        }
        if (nuevoEstado == EstadoReclamo.EN_CURSO) {
            if (responsable == null) {
                throw new ReglaNegocioException("El reclamo no tiene responsable.");
            }
            verificarCupoEmpleado();
            fechaAtencion = LocalDateTime.now();
        }
        if (nuevoEstado == EstadoReclamo.RESUELTO) {
            fechaResolucion = LocalDateTime.now();
        }
        estado = nuevoEstado;
        historial.add(new HistorialEstado(LocalDateTime.now(), nuevoEstado, observacion));
    }

    private void verificarCupoEmpleado() {
        int cantidad = 0;
        for (Reclamo reclamo : reclamosCreados) {
            if (reclamo != this && reclamo.estaEnCurso() &&
                    reclamo.responsable != null &&
                    reclamo.responsable.getLegajo() == responsable.getLegajo()) {
                cantidad++;
            }
        }
        if (cantidad >= 5) {
            throw new ReglaNegocioException("El empleado ya tiene cinco reclamos en curso.");
        }
    }

    public void reabrir(Residente solicitante, LocalDateTime fechaActual) {
        if (solicitante == null || fechaActual == null) {
            throw new IllegalArgumentException("Faltan datos de reapertura.");
        }
        if (!lote.puedePresentarReclamo(solicitante)) {
            throw new ReglaNegocioException("El residente no esta autorizado.");
        }
        if (estado != EstadoReclamo.RESUELTO || fechaResolucion == null) {
            throw new ReglaNegocioException("Solo se puede reabrir un reclamo resuelto.");
        }
        if (fechaActual.isBefore(fechaResolucion) ||
                fechaActual.isAfter(fechaResolucion.plusDays(5))) {
            throw new ReglaNegocioException("El plazo de reapertura vencio.");
        }
        verificarCupoEmpleado();
        estado = EstadoReclamo.EN_CURSO;
        fechaResolucion = null;
        historial.add(new HistorialEstado(fechaActual, estado, "Reabierto por vecino"));
    }

    public void cerrarSiVencioPlazo(LocalDateTime fechaActual) {
        if (fechaActual == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (estado == EstadoReclamo.RESUELTO && fechaResolucion != null &&
                fechaActual.isAfter(fechaResolucion.plusDays(5))) {
            estado = EstadoReclamo.CERRADO;
            historial.add(new HistorialEstado(fechaActual, estado, "Plazo de reapertura vencido"));
        }
    }

    public boolean requiereEscalamiento(LocalDateTime fechaActual) {
        return fechaActual != null && prioridad == Prioridad.ALTA &&
                fechaAtencion == null &&
                !fechaActual.isBefore(fecha) &&
                Duration.between(fecha, fechaActual).toHours() >= 24;
    }

    public void agregarComentario(Persona autor, String texto) {
        if (autor == null) {
            throw new IllegalArgumentException("El autor es obligatorio.");
        }
        if (autor instanceof Residente &&
                !lote.puedePresentarReclamo((Residente) autor)) {
            throw new ReglaNegocioException("El vecino no esta autorizado.");
        }
        if (!(autor instanceof Residente) && !(autor instanceof Personal)) {
            throw new ReglaNegocioException("Este autor no puede comentar reclamos.");
        }
        comentarios.add(new Comentario(autor, texto));
    }

    public List<HistorialEstado> consultarHistorial() {
        return new ArrayList<>(historial);
    }

    // Los atributos static no quedan guardados en la serializacion.
    public static void restaurarReclamos(List<Reclamo> reclamosGuardados) {
        secuencia = 0;
        reclamosCreados = new ArrayList<>();
        for (Reclamo reclamo : reclamosGuardados) {
            reclamosCreados.add(reclamo);
            if (reclamo.getNumero() > secuencia) secuencia = reclamo.getNumero();
        }
    }

    // Restaura una version que ya fue validada al guardarse en texto.
    public void restaurarDatos(int numero, LocalDateTime fecha, EstadoReclamo estado,
                               Personal responsable, LocalDateTime fechaAtencion, LocalDateTime fechaResolucion,
                               List<HistorialEstado> historial, List<Comentario> comentarios) {
        this.numero = numero;
        this.fecha = fecha;
        this.estado = estado;
        this.responsable = responsable;
        this.fechaAtencion = fechaAtencion;
        this.fechaResolucion = fechaResolucion;
        this.historial = new ArrayList<>(historial);
        this.comentarios = new ArrayList<>(comentarios);
    }
    public boolean estaEnCurso() { return estado == EstadoReclamo.EN_CURSO; }
    public int getNumero() { return numero; }
    public LocalDateTime getFecha() { return fecha; }
    public TipoReclamo getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public Prioridad getPrioridad() { return prioridad; }
    public EstadoReclamo getEstado() { return estado; }
    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public LocalDateTime getFechaAtencion() { return fechaAtencion; }
    public Lote getLote() { return lote; }
    public Residente getResidente() { return residente; }
    public Personal getResponsable() { return responsable; }
    public List<Comentario> getComentarios() { return new ArrayList<>(comentarios); }
}
