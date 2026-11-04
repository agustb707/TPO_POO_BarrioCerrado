import Archivos.ArchivoLotes;
import Barrio.BarrioCerrado;
import Barrio.Lote;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String ruta = args.length > 0 ? args[0] : "Datos/lotes.txt";
        ArchivoLotes archivo = new ArchivoLotes(ruta);
        BarrioCerrado barrio = new BarrioCerrado("Los Aromos");

        try {
            for (Lote lote : archivo.leerLotes()) barrio.agregarLote(lote);
        } catch (IOException e) {
            System.out.println("No se pudo cargar el archivo: " + e.getMessage());
            System.out.println("Revisa los datos antes de volver a ejecutar.");
            return;
        }

        System.out.println("Barrio " + barrio.getNombre());
        System.out.println("Archivo: " + archivo.getRuta());
        System.out.println("Lotes cargados: " + barrio.getLotes().size());
        System.out.println("Si el archivo no existe, se crea al guardar.");

        try (Scanner teclado = new Scanner(System.in)) {
            boolean salir = false;
            while (!salir) {
                mostrarMenu();
                if (!teclado.hasNextLine()) break;
                String opcion = teclado.nextLine().trim();
                try {
                    switch (opcion) {
                        case "1":
                            mostrarLotes(barrio);
                            break;
                        case "2":
                            agregarLote(barrio, teclado);
                            break;
                        case "3":
                            archivo.guardarLotes(barrio.getLotes());
                            System.out.println("Lotes guardados en el archivo.");
                            break;
                        case "0":
                            archivo.guardarLotes(barrio.getLotes());
                            System.out.println("Datos guardados. Hasta luego.");
                            salir = true;
                            break;
                        default:
                            System.out.println("Opcion invalida.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Ingresa un numero de lote entero y una superficie numerica.");
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    System.out.println("No se pudo guardar: " + e.getMessage());
                    System.out.println("Los datos siguen en memoria; podes volver a intentar.");
                }
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("1. Consultar lotes");
        System.out.println("2. Agregar lote");
        System.out.println("3. Guardar lotes");
        System.out.println("0. Guardar y salir");
        System.out.print("Opcion: ");
    }

    private static void agregarLote(BarrioCerrado barrio, Scanner teclado) {
        System.out.print("Numero de lote: ");
        int numero = Integer.parseInt(teclado.nextLine().trim());
        System.out.print("Superficie en m2: ");
        double superficie = Double.parseDouble(teclado.nextLine().trim().replace(',', '.'));
        barrio.agregarLote(new Lote(numero, superficie));
        System.out.println("Lote agregado. Guarda para conservarlo entre ejecuciones.");
    }

    private static void mostrarLotes(BarrioCerrado barrio) {
        if (barrio.getLotes().isEmpty()) {
            System.out.println("Todavia no hay lotes cargados.");
            return;
        }
        double total = barrio.calcularSuperficieTotal();
        for (Lote lote : barrio.getLotes()) {
            System.out.printf("Lote %d | %.2f m2 | Coeficiente: %.4f%n",
                    lote.getNumeroLote(), lote.getSuperficie(), lote.calcularCoeficiente(total));
        }
        System.out.printf("Superficie total: %.2f m2%n", total);
    }
}
