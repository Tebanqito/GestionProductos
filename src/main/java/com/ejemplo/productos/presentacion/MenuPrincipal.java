package com.ejemplo.productos.presentacion;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import com.ejemplo.productos.servicio.GestionService;

public class MenuPrincipal extends JFrame {

    private final GestionService servicio;
    private final Runnable volverLogin;

    public MenuPrincipal(GestionService servicio, Runnable volverLogin) {
        super("Gestión de Productos - " + servicio.persona().getNombreRol());
        this.servicio = servicio;
        this.volverLogin = volverLogin;

        setSize(650, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        add(GuiUtil.encabezado("Gestión de Productos", servicio.persona()), BorderLayout.NORTH);

        JLabel bienvenida = new JLabel("<html><div style='text-align:center'>Bienvenido/a, "
                + servicio.persona().getNombreCompleto()
                + "<br><br>Perfil: " + servicio.persona().getNombreRol()
                + "<br><br>Use el menú <b>Gestión</b> para elegir una opción.</div></html>",
                SwingConstants.CENTER);
        bienvenida.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(bienvenida, BorderLayout.CENTER);

        JMenuBar barra = new JMenuBar();
        JMenu gestion = new JMenu("Gestión");
        JMenuItem productos = new JMenuItem("Productos");
        JMenuItem rubros = new JMenuItem("Rubros");
        JMenuItem proveedores = new JMenuItem("Proveedores");
        gestion.add(productos);
        gestion.add(rubros);
        gestion.add(proveedores);
        barra.add(gestion);

        JMenu salir = new JMenu("Salir");
        JMenuItem cerrarSesion = new JMenuItem("Cerrar sesión");
        JMenuItem salirSistema = new JMenuItem("Salir de la aplicación");
        salir.add(cerrarSesion);
        salir.add(salirSistema);
        barra.add(salir);
        setJMenuBar(barra);

        productos.addActionListener(e -> abrirProductos());
        rubros.addActionListener(e -> abrirRubros());
        proveedores.addActionListener(e -> abrirProveedores());
        cerrarSesion.addActionListener(e -> cerrarSesion());
        salirSistema.addActionListener(e -> terminar());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                terminar();
            }
        });
    }

    private void abrirProductos() {
        setVisible(false);
        new VentanaProductosCRUD(servicio, () -> setVisible(true)).setVisible(true);
    }

    private void abrirRubros() {
        setVisible(false);
        new VentanaRubros(servicio, () -> setVisible(true)).setVisible(true);
    }

    private void abrirProveedores() {
        setVisible(false);
        new VentanaProveedores(servicio, () -> setVisible(true)).setVisible(true);
    }

    private void cerrarSesion() {
        servicio.salir();
        dispose();
        volverLogin.run();
    }

    private void terminar() {
        int r = JOptionPane.showConfirmDialog(this, "¿Salir de la aplicación?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            servicio.salir();
            System.exit(0);
        }
    }
}
