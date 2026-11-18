package com.ejemplo.productos.persistencia;

import java.sql.SQLException;

/** Identifica errores de restricciones de PostgreSQL por su SQLState. */
public final class ErroresBD {

    private ErroresBD() {
    }

    /** 23503: violación de clave foránea (ej. rubro con productos asociados). */
    public static boolean esClaveForaneaViolada(SQLException e) {
        return "23503".equals(e.getSQLState());
    }

    /** 23505: violación de UNIQUE (ej. PIN o nombre repetido). */
    public static boolean esDuplicado(SQLException e) {
        return "23505".equals(e.getSQLState());
    }
}
