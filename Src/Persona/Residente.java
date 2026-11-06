
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

public abstract class Residente extends Persona {

    private List<Lote> lotes;
    private static int secuenciaAutorizaciones = 0;

    protected Residente(String nombre, String apellido, String dni, String telefono, String email) {

        super(nombre, apellido, dni, telefono, email);
        this.lotes = new ArrayList<>();
    }

    // ASOCIAR LOTE

    public void agregarLote(Lote lote) {

        if (lote == null) {
            throw new IllegalArgumentException("El lote es obligatorio.");
        }

        if (!lotes.contains(lote)) {
            lotes.add(lote);
        }
    }

    // PRESENTAR RECLAMO

    public Reclamo presentarReclamo(Lote lote, TipoReclamo tipo, String descripcion) {

        if (lote == null || tipo == null) {
            throw new IllegalArgumentException("Faltan datos del reclamo.");
        }

        if (!lote.puedePresentarReclamo(this)) {
            throw new ReglaNegocioException("El residente no esta autorizado para presentar reclamos.");
        }

        return new Reclamo(lote, this, tipo, descripcion);
    }

    // COMENTAR RECLAMO

    public void comentarReclamo(Reclamo reclamo, String texto) {

        if (reclamo == null) {
            throw new IllegalArgumentException("El reclamo es obligatorio.");
        }

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El comentario es obligatorio.");
        }

        reclamo.agregarComentario(this, texto);
    }

    // REGISTRAR FALLA

    public Falla registrarFalla(Equipo equipo, String descripcion) {

        if (equipo == null) {
            throw new IllegalArgumentException("El equipo es obligatorio.");
        }

        Falla falla = new Falla(equipo, this, descripcion);
        equipo.registrarFalla(falla);

        return falla;
    }

    // AUTORIZAR INGRESO

    public AutorizacionIngreso autorizarIngreso(Visitante visitante, Lote lote, LocalDate fecha, LocalTime horaDesde, LocalTime horaHasta) {

        if (lote == null) {
            throw new IllegalArgumentException("El lote es obligatorio.");
        }

        if (!lote.puedeAutorizarIngreso(this)) {
            throw new ReglaNegocioException(
                    "El residente no puede autorizar visitas para este lote.");
        }

        AutorizacionIngreso autorizacion = new AutorizacionIngreso(++secuenciaAutorizaciones, visitante, this, lote, fecha, horaDesde, horaHasta);

        return autorizacion;
    }

    // GETTERS

    public List<Lote> getLotes() {
        return new ArrayList<>(lotes);
    }
}
