package com.ejemplo.productos.presentacion;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.ejemplo.productos.modelo.Producto;
import com.ejemplo.productos.modelo.Proveedor;
import com.ejemplo.productos.modelo.Rubro;
import com.ejemplo.productos.servicio.ProductoService;

public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = Objects.requireNonNull(service);
    }

    public void registrar(String nombre, String descripcion, BigDecimal precio,
                          int stock, int rubroId, int proveedorId) throws SQLException {
        Producto producto = new Producto(nombre, descripcion, precio, stock);
        producto.setRubro(new Rubro(rubroId, null));
        producto.setProveedor(new Proveedor(proveedorId, null, null, null));
        service.registrar(producto);
    }

    public Producto buscar(int id) throws SQLException {
        return service.buscar(id);
    }

    public List<Producto> listar() throws SQLException {
        return service.listar();
    }

    public List<Producto> buscarPorNombre(String texto) throws SQLException {
        return service.buscarPorNombre(texto);
    }

    public List<Producto> buscarPorProveedor(String texto) throws SQLException {
        return service.buscarPorProveedor(texto);
    }

    public List<Producto> buscarPorRubro(String texto) throws SQLException {
        return service.buscarPorRubro(texto);
    }

    public void modificar(Producto producto) throws SQLException {
        service.modificar(producto);
    }

    public void eliminar(int id) throws SQLException {
        service.eliminar(id);
    }
}
