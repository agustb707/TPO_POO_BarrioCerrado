
package Persona;

import Barrio.Lote;
import Expensas.Expensa;
import Excepciones.ReglaNegocioException;

import java.util.List;

public class Titular extends Residente {

    public Titular(String nombre, String apellido, String dni,
                   String telefono, String email) {

        super(nombre, apellido, dni, telefono, email);
    }

    // CONSULTAR EXPENSAS

    public List<Expensa> consultarExpensas(Lote lote) {

        if (lote == null) {
            throw new IllegalArgumentException("El lote es obligatorio.");
        }

        if (lote.getTitular() == null ||
                !lote.getTitular().getDni().equals(this.getDni())) {

            throw new ReglaNegocioException(
                    "El titular no pertenece a este lote.");
        }

        return lote.getExpensas();
    }
}
