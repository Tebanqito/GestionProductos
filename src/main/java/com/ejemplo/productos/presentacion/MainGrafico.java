package com.ejemplo.productos.presentacion;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.ejemplo.productos.servicio.GestionService;

/** Punto de entrada de la versión gráfica. Main.java (consola) se conserva. */
public class MainGrafico {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Se usa el aspecto por defecto de Swing.
            }
            new VentanaLogin(new GestionService()).setVisible(true);
        });
    }
}
