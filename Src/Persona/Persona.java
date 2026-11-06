package Persona;

public abstract class Persona {
    private String nombre;
    private String apellido;
    private int dni;
    private int telefono;
    private String email;

    public Persona(String nombre, String apellido, int dni, int telefono, String email) {
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("Nombre y apellido son obligatorios.");
        }
        if (dni < 1000000 || dni > 999999999) {
            throw new IllegalArgumentException("El DNI debe tener entre 7 y 9 digitos.");
        }
        if (telefono < 10000000 || telefono > 99999999) {
            throw new IllegalArgumentException("El telefono debe tener 8 digitos, sin codigo de area.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio.");
        }
        int arroba = email.indexOf('@');
        int punto = email.indexOf('.', arroba + 1);
        if (arroba <= 0 || punto <= arroba + 1 || punto == email.length() - 1 || email.indexOf('@', arroba + 1) != -1) {
            throw new IllegalArgumentException("El email debe tener un @ y luego un punto.");
        }
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
        this.email = email;
    }
    public String obtenerNombreCompleto() { return nombre + " " + apellido; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public int getDni() { return dni; }
    public int getTelefono() { return telefono; }
    public String getEmail() { return email; }
}
