package Archivos;

import Barrio.Lote;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ArchivoLotes {
    private File archivo;

    public ArchivoLotes(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalArgumentException("La ruta es obligatoria.");
        }
        archivo = new File(ruta);
    }

    // Formato de cada linea: numeroLote;superficie
    public List<Lote> leerLotes() throws IOException {
        List<Lote> lotes = new ArrayList<>();
        if (!archivo.exists()) return lotes;

        try (BufferedReader lector = new BufferedReader(
                new FileReader(archivo, StandardCharsets.UTF_8))) {
            String linea;
            int numeroLinea = 0;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank() || linea.trim().startsWith("#")) continue;
                String[] datos = linea.split(";", -1);
                if (datos.length != 2) {
                    throw new IOException("Linea " + numeroLinea + ": se espera numero;superficie.");
                }
                try {
                    int numero = Integer.parseInt(datos[0].trim());
                    double superficie = Double.parseDouble(datos[1].trim());
                    Lote nuevo = new Lote(numero, superficie);
                    for (Lote existente : lotes) {
                        if (existente.getNumeroLote() == numero) {
                            throw new IllegalArgumentException("Numero de lote repetido.");
                        }
                    }
                    if (lotes.size() >= 150) {
                        throw new IllegalArgumentException("Se superan los 150 lotes.");
                    }
                    lotes.add(nuevo);
                } catch (IllegalArgumentException e) {
                    throw new IOException("Linea " + numeroLinea + ": " + e.getMessage(), e);
                }
            }
        }
        return lotes;
    }

    // Guarda la lista completa; reemplaza la version anterior del archivo.
    public void guardarLotes(List<Lote> lotes) throws IOException {
        if (lotes == null) throw new IllegalArgumentException("La lista es obligatoria.");
        File carpeta = archivo.getAbsoluteFile().getParentFile();
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de datos.");
        }
        try (BufferedWriter escritor = new BufferedWriter(
                new FileWriter(archivo, StandardCharsets.UTF_8))) {
            escritor.write("# numeroLote;superficie (usar punto decimal)");
            escritor.newLine();
            for (Lote lote : lotes) {
                escritor.write(lote.getNumeroLote() + ";" + lote.getSuperficie());
                escritor.newLine();
            }
        }
    }

    public String getRuta() { return archivo.getAbsolutePath(); }
}
