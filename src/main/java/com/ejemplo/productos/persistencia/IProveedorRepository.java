package com.ejemplo.productos.persistencia;

import java.sql.SQLException;
import java.util.List;

import com.ejemplo.productos.modelo.Proveedor;

public interface IProveedorRepository {
    void guardar(Proveedor proveedor) throws SQLException;

    /** Devuelve el proveedor o null si no existe. */
    Proveedor buscarPorId(int id) throws SQLException;

    List<Proveedor> listar() throws SQLException;

    void modificar(Proveedor proveedor) throws SQLException;

    void eliminar(int id) throws SQLException;
}
