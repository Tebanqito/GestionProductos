package com.ejemplo.productos.persistencia;

import java.sql.SQLException;
import java.util.List;

import com.ejemplo.productos.modelo.Rubro;

public interface IRubroRepository {
    void guardar(Rubro rubro) throws SQLException;

    /** Devuelve el rubro o null si no existe. */
    Rubro buscarPorId(int id) throws SQLException;

    List<Rubro> listar() throws SQLException;

    void modificar(Rubro rubro) throws SQLException;

    void eliminar(int id) throws SQLException;
}
