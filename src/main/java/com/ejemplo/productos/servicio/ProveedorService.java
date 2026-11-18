package com.ejemplo.productos.servicio;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.ejemplo.productos.modelo.Proveedor;
import com.ejemplo.productos.persistencia.ErroresBD;
import com.ejemplo.productos.persistencia.IProveedorRepository;

public class ProveedorService {

    private final IProveedorRepository repository;
    private final PermisosService permisos;

    public ProveedorService(IProveedorRepository repository, PermisosService permisos) {
        this.repository = Objects.requireNonNull(repository);
        this.permisos = Objects.requireNonNull(permisos);
    }

    public void registrar(Proveedor proveedor) throws SQLException {
        permisos.exigirAdministrador();
        validar(proveedor);
        try {
            repository.guardar(proveedor);
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    public Proveedor buscar(int id) throws SQLException {
        permisos.exigirAdministrador();
        validarId(id);
        return repository.buscarPorId(id);
    }

    public List<Proveedor> listar() throws SQLException {
        permisos.exigirSesion();
        return repository.listar();
    }

    public void modificar(Proveedor proveedor) throws SQLException {
        permisos.exigirAdministrador();
        validarId(proveedor.getId());
        validar(proveedor);
        if (repository.buscarPorId(proveedor.getId()) == null) {
            throw new IllegalArgumentException("Proveedor inexistente");
        }
        try {
            repository.modificar(proveedor);
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    public void eliminar(int id) throws SQLException {
        permisos.exigirAdministrador();
        validarId(id);
        if (repository.buscarPorId(id) == null) {
            throw new IllegalArgumentException("Proveedor inexistente");
        }
        try {
            repository.eliminar(id);
        } catch (SQLException e) {
            if (ErroresBD.esClaveForaneaViolada(e)) {
                throw new IllegalArgumentException(
                        "No se puede eliminar: hay productos asociados a este proveedor");
            }
            throw e;
        }
    }

    private void validar(Proveedor p) {
        if (p == null || p.getRazonSocial() == null || p.getRazonSocial().isBlank()) {
            throw new IllegalArgumentException("La razón social es obligatoria");
        }
        if (p.getRazonSocial().length() > 100) {
            throw new IllegalArgumentException("La razón social admite hasta 100 caracteres");
        }
        if (p.getDomicilio() != null && p.getDomicilio().length() > 150) {
            throw new IllegalArgumentException("El domicilio admite hasta 150 caracteres");
        }
        if (p.getTelefono() != null && p.getTelefono().length() > 30) {
            throw new IllegalArgumentException("El teléfono admite hasta 30 caracteres");
        }
    }

    private void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }
    }

    private SQLException traducir(SQLException e) {
        if (ErroresBD.esDuplicado(e)) {
            throw new IllegalArgumentException("Ya existe un proveedor con esa razón social");
        }
        return e;
    }
}
