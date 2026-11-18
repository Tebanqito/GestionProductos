package com.ejemplo.productos.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ejemplo.productos.modelo.Proveedor;

public class ProveedorRepository implements IProveedorRepository {

    private static final String SELECT =
            "SELECT id_prov, razon_social, domicilio, telefono FROM proveedores";

    @Override
    public void guardar(Proveedor p) throws SQLException {
        String sql = "INSERT INTO proveedores (razon_social, domicilio, telefono) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getRazonSocial());
            ps.setString(2, p.getDomicilio());
            ps.setString(3, p.getTelefono());
            ps.executeUpdate();
        }
    }

    @Override
    public Proveedor buscarPorId(int id) throws SQLException {
        String sql = SELECT + " WHERE id_prov = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Proveedor> listar() throws SQLException {
        List<Proveedor> lista = new ArrayList<>();
        String sql = SELECT + " ORDER BY id_prov";
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
    public void modificar(Proveedor p) throws SQLException {
        String sql = "UPDATE proveedores SET razon_social = ?, domicilio = ?, telefono = ? WHERE id_prov = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getRazonSocial());
            ps.setString(2, p.getDomicilio());
            ps.setString(3, p.getTelefono());
            ps.setInt(4, p.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM proveedores WHERE id_prov = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        return new Proveedor(rs.getInt("id_prov"), rs.getString("razon_social"),
                rs.getString("domicilio"), rs.getString("telefono"));
    }
}
