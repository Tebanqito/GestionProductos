package com.ejemplo.productos.servicio;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.ejemplo.productos.modelo.Rubro;
import com.ejemplo.productos.persistencia.ErroresBD;
import com.ejemplo.productos.persistencia.IRubroRepository;

public class RubroService {

    private final IRubroRepository repository;
    private final PermisosService permisos;

    public RubroService(IRubroRepository repository, PermisosService permisos) {
        this.repository = Objects.requireNonNull(repository);
        this.permisos = Objects.requireNonNull(permisos);
    }

    public void registrar(Rubro rubro) throws SQLException {
        permisos.exigirAdministrador();
        validar(rubro);
        try {
            repository.guardar(rubro);
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    public Rubro buscar(int id) throws SQLException {
        permisos.exigirAdministrador();
        validarId(id);
        return repository.buscarPorId(id);
    }

    public List<Rubro> listar() throws SQLException {
        permisos.exigirSesion();
        return repository.listar();
    }

    public void modificar(Rubro rubro) throws SQLException {
        permisos.exigirAdministrador();
        validarId(rubro.getId());
        validar(rubro);
        if (repository.buscarPorId(rubro.getId()) == null) {
            throw new IllegalArgumentException("Rubro inexistente");
        }
        try {
            repository.modificar(rubro);
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    public void eliminar(int id) throws SQLException {
        permisos.exigirAdministrador();
        validarId(id);
        if (repository.buscarPorId(id) == null) {
            throw new IllegalArgumentException("Rubro inexistente");
        }
        try {
            repository.eliminar(id);
        } catch (SQLException e) {
            if (ErroresBD.esClaveForaneaViolada(e)) {
                throw new IllegalArgumentException(
                        "No se puede eliminar: hay productos asociados a este rubro");
            }
            throw e;
        }
    }

    private void validar(Rubro rubro) {
        if (rubro == null || rubro.getNombre() == null || rubro.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del rubro es obligatorio");
        }
        if (rubro.getNombre().length() > 100) {
            throw new IllegalArgumentException("El nombre del rubro admite hasta 100 caracteres");
        }
    }

    private void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }
    }

    private SQLException traducir(SQLException e) {
        if (ErroresBD.esDuplicado(e)) {
            throw new IllegalArgumentException("Ya existe un rubro con ese nombre");
        }
        return e;
    }
}
