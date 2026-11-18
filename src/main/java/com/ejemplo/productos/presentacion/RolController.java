package com.ejemplo.productos.presentacion;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.ejemplo.productos.modelo.Rol;
import com.ejemplo.productos.servicio.RolService;

public class RolController {

    private final RolService service;

    public RolController(RolService service) {
        this.service = Objects.requireNonNull(service);
    }

    public List<Rol> listar() throws SQLException {
        return service.listar();
    }

    public Rol buscar(int id) throws SQLException {
        return service.buscar(id);
    }

    public void registrar(String pin, String apellido, String nombre, String tipo)
            throws SQLException {
        service.registrar(pin, apellido, nombre, tipo);
    }

    public void modificar(int id, String apellido, String nombre, String tipo, String nuevoPin)
            throws SQLException {
        service.modificar(id, apellido, nombre, tipo, nuevoPin);
    }

    public void eliminar(int id) throws SQLException {
        service.eliminar(id);
    }
}
