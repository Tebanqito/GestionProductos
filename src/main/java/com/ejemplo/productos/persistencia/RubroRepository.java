package com.ejemplo.productos.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ejemplo.productos.modelo.Rubro;

public class RubroRepository implements IRubroRepository {

    @Override
    public void guardar(Rubro rubro) throws SQLException {
        String sql = "INSERT INTO rubros (nombre) VALUES (?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rubro.getNombre());
            ps.executeUpdate();
        }
    }

    @Override
    public Rubro buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM rubros WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Rubro(rs.getInt("id"), rs.getString("nombre"));
                }
            }
        }
        return null;
    }

    @Override
    public List<Rubro> listar() throws SQLException {
        List<Rubro> rubros = new ArrayList<>();
        String sql = "SELECT id, nombre FROM rubros ORDER BY id";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rubros.add(new Rubro(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return rubros;
    }

    @Override
    public void modificar(Rubro rubro) throws SQLException {
        String sql = "UPDATE rubros SET nombre = ? WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rubro.getNombre());
            ps.setInt(2, rubro.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM rubros WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
