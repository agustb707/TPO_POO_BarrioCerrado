package Reclamos;


import Persona.Persona;
import java.time.LocalDateTime;

public class Comentario {
    private LocalDateTime fecha;
    private String texto;
    private Persona autor;

    public Comentario(Persona autor, String texto) {
        if (autor == null || texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Autor y comentario obligatorios.");
        }
        this.fecha = LocalDateTime.now();
        this.texto = texto;
        this.autor = autor;
    }

    public void restaurarFecha(LocalDateTime fecha) {
        if (fecha == null) throw new IllegalArgumentException("Fecha obligatoria.");
        this.fecha = fecha;
    }
    public LocalDateTime getFecha() { return fecha; }
    public String getTexto() { return texto; }
    public Persona getAutor() { return autor; }
}
