package com.ejemplo.productos.presentacion;

import java.math.BigDecimal;
import java.util.Scanner;

/** Lectura de datos por consola con reintentos ante entradas numéricas inválidas. */
public class Consola {

    private final Scanner teclado;

    public Consola(Scanner teclado) {
        this.teclado = teclado;
    }

    public String texto(String mensaje) {
        System.out.print(mensaje);
        return teclado.nextLine().trim();
    }

    public int entero(String mensaje) {
        while (true) {
            String s = texto(mensaje);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número entero");
            }
        }
    }

    /** Devuelve null si el usuario presiona Enter (conservar valor actual). */
    public Integer enteroOpcional(String mensaje) {
        while (true) {
            String s = texto(mensaje);
            if (s.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número entero (o Enter para conservar)");
            }
        }
    }

    public BigDecimal decimal(String mensaje) {
        while (true) {
            String s = texto(mensaje);
            try {
                return new BigDecimal(s.replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número (ej. 1250.50)");
            }
        }
    }

    /** Devuelve null si el usuario presiona Enter (conservar valor actual). */
    public BigDecimal decimalOpcional(String mensaje) {
        while (true) {
            String s = texto(mensaje);
            if (s.isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(s.replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número (o Enter para conservar)");
            }
        }
    }
}
