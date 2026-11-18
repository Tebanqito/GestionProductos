package com.ejemplo.productos.servicio;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import com.ejemplo.productos.modelo.Rol;
import com.ejemplo.productos.persistencia.ErroresBD;
import com.ejemplo.productos.persistencia.IRolRepository;

/** ABM de roles (personas con PIN). Exclusivo del administrador. */
public class RolService {

    private final IRolRepository repository;
    private final PermisosService permisos;

    public RolService(IRolRepository repository, PermisosService permisos) {
        this.repository = Objects.requireNonNull(repository);
        this.permisos = Objects.requireNonNull(permisos);
    }

    public List<Rol> listar() throws SQLException {
        permisos.exigirAdministrador();
        return repository.listar();
    }

    public Rol buscar(int id) throws SQLException {
        permisos.exigirAdministrador();
        validarId(id);
        return repository.buscarPorId(id);
    }

    public void registrar(String pin, String apellido, String nombre, String tipoRol)
            throws SQLException {
        permisos.exigirAdministrador();
        validarPin(pin);
        validarTexto(apellido, "El apellido");
        validarTexto(nombre, "El nombre");
        Rol nueva = new Rol(0, apellido.trim(), nombre.trim(), validarTipo(tipoRol));
        try {
            repository.guardar(nueva, pin);
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    /** Si nuevoPin es null o vacío, el PIN actual se conserva. */
    public void modificar(int id, String apellido, String nombre, String tipoRol, String nuevoPin)
            throws SQLException {
        permisos.exigirAdministrador();
        validarId(id);
        validarTexto(apellido, "El apellido");
        validarTexto(nombre, "El nombre");
        String tipo = validarTipo(tipoRol);
        if (id == permisos.getPersona().getId() && !Rol.ADMINISTRADOR.equals(tipo)) {
            throw new IllegalArgumentException(
                    "No puede quitarse a sí mismo el rol de administrador");
        }
        if (repository.buscarPorId(id) == null) {
            throw new IllegalArgumentException("Rol inexistente");
        }
        boolean cambiaPin = nuevoPin != null && !nuevoPin.isBlank();
        if (cambiaPin) {
            validarPin(nuevoPin);
        }
        try {
            repository.modificar(new Rol(id, apellido.trim(), nombre.trim(), tipo));
            if (cambiaPin) {
                repository.cambiarPin(id, nuevoPin);
            }
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    public void eliminar(int id) throws SQLException {
        permisos.exigirAdministrador();
        validarId(id);
        if (id == permisos.getPersona().getId()) {
            throw new IllegalArgumentException("No puede eliminar su propia persona");
        }
        if (repository.buscarPorId(id) == null) {
            throw new IllegalArgumentException("Rol inexistente");
        }
        repository.eliminar(id);
    }

    private void validarPin(String pin) {
        if (pin == null || !pin.matches("[0-9]{4}")) {
            throw new IllegalArgumentException("El PIN debe contener exactamente 4 dígitos");
        }
    }

    private void validarTexto(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio");
        }
        if (texto.length() > 80) {
            throw new IllegalArgumentException(campo + " admite hasta 80 caracteres");
        }
    }

    private String validarTipo(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Rol inválido (use e, a o i)");
        }
        String t = tipo.trim().toLowerCase();
        if (!Rol.EMPLEADO.equals(t) && !Rol.ADMINISTRADOR.equals(t) && !Rol.INVITADO.equals(t)) {
            throw new IllegalArgumentException("Rol inválido (use e, a o i)");
        }
        return t;
    }

    private void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }
    }

    private SQLException traducir(SQLException e) {
        if (ErroresBD.esDuplicado(e)) {
            throw new IllegalArgumentException("Ese PIN ya está en uso");
        }
        return e;
    }
}
