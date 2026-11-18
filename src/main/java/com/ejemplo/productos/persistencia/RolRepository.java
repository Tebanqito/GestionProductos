package com.ejemplo.productos.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ejemplo.productos.modelo.Rol;

public class RolRepository implements IRolRepository {

    @Override
    public Rol buscarPorPin(String pin) throws SQLException {
        String sql = "SELECT id, apellido, nombre, rol FROM roles WHERE pin = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pin);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Rol buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, apellido, nombre, rol FROM roles WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Rol> listar() throws SQLException {
        List<Rol> lista = new ArrayList<>();
        String sql = "SELECT id, apellido, nombre, rol FROM roles ORDER BY id";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public void guardar(Rol persona, String pin) throws SQLException {
        String sql = "INSERT INTO roles (pin, apellido, nombre, rol) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pin);
            ps.setString(2, persona.getApellido());
            ps.setString(3, persona.getNombre());
            ps.setString(4, persona.getRol());
            ps.executeUpdate();
        }
    }

    @Override
    public void modificar(Rol persona) throws SQLException {
        String sql = "UPDATE roles SET apellido = ?, nombre = ?, rol = ? WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, persona.getApellido());
            ps.setString(2, persona.getNombre());
            ps.setString(3, persona.getRol());
            ps.setInt(4, persona.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void cambiarPin(int id, String pin) throws SQLException {
        String sql = "UPDATE roles SET pin = ? WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pin);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM roles WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Rol mapear(ResultSet rs) throws SQLException {
        return new Rol(rs.getInt("id"), rs.getString("apellido"),
                rs.getString("nombre"), rs.getString("rol"));
    }
}
