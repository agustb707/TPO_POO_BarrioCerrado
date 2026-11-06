
package Persona;

public abstract class Persona {

    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String email;

    public Persona(String nombre, String apellido, String dni,
                   String telefono, String email) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }

        if (dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("El DNI es obligatorio.");
        }

        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
        this.email = email;
    }

    // OBTENER NOMBRE COMPLETO

    public String obtenerNombreCompleto() {
        return nombre + " " + apellido;
    }

    // GETTERS

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getDni() {
        return dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }
}
