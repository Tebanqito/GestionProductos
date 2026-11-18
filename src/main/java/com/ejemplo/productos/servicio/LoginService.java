package com.ejemplo.productos.servicio;

import java.sql.SQLException;
import java.util.Objects;

import com.ejemplo.productos.modelo.Rol;
import com.ejemplo.productos.persistencia.IRolRepository;

public class LoginService {

    private final IRolRepository repository;

    public LoginService(IRolRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Rol ingresar(String pin) throws SQLException {
        if (pin == null || !pin.matches("[0-9]{4}")) {
            throw new IllegalArgumentException("El PIN debe contener exactamente 4 dígitos");
        }
        Rol persona = repository.buscarPorPin(pin);
        if (persona == null) {
            throw new IllegalArgumentException("PIN incorrecto");
        }
        return persona;
    }
}
