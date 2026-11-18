package com.ejemplo.productos.persistencia;

import java.sql.SQLException;
import java.util.List;

import com.ejemplo.productos.modelo.Rol;

public interface IRolRepository {
    /** Devuelve la persona o null si el PIN no existe. */
    Rol buscarPorPin(String pin) throws SQLException;

    Rol buscarPorId(int id) throws SQLException;

    List<Rol> listar() throws SQLException;

    void guardar(Rol persona, String pin) throws SQLException;

    void modificar(Rol persona) throws SQLException;

    void cambiarPin(int id, String pin) throws SQLException;

    void eliminar(int id) throws SQLException;
}
