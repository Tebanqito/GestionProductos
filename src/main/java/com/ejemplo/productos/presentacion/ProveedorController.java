package com.ejemplo.productos.presentacion;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.ejemplo.productos.modelo.Proveedor;
import com.ejemplo.productos.servicio.ProveedorService;

public class ProveedorController {

    private final ProveedorService service;

    public ProveedorController(ProveedorService service) {
        this.service = Objects.requireNonNull(service);
    }

    public void registrar(String razonSocial, String domicilio, String telefono)
            throws SQLException {
        service.registrar(new Proveedor(0, razonSocial, domicilio, telefono));
    }

    public Proveedor buscar(int id) throws SQLException {
        return service.buscar(id);
    }

    public List<Proveedor> listar() throws SQLException {
        return service.listar();
    }

    public void modificar(Proveedor proveedor) throws SQLException {
        service.modificar(proveedor);
    }

    public void eliminar(int id) throws SQLException {
        service.eliminar(id);
    }
}
