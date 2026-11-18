package com.ejemplo.productos.presentacion;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;

import com.ejemplo.productos.servicio.GestionService;

public class VentanaLogin extends JFrame {

    private final GestionService servicio;
    private final JPasswordField txtPin = new JPasswordField(6);

    public VentanaLogin(GestionService servicio) {
        super("Gestión de Productos - Ingreso");
        this.servicio = servicio;

        setSize(380, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("INGRESO AL SISTEMA", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 10, 0, 10));
        add(titulo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(2, 1, 5, 5));
        centro.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        centro.add(new JLabel("PIN de 4 dígitos:", SwingConstants.CENTER));
        JPanel campo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        campo.add(txtPin);
        centro.add(campo);
        add(centro, BorderLayout.CENTER);

        JButton btnIngresar = new JButton("Ingresar");
        JButton btnSalir = new JButton("Salir");
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        sur.add(btnIngresar);
        sur.add(btnSalir);
        add(sur, BorderLayout.SOUTH);

        btnIngresar.addActionListener(e -> ingresar());
        txtPin.addActionListener(e -> ingresar()); // Enter dentro del campo
        btnSalir.addActionListener(e -> {
            servicio.salir();
            dispose();
            System.exit(0);
        });
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void ingresar() {
        String pin = new String(txtPin.getPassword());
        if (!pin.matches("\\d{4}")) {
            JOptionPane.showMessageDialog(this, "El PIN debe tener 4 dígitos",
                    "Dato inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        GuiUtil.ejecutar(this, () -> {
            servicio.ingresar(pin); // valida la existencia del PIN y abre la sesión
            txtPin.setText("");
            new MenuPrincipal(servicio, () -> {
                txtPin.setText("");
                setVisible(true);
            }).setVisible(true);
            setVisible(false);
        });
    }
}
