package com.ejemplo.productos.servicio;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ejemplo.productos.modelo.ItemCatalogo;
import com.ejemplo.productos.modelo.Producto;
import com.ejemplo.productos.modelo.Proveedor;
import com.ejemplo.productos.modelo.Rol;
import com.ejemplo.productos.modelo.Rubro;
import com.ejemplo.productos.persistencia.IProductoRepository;
import com.ejemplo.productos.persistencia.IProveedorRepository;
import com.ejemplo.productos.persistencia.IRolRepository;
import com.ejemplo.productos.persistencia.IRubroRepository;
import com.ejemplo.productos.persistencia.ProductoRepository;
import com.ejemplo.productos.persistencia.ProveedorRepository;
import com.ejemplo.productos.persistencia.RolRepository;
import com.ejemplo.productos.persistencia.RubroRepository;

/**
 * Fachada para la interfaz gráfica. Conserva la sesión de la persona autenticada y delega
 * en los servicios existentes (que siguen validando datos y permisos). Las ventanas Swing
 * usan UNA sola instancia de esta clase y nunca ejecutan SQL.
 */
public class GestionService {

    public static final String RUBRO = "rubro";
    public static final String PROVEEDOR = "proveedor";

    private final IRolRepository rolRepository;
    private final IProductoRepository productoRepository;
    private final IRubroRepository rubroRepository;
    private final IProveedorRepository proveedorRepository;

    private Rol persona;
    private ProductoService productos;
    private RubroService rubros;
    private ProveedorService proveedores;

    /** Usa los repositorios de PostgreSQL. */
    public GestionService() {
        this(new RolRepository(), new ProductoRepository(),
                new RubroRepository(), new ProveedorRepository());
    }

    /** Permite inyectar otras implementaciones (por ejemplo, repositorios falsos en pruebas). */
    public GestionService(IRolRepository rolRepository, IProductoRepository productoRepository,
                          IRubroRepository rubroRepository, IProveedorRepository proveedorRepository) {
        this.rolRepository = rolRepository;
        this.productoRepository = productoRepository;
        this.rubroRepository = rubroRepository;
        this.proveedorRepository = proveedorRepository;
    }

    // ------------------------------------------------------------------
    // Sesión
    // ------------------------------------------------------------------

    public Rol ingresar(String pin) throws SQLException {
        Rol autenticada = new LoginService(rolRepository).ingresar(pin);
        PermisosService permisos = new PermisosService(autenticada);
        productos = new ProductoService(productoRepository, rubroRepository,
                proveedorRepository, permisos);
        rubros = new RubroService(rubroRepository, permisos);
        proveedores = new ProveedorService(proveedorRepository, permisos);
        persona = autenticada;
        return autenticada;
    }

    public void salir() {
        persona = null;
        productos = null;
        rubros = null;
        proveedores = null;
    }

    /** Persona autenticada, o null si no hay sesión. */
    public Rol persona() {
        return persona;
    }

    private ProductoService productos() {
        if (productos == null) throw new SecurityException("Debe iniciar sesión");
        return productos;
    }

    private RubroService rubros() {
        if (rubros == null) throw new SecurityException("Debe iniciar sesión");
        return rubros;
    }

    private ProveedorService proveedores() {
        if (proveedores == null) throw new SecurityException("Debe iniciar sesión");
        return proveedores;
    }

    // ------------------------------------------------------------------
    // Productos
    // ------------------------------------------------------------------

    /** Solo productos activos. */
    public List<Producto> listarProductos() throws SQLException {
        return productos().listar();
    }

    public List<Producto> listarProductos(boolean incluirInactivos) throws SQLException {
        return incluirInactivos ? productos().listarTodos() : productos().listar();
    }

    /** Devuelve null si no existe. Incluye productos inactivos. */
    public Producto buscarProducto(int id) throws SQLException {
        return productos().buscarConInactivos(id);
    }

    public void agregarProducto(Producto p) throws SQLException {
        productos().registrar(p);
    }

    public void modificarProducto(Producto p) throws SQLException {
        productos().modificar(p);
    }

    /** Baja lógica. */
    public void eliminarProducto(int id) throws SQLException {
        productos().eliminar(id);
    }

    // ------------------------------------------------------------------
    // Rubros y proveedores (catálogos)
    // ------------------------------------------------------------------

    public List<ItemCatalogo> listarRubros() throws SQLException {
        return listarCatalogo(RUBRO);
    }

    public List<ItemCatalogo> listarProveedores() throws SQLException {
        return listarCatalogo(PROVEEDOR);
    }

    public List<ItemCatalogo> listarCatalogo(String tipo) throws SQLException {
        List<ItemCatalogo> items = new ArrayList<>();
        if (esRubro(tipo)) {
            for (Rubro r : rubros().listar()) {
                items.add(new ItemCatalogo(r.getId(), r.getNombre()));
            }
        } else {
            for (Proveedor p : proveedores().listar()) {
                items.add(new ItemCatalogo(p.getId(), p.getRazonSocial(),
                        p.getDomicilio(), p.getTelefono()));
            }
        }
        return items;
    }

    /** Si item.getId() es 0 se da de alta; si no, se modifica. */
    public void guardarCatalogo(String tipo, ItemCatalogo item) throws SQLException {
        boolean alta = item.getId() <= 0;
        if (esRubro(tipo)) {
            Rubro r = new Rubro(item.getId(), item.getNombre());
            if (alta) rubros().registrar(r); else rubros().modificar(r);
        } else {
            Proveedor p = new Proveedor(item.getId(), item.getNombre(),
                    item.getDomicilio(), item.getTelefono());
            if (alta) proveedores().registrar(p); else proveedores().modificar(p);
        }
    }

    public void eliminarCatalogo(String tipo, int id) throws SQLException {
        if (esRubro(tipo)) {
            rubros().eliminar(id);
        } else {
            proveedores().eliminar(id);
        }
    }

    private boolean esRubro(String tipo) {
        if (RUBRO.equals(tipo)) return true;
        if (PROVEEDOR.equals(tipo)) return false;
        throw new IllegalArgumentException("Tipo de catálogo inválido: " + tipo);
    }
}
