package com.ejemplo.productos.servicio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.ejemplo.productos.modelo.Producto;
import com.ejemplo.productos.modelo.Proveedor;
import com.ejemplo.productos.modelo.Rubro;
import com.ejemplo.productos.persistencia.IProductoRepository;
import com.ejemplo.productos.persistencia.IProveedorRepository;
import com.ejemplo.productos.persistencia.IRubroRepository;

public class ProductoService {

    private final IProductoRepository repository;
    private final IRubroRepository rubros;
    private final IProveedorRepository proveedores;
    private final PermisosService permisos;

    public ProductoService(IProductoRepository repository,
                           IRubroRepository rubros,
                           IProveedorRepository proveedores,
                           PermisosService permisos) {
        this.repository = Objects.requireNonNull(repository);
        this.rubros = Objects.requireNonNull(rubros);
        this.proveedores = Objects.requireNonNull(proveedores);
        this.permisos = Objects.requireNonNull(permisos);
    }

    public void registrar(Producto producto) throws SQLException {
        permisos.exigirOperador();
        validar(producto);
        repository.guardar(producto);
    }

    /** Consulta por ID (solo activos). Habilitada para todos los roles. */
    public Producto buscar(int id) throws SQLException {
        permisos.exigirSesion();
        validarId(id);
        return repository.buscarPorId(id);
    }

    /** Consulta por ID incluyendo productos dados de baja (la interfaz gráfica los muestra bloqueados). */
    public Producto buscarConInactivos(int id) throws SQLException {
        permisos.exigirSesion();
        validarId(id);
        return repository.buscarPorIdConInactivos(id);
    }

    /** Lista activos e inactivos. */
    public List<Producto> listarTodos() throws SQLException {
        permisos.exigirSesion();
        return repository.listarTodos();
    }

    /** Los listados están habilitados para todos los roles, incluido el invitado. */
    public List<Producto> listar() throws SQLException {
        permisos.exigirSesion();
        return repository.listar();
    }

    public List<Producto> buscarPorNombre(String texto) throws SQLException {
        permisos.exigirSesion();
        return repository.buscarPorNombre(textoBusqueda(texto));
    }

    public List<Producto> buscarPorProveedor(String texto) throws SQLException {
        permisos.exigirSesion();
        return repository.buscarPorProveedor(textoBusqueda(texto));
    }

    public List<Producto> buscarPorRubro(String texto) throws SQLException {
        permisos.exigirSesion();
        return repository.buscarPorRubro(textoBusqueda(texto));
    }

    public void modificar(Producto producto) throws SQLException {
        permisos.exigirOperador();
        validarId(producto.getId());
        validar(producto);
        Producto existente = repository.buscarPorIdConInactivos(producto.getId());
        if (existente == null) {
            throw new IllegalArgumentException("Producto inexistente");
        }
        // Bloqueo de inactivos: solo se puede editar un producto inactivo si se lo vuelve a activar.
        if (!existente.isActivo() && !producto.isActivo()) {
            throw new IllegalArgumentException(
                    "El producto está inactivo: no se pueden modificar sus datos. Actívelo primero");
        }
        repository.modificar(producto);
    }

    /** Baja lógica: el producto deja de mostrarse pero sigue en la base de datos. */
    public void eliminar(int id) throws SQLException {
        permisos.exigirOperador();
        validarId(id);
        Producto existente = repository.buscarPorIdConInactivos(id);
        if (existente == null) {
            throw new IllegalArgumentException("Producto inexistente");
        }
        if (!existente.isActivo()) {
            throw new IllegalArgumentException("El producto ya está dado de baja");
        }
        repository.eliminar(id);
    }

    private void validar(Producto producto) throws SQLException {
        if (producto == null) {
            throw new IllegalArgumentException("Producto inválido");
        }
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (producto.getNombre().length() > 100) {
            throw new IllegalArgumentException("El nombre admite hasta 100 caracteres");
        }
        if (producto.getDescripcion() != null && producto.getDescripcion().length() > 250) {
            throw new IllegalArgumentException("La descripción admite hasta 250 caracteres");
        }
        if (producto.getPrecio() == null
                || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        if (producto.getPrecio().scale() > 2) {
            throw new IllegalArgumentException("El precio admite hasta 2 decimales");
        }
        if (producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        validarRubro(producto);
        validarProveedor(producto);
    }

    private void validarRubro(Producto producto) throws SQLException {
        Rubro elegido = producto.getRubro();
        if (elegido == null || elegido.getId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un rubro válido");
        }
        Rubro existente = rubros.buscarPorId(elegido.getId());
        if (existente == null) {
            throw new IllegalArgumentException("El rubro seleccionado no existe");
        }
        producto.setRubro(existente);
    }

    private void validarProveedor(Producto producto) throws SQLException {
        Proveedor elegido = producto.getProveedor();
        if (elegido == null || elegido.getId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor válido");
        }
        Proveedor existente = proveedores.buscarPorId(elegido.getId());
        if (existente == null) {
            throw new IllegalArgumentException("El proveedor seleccionado no existe");
        }
        producto.setProveedor(existente);
    }

    private void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }
    }

    private String textoBusqueda(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Ingrese un texto para buscar");
        }
        return texto.trim();
    }
}
