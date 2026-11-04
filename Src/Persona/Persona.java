package Persona;

public abstract class Persona {
    private String nombre;
    private String apellido;
    private int dni;
    private String telefono;
    private String email;

    public Persona(String nombre, String apellido, int dni, String telefono, String email) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
        this.email = email;
    }

    public String obtenerNombreCompleto() {
        return nombre + " " + apellido;
    }
    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public int getDni() {
        return dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }
}