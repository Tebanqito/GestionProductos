package com.ejemplo.productos.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.ejemplo.productos.modelo.Producto;
import com.ejemplo.productos.modelo.Proveedor;
import com.ejemplo.productos.modelo.Rubro;

public class ProductoRepository implements IProductoRepository {

    private static final String SELECT_BASE = """
            SELECT p.id, p.nombre, p.descripcion, p.precio, p.stock,
                   p.activo, p.fecha_alta,
                   r.id AS rubro_id, r.nombre AS rubro_nombre,
                   pr.id_prov AS prov_id, pr.razon_social,
                   pr.domicilio, pr.telefono
            FROM productos p
            INNER JOIN rubros r ON p.rubro_id = r.id
            INNER JOIN proveedores pr ON p.proveedor_id = pr.id_prov
            """;

    @Override
    public void guardar(Producto producto) throws SQLException {
        String sql = """
                INSERT INTO productos
                (nombre, descripcion, precio, stock, activo, fecha_alta, rubro_id, proveedor_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setBigDecimal(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setBoolean(5, producto.isActivo());
            ps.setObject(6, producto.getFechaAlta());
            ps.setInt(7, producto.getRubro().getId());
            ps.setInt(8, producto.getProveedor().getId());
            ps.executeUpdate();
        }
    }

    @Override
    public Producto buscarPorId(int id) throws SQLException {
        return buscarUno(" WHERE p.id = ? AND p.activo = TRUE", id);
    }

    @Override
    public Producto buscarPorIdConInactivos(int id) throws SQLException {
        return buscarUno(" WHERE p.id = ?", id);
    }

    private Producto buscarUno(String condicion, int id) throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + condicion)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Producto> listar() throws SQLException {
        return consultar(" WHERE p.activo = TRUE ORDER BY p.id", null);
    }

    @Override
    public List<Producto> listarTodos() throws SQLException {
        return consultar(" ORDER BY p.id", null);
    }

    @Override
    public List<Producto> buscarPorNombre(String texto) throws SQLException {
        return consultar(" WHERE p.activo = TRUE AND p.nombre ILIKE ? ORDER BY p.id", texto);
    }

    @Override
    public List<Producto> buscarPorProveedor(String texto) throws SQLException {
        return consultar(" WHERE p.activo = TRUE AND pr.razon_social ILIKE ? ORDER BY p.id", texto);
    }

    @Override
    public List<Producto> buscarPorRubro(String texto) throws SQLException {
        return consultar(" WHERE p.activo = TRUE AND r.nombre ILIKE ? ORDER BY p.id", texto);
    }

    @Override
    public void modificar(Producto producto) throws SQLException {
        String sql = """
                UPDATE productos
                SET nombre = ?, descripcion = ?, precio = ?, stock = ?,
                    activo = ?, rubro_id = ?, proveedor_id = ?
                WHERE id = ?
                """;
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setBigDecimal(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setBoolean(5, producto.isActivo());
            ps.setInt(6, producto.getRubro().getId());
            ps.setInt(7, producto.getProveedor().getId());
            ps.setInt(8, producto.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        String sql = "UPDATE productos SET activo = FALSE WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** El fragmento SQL es fijo (definido en este archivo); el texto del usuario va como parámetro. */
    private List<Producto> consultar(String condicionYOrden, String texto) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + condicionYOrden)) {
            if (texto != null) {
                ps.setString(1, "%" + texto + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapearProducto(rs));
                }
            }
        }
        return productos;
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Rubro rubro = new Rubro(rs.getInt("rubro_id"), rs.getString("rubro_nombre"));
        Proveedor proveedor = new Proveedor(rs.getInt("prov_id"), rs.getString("razon_social"),
                rs.getString("domicilio"), rs.getString("telefono"));

        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setStock(rs.getInt("stock"));
        p.setActivo(rs.getBoolean("activo"));
        p.setFechaAlta(rs.getObject("fecha_alta", LocalDate.class));
        p.setRubro(rubro);
        p.setProveedor(proveedor);
        return p;
    }
}
