package Persona;
import java.time.LocalDate;

public abstract class Personal extends Persona {
    private final int legajo;
    private final LocalDate fechaIngreso;

    protected Personal(String nombre, String apellido, String dni, String telefono, String email,int legajo, LocalDate fechaIngreso) {
        super(nombre, apellido, dni, telefono, email);
        this.legajo = legajo;
        this.fechaIngreso = fechaIngreso; }

    public int getLegajo() { 
        return legajo; }
        
    public LocalDate getFechaIngreso() { 
        return fechaIngreso; }  
}