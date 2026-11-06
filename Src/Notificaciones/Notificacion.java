package Notificaciones;

import Barrio.BarrioCerrado;
import Barrio.Lote;
import Enums.TipoNotificacion;
import Equipos.Camara;
import Equipos.Equipo;
import Equipos.Falla;
import Persona.Persona;
import Reclamos.Reclamo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Notificacion {
    private LocalDateTime fecha;
    private Persona destinatario;
    private Lote lote;
    private String mensaje;
    private TipoNotificacion motivo;

    public Notificacion(Persona destinatario, Lote lote, String mensaje,
                        TipoNotificacion motivo) {
        if (destinatario == null || motivo == null ||
                mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException("Datos de notificacion invalidos.");
        }
        this.fecha = LocalDateTime.now();
        this.destinatario = destinatario;
        this.lote = lote;
        this.mensaje = mensaje;
        this.motivo = motivo;
    }

    public String generarAviso() {
        return fecha + " - Para: " + destinatario.obtenerNombreCompleto()
                + " - " + motivo + " - " + mensaje;
    }

    // Genera avisos internos; no envia correo ni mensajes externos.
    public static List<Notificacion> generarAvisosAdministrador(BarrioCerrado barrio) {
        if (barrio == null || barrio.getAdministrador() == null) {
            throw new IllegalArgumentException("Falta el administrador del barrio.");
        }
        List<Notificacion> avisos = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();

        for (Reclamo reclamo : barrio.getReclamos()) {
            if (reclamo.requiereEscalamiento(ahora)) {
                avisos.add(new Notificacion(barrio.getAdministrador(), reclamo.getLote(),
                        "Reclamo " + reclamo.getNumero() + " sin atencion por 24 horas.",
                        TipoNotificacion.RECLAMO_ESCALADO));
            }
        }

        for (Equipo equipo : barrio.getEquipos()) {
            if (equipo instanceof Camara) {
                for (Falla falla : equipo.getFallas()) {
                    if (falla.requiereAvisoAdministrador(ahora)) {
                        avisos.add(new Notificacion(barrio.getAdministrador(), null,
                                "Camara " + equipo.getCodigo() + " sin funcionar por mas de 48 horas.",
                                TipoNotificacion.CAMARA_FUERA_DE_SERVICIO));
                        break;
                    }
                }
            }
        }
        return avisos;
    }

    public LocalDateTime getFecha() { return fecha; }
    public Persona getDestinatario() { return destinatario; }
    public Lote getLote() { return lote; }
    public String getMensaje() { return mensaje; }
    public TipoNotificacion getMotivo() { return motivo; }
}
