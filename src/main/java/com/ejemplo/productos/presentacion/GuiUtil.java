package com.ejemplo.productos.presentacion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.ejemplo.productos.modelo.Rol;

/** Utilidades compartidas por las ventanas Swing (sin lógica de negocio ni SQL). */
final class GuiUtil {

    private GuiUtil() {
    }

    @FunctionalInterface
    interface Accion {
        void ejecutar() throws Exception;
    }

    /** Ejecuta una acción y traduce las excepciones a mensajes con JOptionPane. */
    static void ejecutar(Component padre, Accion accion) {
        try {
            accion.ejecutar();
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(padre, e.getMessage(),
                    "Acceso denegado", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(padre, e.getMessage(),
                    "Dato inválido", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(padre,
                    "No se pudo acceder a la base de datos:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(padre, "Error inesperado: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Parte superior de las ventanas: título y datos del perfil (nombre y rol). */
    static JPanel encabezado(String titulo, Rol persona) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));
        JLabel lblUsuario = new JLabel("Usuario: " + persona.getNombreCompleto()
                + "  |  Perfil: " + persona.getNombreRol());
        panel.add(lblTitulo, BorderLayout.WEST);
        panel.add(lblUsuario, BorderLayout.EAST);
        return panel;
    }
}
