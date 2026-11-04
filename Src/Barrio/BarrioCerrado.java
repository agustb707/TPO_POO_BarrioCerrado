package Barrio;

import java.util.ArrayList;
import java.util.List;

public class BarrioCerrado {
 private String nombre;
 private List<Lote> lotes;

 public BarrioCerrado(String nombre) {
  if (nombre == null || nombre.isBlank()) {
   throw new IllegalArgumentException("El nombre es obligatorio.");
  }
  this.nombre = nombre;
  this.lotes = new ArrayList<>();
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
}
