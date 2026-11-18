package com.ejemplo.productos.presentacion;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

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
import com.ejemplo.productos.servicio.LoginService;
import com.ejemplo.productos.servicio.PermisosService;
import com.ejemplo.productos.servicio.ProductoService;
import com.ejemplo.productos.servicio.ProveedorService;
import com.ejemplo.productos.servicio.RolService;
import com.ejemplo.productos.servicio.RubroService;

public class Main {

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    public static void main(String[] args) {
        try (Scanner teclado = new Scanner(System.in)) {
            Consola in = new Consola(teclado);

            // 1) Autenticación
            IRolRepository rolRepository = new RolRepository();
            LoginService login = new LoginService(rolRepository);
            Rol persona = solicitarLogin(in, login);
            if (persona == null) {
                System.out.println("Fin del sistema");
                return;
            }

            // 2) Construcción de dependencias (inyección por constructor)
            PermisosService permisos = new PermisosService(persona);
            IRubroRepository rubroRepository = new RubroRepository();
            IProveedorRepository proveedorRepository = new ProveedorRepository();
            IProductoRepository productoRepository = new ProductoRepository();

            ProductoController productos = new ProductoController(new ProductoService(
                    productoRepository, rubroRepository, proveedorRepository, permisos));
            RubroController rubros = new RubroController(
                    new RubroService(rubroRepository, permisos));
            ProveedorController proveedores = new ProveedorController(
                    new ProveedorService(proveedorRepository, permisos));
            RolController roles = new RolController(new RolService(rolRepository, permisos));

            // 3) Menú principal
            int opcion;
            do {
                mostrarMenu(persona);
                opcion = in.entero("Opción: ");
                switch (opcion) {
                    case 1 -> ejecutar(() -> registrarProducto(in, productos, rubros, proveedores));
                    case 2 -> ejecutar(() -> listarProductos(productos));
                    case 3 -> ejecutar(() -> buscarProducto(in, productos));
                    case 4 -> ejecutar(() -> modificarProducto(in, productos, rubros, proveedores));
                    case 5 -> ejecutar(() -> eliminarProducto(in, productos));
                    case 6 -> ejecutar(() -> buscarProductos(in, productos));
                    case 7 -> ejecutar(() -> listarRubros(rubros));
                    case 8 -> ejecutar(() -> listarProveedores(proveedores));
                    case 9 -> ejecutar(() -> {
                        permisos.exigirAdministrador();
                        menuRubros(in, rubros);
                    });
                    case 10 -> ejecutar(() -> {
                        permisos.exigirAdministrador();
                        menuProveedores(in, proveedores);
                    });
                    case 11 -> ejecutar(() -> {
                        permisos.exigirAdministrador();
                        menuRoles(in, roles);
                    });
                    case 0 -> System.out.println("Fin del sistema");
                    default -> System.out.println("Opción incorrecta");
                }
            } while (opcion != 0);
        }
    }

    // ------------------------------------------------------------------
    // Login y menú
    // ------------------------------------------------------------------

    private static Rol solicitarLogin(Consola in, LoginService login) {
        while (true) {
            System.out.println("\n--- INGRESO AL SISTEMA ---");
            String pin = in.texto("PIN de 4 dígitos o 0 para salir: ");
            if ("0".equals(pin)) {
                return null;
            }
            try {
                return login.ingresar(pin);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (SQLException e) {
                System.out.println("No se pudo acceder a PostgreSQL. "
                        + "Verifique conexión y tabla roles.");
                return null;
            }
        }
    }

    private static void mostrarMenu(Rol persona) {
        System.out.println("\n--- GESTIÓN DE PRODUCTOS ---");
        System.out.println("Persona: " + persona.getNombreCompleto());
        System.out.println("Rol: " + persona.getNombreRol());
        if (!persona.esInvitado()) {
            System.out.println("1. Registrar producto");
        }
        System.out.println("2. Listar productos");
        System.out.println("3. Buscar producto por ID");
        if (!persona.esInvitado()) {
            System.out.println("4. Modificar producto");
            System.out.println("5. Eliminar producto (baja lógica)");
        }
        System.out.println("6. Buscar productos por nombre, proveedor o rubro");
        System.out.println("7. Listar rubros");
        System.out.println("8. Listar proveedores");
        if (persona.esAdministrador()) {
            System.out.println("9. Gestionar rubros");
            System.out.println("10. Gestionar proveedores");
            System.out.println("11. Gestionar roles");
        }
        System.out.println("0. Salir");
    }

    private static void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (SecurityException e) {
            System.out.println("Acceso denegado: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Dato inválido: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("No fue posible acceder a la base de datos.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Productos
    // ------------------------------------------------------------------

    private static void registrarProducto(Consola in, ProductoController productos,
                                          RubroController rubros,
                                          ProveedorController proveedores) throws Exception {
        String nombre = in.texto("Nombre: ");
        String descripcion = in.texto("Descripción: ");
        BigDecimal precio = in.decimal("Precio: ");
        int stock = in.entero("Stock: ");
        System.out.println("Rubros disponibles:");
        listarRubros(rubros);
        int rubroId = in.entero("ID de rubro: ");
        System.out.println("Proveedores disponibles:");
        listarProveedores(proveedores);
        int proveedorId = in.entero("ID de proveedor: ");

        productos.registrar(nombre, descripcion, precio, stock, rubroId, proveedorId);
        System.out.println("Producto registrado");
    }

    private static void listarProductos(ProductoController productos) throws Exception {
        mostrarProductos(productos.listar());
    }

    private static void mostrarProductos(List<Producto> lista) {
        if (lista.isEmpty()) {
            System.out.println("No hay productos para mostrar");
            return;
        }
        for (Producto p : lista) {
            System.out.println(p.getId() + " - " + p.getNombre()
                    + " - $" + p.getPrecio()
                    + " - Stock: " + p.getStock()
                    + " - Rubro: " + p.getRubro().getNombre()
                    + " - Proveedor: " + p.getProveedor().getRazonSocial());
        }
    }

    private static void buscarProducto(Consola in, ProductoController productos)
            throws Exception {
        int id = in.entero("ID: ");
        Producto p = productos.buscar(id);
        if (p == null) {
            System.out.println("Producto inexistente");
            return;
        }
        System.out.println("Nombre: " + p.getNombre());
        System.out.println("Descripción: " + p.getDescripcion());
        System.out.println("Precio: $" + p.getPrecio());
        System.out.println("Stock: " + p.getStock());
        System.out.println("Fecha de alta: " + p.getFechaAlta());
        System.out.println("Rubro: " + p.getRubro().getNombre());
        System.out.println("Proveedor: " + p.getProveedor().getRazonSocial());
    }

    private static void buscarProductos(Consola in, ProductoController productos)
            throws Exception {
        System.out.println("Buscar por: 1. Nombre de producto  2. Nombre de proveedor  3. Rubro");
        int criterio = in.entero("Criterio: ");
        if (criterio < 1 || criterio > 3) {
            System.out.println("Opción incorrecta");
            return;
        }
        String texto = in.texto("Texto a buscar: ");
        List<Producto> resultado = switch (criterio) {
            case 1 -> productos.buscarPorNombre(texto);
            case 2 -> productos.buscarPorProveedor(texto);
            default -> productos.buscarPorRubro(texto);
        };
        mostrarProductos(resultado);
    }

    private static void modificarProducto(Consola in, ProductoController productos,
                                          RubroController rubros,
                                          ProveedorController proveedores) throws Exception {
        int id = in.entero("ID a modificar: ");
        Producto p = productos.buscar(id);
        if (p == null) {
            System.out.println("Producto inexistente");
            return;
        }
        System.out.println("(Enter conserva el valor actual)");

        String nombre = in.texto("Nuevo nombre [" + p.getNombre() + "]: ");
        if (!nombre.isEmpty()) p.setNombre(nombre);

        String desc = in.texto("Nueva descripción [" + p.getDescripcion() + "]: ");
        if (!desc.isEmpty()) p.setDescripcion(desc);

        BigDecimal precio = in.decimalOpcional("Nuevo precio [" + p.getPrecio() + "]: ");
        if (precio != null) p.setPrecio(precio);

        Integer stock = in.enteroOpcional("Nuevo stock [" + p.getStock() + "]: ");
        if (stock != null) p.setStock(stock);

        System.out.println("Rubros disponibles:");
        listarRubros(rubros);
        Integer rubroId = in.enteroOpcional(
                "Nuevo ID de rubro [" + p.getRubro().getId() + " - " + p.getRubro().getNombre() + "]: ");
        if (rubroId != null) p.setRubro(new Rubro(rubroId, null));

        System.out.println("Proveedores disponibles:");
        listarProveedores(proveedores);
        Integer provId = in.enteroOpcional(
                "Nuevo ID de proveedor [" + p.getProveedor().getId() + " - "
                        + p.getProveedor().getRazonSocial() + "]: ");
        if (provId != null) p.setProveedor(new Proveedor(provId, null, null, null));

        productos.modificar(p);
        System.out.println("Producto modificado");
    }

    private static void eliminarProducto(Consola in, ProductoController productos)
            throws Exception {
        int id = in.entero("ID a eliminar: ");
        productos.eliminar(id);
        System.out.println("Producto dado de baja (baja lógica)");
    }

    // ------------------------------------------------------------------
    // Rubros
    // ------------------------------------------------------------------

    private static void listarRubros(RubroController rubros) throws Exception {
        List<Rubro> lista = rubros.listar();
        if (lista.isEmpty()) {
            System.out.println("No hay rubros cargados");
        }
        for (Rubro r : lista) {
            System.out.println(r);
        }
    }

    private static void menuRubros(Consola in, RubroController rubros) {
        int op;
        do {
            System.out.println("\n--- GESTIÓN DE RUBROS ---");
            System.out.println("1. Alta  2. Listar  3. Modificar  4. Eliminar  0. Volver");
            op = in.entero("Opción: ");
            switch (op) {
                case 1 -> ejecutar(() -> {
                    rubros.registrar(in.texto("Nombre del rubro: "));
                    System.out.println("Rubro registrado");
                });
                case 2 -> ejecutar(() -> listarRubros(rubros));
                case 3 -> ejecutar(() -> {
                    Rubro r = rubros.buscar(in.entero("ID a modificar: "));
                    if (r == null) {
                        System.out.println("Rubro inexistente");
                        return;
                    }
                    String nombre = in.texto("Nuevo nombre [" + r.getNombre() + "]: ");
                    if (!nombre.isEmpty()) r.setNombre(nombre);
                    rubros.modificar(r);
                    System.out.println("Rubro modificado");
                });
                case 4 -> ejecutar(() -> {
                    rubros.eliminar(in.entero("ID a eliminar: "));
                    System.out.println("Rubro eliminado");
                });
                case 0 -> { }
                default -> System.out.println("Opción incorrecta");
            }
        } while (op != 0);
    }

    // ------------------------------------------------------------------
    // Proveedores
    // ------------------------------------------------------------------

    private static void listarProveedores(ProveedorController proveedores) throws Exception {
        List<Proveedor> lista = proveedores.listar();
        if (lista.isEmpty()) {
            System.out.println("No hay proveedores cargados");
        }
        for (Proveedor p : lista) {
            System.out.println(p);
        }
    }

    private static void menuProveedores(Consola in, ProveedorController proveedores) {
        int op;
        do {
            System.out.println("\n--- GESTIÓN DE PROVEEDORES ---");
            System.out.println("1. Alta  2. Listar  3. Modificar  4. Eliminar  0. Volver");
            op = in.entero("Opción: ");
            switch (op) {
                case 1 -> ejecutar(() -> {
                    String razon = in.texto("Razón social: ");
                    String dom = in.texto("Domicilio: ");
                    String tel = in.texto("Teléfono: ");
                    proveedores.registrar(razon, dom, tel);
                    System.out.println("Proveedor registrado");
                });
                case 2 -> ejecutar(() -> listarProveedores(proveedores));
                case 3 -> ejecutar(() -> {
                    Proveedor p = proveedores.buscar(in.entero("ID a modificar: "));
                    if (p == null) {
                        System.out.println("Proveedor inexistente");
                        return;
                    }
                    System.out.println("(Enter conserva el valor actual)");
                    String razon = in.texto("Razón social [" + p.getRazonSocial() + "]: ");
                    if (!razon.isEmpty()) p.setRazonSocial(razon);
                    String dom = in.texto("Domicilio [" + p.getDomicilio() + "]: ");
                    if (!dom.isEmpty()) p.setDomicilio(dom);
                    String tel = in.texto("Teléfono [" + p.getTelefono() + "]: ");
                    if (!tel.isEmpty()) p.setTelefono(tel);
                    proveedores.modificar(p);
                    System.out.println("Proveedor modificado");
                });
                case 4 -> ejecutar(() -> {
                    proveedores.eliminar(in.entero("ID a eliminar: "));
                    System.out.println("Proveedor eliminado");
                });
                case 0 -> { }
                default -> System.out.println("Opción incorrecta");
            }
        } while (op != 0);
    }

    // ------------------------------------------------------------------
    // Roles (solo administrador)
    // ------------------------------------------------------------------

    private static void listarRoles(RolController roles) throws Exception {
        for (Rol r : roles.listar()) {
            System.out.println(r.getId() + " - " + r.getNombreCompleto()
                    + " - " + r.getNombreRol() + " (" + r.getRol() + ")");
        }
    }

    private static void menuRoles(Consola in, RolController roles) {
        int op;
        do {
            System.out.println("\n--- GESTIÓN DE ROLES ---");
            System.out.println("1. Alta  2. Listar  3. Modificar  4. Eliminar  0. Volver");
            op = in.entero("Opción: ");
            switch (op) {
                case 1 -> ejecutar(() -> {
                    String pin = in.texto("PIN (4 dígitos): ");
                    String apellido = in.texto("Apellido: ");
                    String nombre = in.texto("Nombre: ");
                    String tipo = in.texto("Rol (e = empleado, a = administrador, i = invitado): ");
                    roles.registrar(pin, apellido, nombre, tipo);
                    System.out.println("Persona registrada");
                });
                case 2 -> ejecutar(() -> listarRoles(roles));
                case 3 -> ejecutar(() -> {
                    Rol r = roles.buscar(in.entero("ID a modificar: "));
                    if (r == null) {
                        System.out.println("Rol inexistente");
                        return;
                    }
                    System.out.println("(Enter conserva el valor actual)");
                    String apellido = in.texto("Apellido [" + r.getApellido() + "]: ");
                    String nombre = in.texto("Nombre [" + r.getNombre() + "]: ");
                    String tipo = in.texto("Rol e/a/i [" + r.getRol() + "]: ");
                    String pin = in.texto("Nuevo PIN (4 dígitos): ");
                    roles.modificar(r.getId(),
                            apellido.isEmpty() ? r.getApellido() : apellido,
                            nombre.isEmpty() ? r.getNombre() : nombre,
                            tipo.isEmpty() ? r.getRol() : tipo,
                            pin);
                    System.out.println("Persona modificada");
                });
                case 4 -> ejecutar(() -> {
                    roles.eliminar(in.entero("ID a eliminar: "));
                    System.out.println("Persona eliminada");
                });
                case 0 -> { }
                default -> System.out.println("Opción incorrecta");
            }
        } while (op != 0);
    }
}
