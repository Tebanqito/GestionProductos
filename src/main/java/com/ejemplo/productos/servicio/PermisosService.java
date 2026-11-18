package com.ejemplo.productos.servicio;

import com.ejemplo.productos.modelo.Rol;

/**
 * Centraliza la autorización según el rol de la persona autenticada.
 *  - Administrador (a): todo.
 *  - Empleado (e): ABM y búsquedas de productos, listados de rubros y proveedores.
 *  - Invitado (i): solo listados de productos, rubros y proveedores.
 */
public class PermisosService {

    private final Rol persona;

    public PermisosService(Rol persona) {
        if (persona == null) {
            throw new SecurityException("Debe iniciar sesión");
        }
        this.persona = persona;
    }

    public Rol getPersona() {
        return persona;
    }

    /** Cualquier rol válido: permite ver listados. */
    public void exigirSesion() {
        if (!persona.esAdministrador() && !persona.esEmpleado() && !persona.esInvitado()) {
            throw new SecurityException("Rol no autorizado");
        }
    }

    /** Empleado o administrador: ABM y búsquedas de productos. */
    public void exigirOperador() {
        exigirSesion();
        if (persona.esInvitado()) {
            throw new SecurityException("El invitado solo puede ver listados");
        }
    }

    /** Solo administrador: ABM de rubros, proveedores y roles. */
    public void exigirAdministrador() {
        exigirSesion();
        if (!persona.esAdministrador()) {
            throw new SecurityException("Operación exclusiva del administrador");
        }
    }
}
