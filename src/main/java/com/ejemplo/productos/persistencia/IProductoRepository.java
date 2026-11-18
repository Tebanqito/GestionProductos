package com.ejemplo.productos.persistencia;

import java.sql.SQLException;
import java.util.List;

import com.ejemplo.productos.modelo.Producto;

public interface IProductoRepository {
    void guardar(Producto producto) throws SQLException;

    /** Devuelve el producto activo o null si no existe (o fue dado de baja). */
    Producto buscarPorId(int id) throws SQLException;

    /** Lista solo productos activos. */
    List<Producto> listar() throws SQLException;

    /** Lista todos los productos, activos e inactivos (la interfaz gráfica muestra el estado). */
    List<Producto> listarTodos() throws SQLException;

    /** Igual que buscarPorId, pero también devuelve productos dados de baja. */
    Producto buscarPorIdConInactivos(int id) throws SQLException;

    List<Producto> buscarPorNombre(String texto) throws SQLException;

    List<Producto> buscarPorProveedor(String texto) throws SQLException;

    List<Producto> buscarPorRubro(String texto) throws SQLException;

    void modificar(Producto producto) throws SQLException;

    /** Baja lógica: activo = false. El registro permanece en la base. */
    void eliminar(int id) throws SQLException;
}
