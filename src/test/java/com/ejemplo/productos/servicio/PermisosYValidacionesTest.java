package com.ejemplo.productos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ejemplo.productos.modelo.Producto;
import com.ejemplo.productos.modelo.Proveedor;
import com.ejemplo.productos.modelo.Rol;
import com.ejemplo.productos.modelo.Rubro;
import com.ejemplo.productos.persistencia.IProductoRepository;
import com.ejemplo.productos.persistencia.IProveedorRepository;
import com.ejemplo.productos.persistencia.IRolRepository;
import com.ejemplo.productos.persistencia.IRubroRepository;

/** Pruebas sin base de datos: repositorios falsos en memoria que cuentan las escrituras. */
class PermisosYValidacionesTest {

    // ---------- repositorios falsos ----------
    static class FakeRubros implements IRubroRepository {
        int escrituras = 0;
        public void guardar(Rubro r) { escrituras++; }
        public Rubro buscarPorId(int id) { return id == 1 ? new Rubro(1, "Informática") : null; }
        public List<Rubro> listar() { return List.of(new Rubro(1, "Informática")); }
        public void modificar(Rubro r) { escrituras++; }
        public void eliminar(int id) { escrituras++; }
    }

    static class FakeProveedores implements IProveedorRepository {
        int escrituras = 0;
        public void guardar(Proveedor p) { escrituras++; }
        public Proveedor buscarPorId(int id) { return id == 1 ? new Proveedor(1, "TecnoSur", null, null) : null; }
        public List<Proveedor> listar() { return new ArrayList<>(); }
        public void modificar(Proveedor p) { escrituras++; }
        public void eliminar(int id) { escrituras++; }
    }

    static class FakeProductos implements IProductoRepository {
        int escrituras = 0;
        Producto existente; // producto que "está en la base" (puede estar inactivo)
        public void guardar(Producto p) { escrituras++; }
        public Producto buscarPorId(int id) {
            return existente != null && existente.isActivo() && existente.getId() == id ? existente : null;
        }
        public Producto buscarPorIdConInactivos(int id) {
            return existente != null && existente.getId() == id ? existente : null;
        }
        public List<Producto> listarTodos() { return new ArrayList<>(); }
        public List<Producto> listar() { return new ArrayList<>(); }
        public List<Producto> buscarPorNombre(String t) { return new ArrayList<>(); }
        public List<Producto> buscarPorProveedor(String t) { return new ArrayList<>(); }
        public List<Producto> buscarPorRubro(String t) { return new ArrayList<>(); }
        public void modificar(Producto p) { escrituras++; }
        public void eliminar(int id) { escrituras++; }
    }

    static class FakeRoles implements IRolRepository {
        int escrituras = 0;
        public Rol buscarPorPin(String pin) { return "1234".equals(pin) ? new Rol(1, "Pérez", "Ana", "e") : null; }
        public Rol buscarPorId(int id) { return new Rol(id, "X", "Y", "e"); }
        public List<Rol> listar() { return new ArrayList<>(); }
        public void guardar(Rol r, String pin) { escrituras++; }
        public void modificar(Rol r) { escrituras++; }
        public void cambiarPin(int id, String pin) { escrituras++; }
        public void eliminar(int id) { escrituras++; }
    }

    private static final Rol EMPLEADO = new Rol(1, "Pérez", "Ana", "e");
    private static final Rol ADMIN = new Rol(2, "Gómez", "Luis", "a");
    private static final Rol INVITADO = new Rol(3, "Demo", "Invitado", "i");

    private static Producto productoValido() {
        Producto p = new Producto("Monitor", "24 pulgadas", new BigDecimal("250000"), 8);
        p.setRubro(new Rubro(1, null));
        p.setProveedor(new Proveedor(1, null, null, null));
        return p;
    }

    private static ProductoService productos(Rol rol, FakeProductos repo) {
        return new ProductoService(repo, new FakeRubros(), new FakeProveedores(), new PermisosService(rol));
    }

    // ---------- login ----------
    @Test
    void loginRechazaFormatoInvalido() {
        LoginService login = new LoginService(new FakeRoles());
        assertThrows(IllegalArgumentException.class, () -> login.ingresar("12"));
        assertThrows(IllegalArgumentException.class, () -> login.ingresar("abcd"));
        assertThrows(IllegalArgumentException.class, () -> login.ingresar(""));
        assertThrows(IllegalArgumentException.class, () -> login.ingresar(null));
    }

    @Test
    void loginRechazaPinInexistenteYAceptaValido() throws SQLException {
        LoginService login = new LoginService(new FakeRoles());
        assertThrows(IllegalArgumentException.class, () -> login.ingresar("9999"));
        assertEquals("Ana Pérez", login.ingresar("1234").getNombreCompleto());
    }

    // ---------- rubros y proveedores ----------
    @Test
    void empleadoNoPuedeEscribirRubrosNiProveedores() {
        FakeRubros rubros = new FakeRubros();
        FakeProveedores provs = new FakeProveedores();
        PermisosService permisos = new PermisosService(EMPLEADO);
        RubroService rs = new RubroService(rubros, permisos);
        ProveedorService ps = new ProveedorService(provs, permisos);

        assertThrows(SecurityException.class, () -> rs.registrar(new Rubro(0, "Nuevo")));
        assertThrows(SecurityException.class, () -> rs.modificar(new Rubro(1, "Otro")));
        assertThrows(SecurityException.class, () -> rs.eliminar(1));
        assertThrows(SecurityException.class, () -> ps.registrar(new Proveedor(0, "Nuevo", null, null)));
        assertThrows(SecurityException.class, () -> ps.modificar(new Proveedor(1, "Otro", null, null)));
        assertThrows(SecurityException.class, () -> ps.eliminar(1));
        assertEquals(0, rubros.escrituras);
        assertEquals(0, provs.escrituras);
    }

    @Test
    void empleadoEInvitadoPuedenListarRubros() throws SQLException {
        assertEquals(1, new RubroService(new FakeRubros(), new PermisosService(EMPLEADO)).listar().size());
        assertEquals(1, new RubroService(new FakeRubros(), new PermisosService(INVITADO)).listar().size());
    }

    @Test
    void administradorEscribeRubrosConValidaciones() throws SQLException {
        FakeRubros rubros = new FakeRubros();
        RubroService rs = new RubroService(rubros, new PermisosService(ADMIN));
        rs.registrar(new Rubro(0, "Nuevo"));
        assertEquals(1, rubros.escrituras);
        assertThrows(IllegalArgumentException.class, () -> rs.registrar(new Rubro(0, "  ")));
        assertEquals(1, rubros.escrituras);
    }

    // ---------- productos ----------
    @Test
    void empleadoYAdminRegistranProductosValidos() throws SQLException {
        FakeProductos repo = new FakeProductos();
        productos(EMPLEADO, repo).registrar(productoValido());
        productos(ADMIN, repo).registrar(productoValido());
        assertEquals(2, repo.escrituras);
    }

    @Test
    void productoRechazaDatosInvalidos() {
        FakeProductos repo = new FakeProductos();
        ProductoService s = productos(EMPLEADO, repo);

        Producto sinNombre = productoValido(); sinNombre.setNombre("  ");
        Producto precioCero = productoValido(); precioCero.setPrecio(BigDecimal.ZERO);
        Producto stockNegativo = productoValido(); stockNegativo.setStock(-1);
        Producto sinRubro = productoValido(); sinRubro.setRubro(null);
        Producto rubroInexistente = productoValido(); rubroInexistente.setRubro(new Rubro(99, null));
        Producto provInexistente = productoValido(); provInexistente.setProveedor(new Proveedor(99, null, null, null));

        for (Producto p : List.of(sinNombre, precioCero, stockNegativo, sinRubro, rubroInexistente, provInexistente)) {
            assertThrows(IllegalArgumentException.class, () -> s.registrar(p));
        }
        assertEquals(0, repo.escrituras);
    }

    @Test
    void invitadoPuedeListarYBuscarPeroNoEscribirProductos() throws SQLException {
        FakeProductos repo = new FakeProductos();
        ProductoService s = productos(INVITADO, repo);
        assertTrue(s.listar().isEmpty());
        assertTrue(s.listarTodos().isEmpty());
        assertTrue(s.buscarPorNombre("a").isEmpty());
        assertEquals(null, s.buscar(1));
        assertThrows(SecurityException.class, () -> s.registrar(productoValido()));
        assertThrows(SecurityException.class, () -> s.modificar(productoValido()));
        assertThrows(SecurityException.class, () -> s.eliminar(1));
        assertEquals(0, repo.escrituras);
    }

    @Test
    void productoInactivoEstaBloqueado() throws SQLException {
        FakeProductos repo = new FakeProductos();
        Producto inactivo = productoValido();
        inactivo.setId(7);
        inactivo.setActivo(false);
        repo.existente = inactivo;
        ProductoService s = productos(EMPLEADO, repo);

        // Modificar datos de un inactivo (sigue inactivo): rechazado
        Producto edicion = productoValido();
        edicion.setId(7);
        edicion.setActivo(false);
        assertThrows(IllegalArgumentException.class, () -> s.modificar(edicion));
        // Darlo de baja otra vez: rechazado
        assertThrows(IllegalArgumentException.class, () -> s.eliminar(7));
        assertEquals(0, repo.escrituras);

        // Reactivarlo sí está permitido
        Producto reactivar = productoValido();
        reactivar.setId(7);
        reactivar.setActivo(true);
        s.modificar(reactivar);
        assertEquals(1, repo.escrituras);
    }

    // ---------- GestionService (fachada de la interfaz gráfica) ----------
    @Test
    void gestionServiceRequiereSesionYGuardaLaPersona() throws SQLException {
        GestionService g = new GestionService(new FakeRoles(), new FakeProductos(),
                new FakeRubros(), new FakeProveedores());
        assertThrows(SecurityException.class, g::listarProductos);
        assertThrows(SecurityException.class, () -> g.listarCatalogo("rubro"));

        assertEquals("Empleado", g.ingresar("1234").getNombreRol());
        assertEquals("Ana Pérez", g.persona().getNombreCompleto());
        assertTrue(g.listarProductos().isEmpty());
        assertEquals(1, g.listarRubros().size());
        // el empleado no puede escribir catálogos aunque llegue por la fachada
        assertThrows(SecurityException.class,
                () -> g.guardarCatalogo("rubro", new com.ejemplo.productos.modelo.ItemCatalogo(0, "Nuevo")));

        g.salir();
        assertEquals(null, g.persona());
        assertThrows(SecurityException.class, g::listarProductos);
    }

    // ---------- roles ----------
    @Test
    void soloAdminGestionaRoles() {
        FakeRoles repo = new FakeRoles();
        RolService empleado = new RolService(repo, new PermisosService(EMPLEADO));
        RolService invitado = new RolService(repo, new PermisosService(INVITADO));
        assertThrows(SecurityException.class, empleado::listar);
        assertThrows(SecurityException.class, () -> invitado.registrar("1111", "A", "B", "e"));
        assertEquals(0, repo.escrituras);
    }

    @Test
    void adminValidaRolesYSeProtegeASiMismo() throws SQLException {
        FakeRoles repo = new FakeRoles();
        RolService s = new RolService(repo, new PermisosService(ADMIN));

        s.registrar("1111", "Lopez", "Eva", "i");
        assertEquals(1, repo.escrituras);

        assertThrows(IllegalArgumentException.class, () -> s.registrar("12", "A", "B", "e"));
        assertThrows(IllegalArgumentException.class, () -> s.registrar("1111", "A", "B", "x"));
        assertThrows(IllegalArgumentException.class, () -> s.registrar("1111", " ", "B", "e"));
        assertThrows(IllegalArgumentException.class, () -> s.eliminar(ADMIN.getId()));
        assertThrows(IllegalArgumentException.class,
                () -> s.modificar(ADMIN.getId(), "Gómez", "Luis", "e", null));
        assertEquals(1, repo.escrituras);
    }

    @Test
    void rolInvalidoEnLaEntidad() {
        assertThrows(IllegalArgumentException.class, () -> new Rol(1, "A", "B", "x"));
    }
}
