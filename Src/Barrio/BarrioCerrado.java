package Barrio;

import java.util.ArrayList;
import java.util.List;
import Equipos.Equipo;
import Enums.Sector;
import Equipos.Falla;

public class BarrioCerrado {
 private String nombre;
 private List<Lote> lotes;
 private List<Equipo> equipos;

 public BarrioCerrado(String nombre) {
  if (nombre == null || nombre.isBlank()) {
   throw new IllegalArgumentException("El nombre es obligatorio.");
  }
  this.nombre = nombre;
  this.lotes = new ArrayList<>();
    this.equipos = new ArrayList<>();
 }

 public void agregarLote(Lote lote) {
  if (lote == null) throw new IllegalArgumentException("El lote es obligatorio.");
  if (lotes.size() >= 150) throw new IllegalStateException("El barrio admite 150 lotes.");
  if (buscarLote(lote.getNumeroLote()) != null) {
   throw new IllegalArgumentException("Ya existe un lote con ese numero.");
  }
  lotes.add(lote);
 }

 public Lote buscarLote(int numero) {
  for (Lote lote : lotes) {
   if (lote.getNumeroLote() == numero) return lote;
  }
  return null;
 }

 public double calcularSuperficieTotal() {
  double total = 0;
  for (Lote lote : lotes) total += lote.getSuperficie();
  return total;
 }

 public String getNombre() { return nombre; }
 public List<Lote> getLotes() { return new ArrayList<>(lotes); }

    // Cantidad de fallas activas (sin orden cerrada) de los equipos del sector.
    public int contarFallasSector(Sector sector) {
        int cantidad = 0;
        for (Equipo equipo : equipos) {
            if (equipo.getSector() == sector) {
                for (Falla falla : equipo.getFallas()) {
                    if (falla.estaActiva()) {
                        cantidad++;
                    }
                }
            }
        }
        return cantidad;
    }

}
