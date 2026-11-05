package Excepciones;

// Se lanza cuando una operación falla una regla de negocio del barrio.
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}

