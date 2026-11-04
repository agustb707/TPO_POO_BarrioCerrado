package Persona;

public  class Familiar extends Residente {
    private  Titular responsable;

    public Familiar(String nombre, String apellido, int dni, String telefono, String email, Titular responsable) {
        super(nombre, apellido, dni, telefono, email);
        this.responsable = responsable;
    }

    public Titular getResponsable() {
        return responsable;
    }
    
}
