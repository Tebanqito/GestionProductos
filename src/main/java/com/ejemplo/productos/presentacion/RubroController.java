package com.ejemplo.productos.presentacion;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.ejemplo.productos.modelo.Rubro;
import com.ejemplo.productos.servicio.RubroService;

public class RubroController {

    private final RubroService service;

    public RubroController(RubroService service) {
        this.service = Objects.requireNonNull(service);
    }

    public void registrar(String nombre) throws SQLException {
        service.registrar(new Rubro(0, nombre));
    }

    public Rubro buscar(int id) throws SQLException {
        return service.buscar(id);
    }

    public List<Rubro> listar() throws SQLException {
        return service.listar();
    }

    public void modificar(Rubro rubro) throws SQLException {
        service.modificar(rubro);
    }

    public void eliminar(int id) throws SQLException {
        service.eliminar(id);
    }
}
