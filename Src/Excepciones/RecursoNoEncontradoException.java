package Excepciones;
// Se lanza cuando se busca un recurso (lote, expensa, presupuesto) que no existe.
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
