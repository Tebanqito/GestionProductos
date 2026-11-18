package com.ejemplo.productos.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexión a PostgreSQL. Los valores por defecto sirven para una práctica local;
 * pueden sobrescribirse con las variables de entorno DB_URL, DB_USER y DB_PASSWORD.
 */
public class ConexionBD {

    private static final String URL = valor("DB_URL",
            "jdbc:postgresql://localhost:5432/GestionProductosDB");
    private static final String USUARIO = valor("DB_USER", "postgres");
    private static final String CLAVE = valor("DB_PASSWORD", "capua");

    private static String valor(String variable, String porDefecto) {
        String v = System.getenv(variable);
        return (v == null || v.isBlank()) ? porDefecto : v;
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
