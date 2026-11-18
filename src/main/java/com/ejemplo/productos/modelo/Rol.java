package com.ejemplo.productos.modelo;

/**
 * Persona autenticada. No conserva el PIN.
 * Tipos de rol: "e" empleado, "a" administrador, "i" invitado.
 */
public class Rol {
    public static final String EMPLEADO = "e";
    public static final String ADMINISTRADOR = "a";
    public static final String INVITADO = "i";

    private final int id;
    private final String apellido;
    private final String nombre;
    private final String rol;

    public Rol(int id, String apellido, String nombre, String rol) {
        if (!EMPLEADO.equals(rol) && !ADMINISTRADOR.equals(rol) && !INVITADO.equals(rol)) {
            throw new IllegalArgumentException("Rol inválido");
        }
        this.id = id;
        this.apellido = apellido;
        this.nombre = nombre;
        this.rol = rol;
    }

    public int getId() { return id; }
    public String getApellido() { return apellido; }
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }

    public boolean esAdministrador() {
        return ADMINISTRADOR.equals(rol);
    }

    public boolean esEmpleado() {
        return EMPLEADO.equals(rol);
    }

    public boolean esInvitado() {
        return INVITADO.equals(rol);
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getNombreRol() {
        if (esAdministrador()) return "Administrador";
        if (esEmpleado()) return "Empleado";
        return "Invitado";
    }
}
