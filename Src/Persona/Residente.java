package Persona;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import Barrio.Lote;
import Equipos.Equipo;
import Equipos.Falla;
import Enums.TipoReclamo;
import Ingresos.AutorizacionIngreso;
import Reclamos.Reclamo;
import Excepciones.ReglaNegocioException;


//Residente malvado

public abstract class Residente extends Persona {
    private List<Lote> lotes;
    private static int secuenciaAutorizaciones = 0;

    protected Residente(String nombre, String apellido, int dni, int telefono, String email) {
        super(nombre, apellido, dni, telefono, email);
        lotes = new ArrayList<>();
    }
    public void agregarLote(Lote lote) {
        if (lote == null) throw new IllegalArgumentException("Lote obligatorio.");
        if (!lotes.contains(lote)) lotes.add(lote);
    }
    public Reclamo presentarReclamo(Lote lote, TipoReclamo tipo, String descripcion) {
        if (lote == null || tipo == null) throw new IllegalArgumentException("Datos del reclamo invalidos.");
        if (!lote.puedePresentarReclamo(this)) {
            throw new ReglaNegocioException("El residente no esta autorizado para reclamar.");
        }
        return new Reclamo(lote, this, tipo, descripcion);
    }
    public void comentarReclamo(Reclamo reclamo, String texto) {
        if (reclamo == null || texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Comentario invalido.");
        }
        reclamo.agregarComentario(this, texto);
    }
    public Falla registrarFalla(Equipo equipo, String descripcion) {
        if (equipo == null) throw new IllegalArgumentException("Equipo obligatorio.");
        Falla falla = new Falla(equipo, this, descripcion);
        equipo.registrarFalla(falla);
        return falla;
    }
    public AutorizacionIngreso autorizarIngreso(Visitante visitante, Lote lote,
                                                LocalDate fecha, LocalTime desde, LocalTime hasta) {
        if (lote == null || !lote.puedeAutorizarIngreso(this)) {
            throw new ReglaNegocioException("El residente no puede autorizar visitas.");
        }
        return new AutorizacionIngreso(++secuenciaAutorizaciones, visitante, this, lote,
                fecha, desde, hasta);
    }
    public static void restaurarSecuenciaAutorizaciones(List<AutorizacionIngreso> permisos) {
        secuenciaAutorizaciones = 0;
        for (AutorizacionIngreso permiso : permisos) {
            if (permiso.getNumero() > secuenciaAutorizaciones) {
                secuenciaAutorizaciones = permiso.getNumero();
            }
        }
    }
    public List<Lote> getLotes() { return new ArrayList<>(lotes); }
}