
package Archivos;

import Barrio.Lote;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArchivoLotes {

    private File archivo;

    public ArchivoLotes(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta es obligatoria.");
        }
        this.archivo = new File(ruta);
    }

    // Leer los lotes desde el archivo
    public List<Lote> leerLotes() throws IOException {
        List<Lote> lotes = new ArrayList<>();

        if (!archivo.exists()) {
            return lotes;
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int numeroLinea = 0;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;

                if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                    continue;
                }

                String[] datos = linea.split(";");

                if (datos.length != 2) {
                    throw new IOException("Formato incorrecto en la linea " + numeroLinea);
                }

                try {
                    int numero = Integer.parseInt(datos[0].trim());
                    double superficie = Double.parseDouble(datos[1].trim());

                    // Verificar que el lote no esté repetido
                    for (Lote lote : lotes) {
                        if (lote.getNumeroLote() == numero) {
                            throw new IllegalArgumentException("Numero de lote repetido.");
                        }
                    }

                    if (lotes.size() >= 150) {
                        throw new IllegalArgumentException("No puede haber mas de 150 lotes.");
                    }

                    Lote nuevo = new Lote(numero, superficie);
                    lotes.add(nuevo);

                } catch (IllegalArgumentException e) {
                    throw new IOException("Linea " + numeroLinea + ": " + e.getMessage(), e);
                }
            }
        }

        return lotes;
    }

    // Guardar los lotes en el archivo
    public void guardarLotes(List<Lote> lotes) throws IOException {
        if (lotes == null) {
            throw new IllegalArgumentException("La lista de lotes es obligatoria.");
        }

        if (lotes.size() > 150) {
            throw new IllegalArgumentException("No puede haber mas de 150 lotes.");
        }

        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo))) {

            escritor.write("# numeroLote;superficie");
            escritor.newLine();

            for (Lote lote : lotes) {
                escritor.write(lote.getNumeroLote() + ";" + lote.getSuperficie());
                escritor.newLine();
            }
        }
    }

    public String getRuta() {
        return archivo.getAbsolutePath();
    }
}
