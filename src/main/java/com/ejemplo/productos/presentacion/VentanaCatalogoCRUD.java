package com.ejemplo.productos.presentacion;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.ejemplo.productos.modelo.ItemCatalogo;
import com.ejemplo.productos.servicio.GestionService;

/**
 * ABM de un catálogo (rubros o proveedores). Todos los perfiles pueden listar y buscar;
 * solo el administrador puede agregar, modificar y eliminar.
 */
public class VentanaCatalogoCRUD extends JFrame {

    private final GestionService servicio;
    private final String tipo;
    private final boolean esProveedor;
    private final boolean puedeEditar;

    private final DefaultTableModel modelo;
    private final JTable tabla;

    private final JTextField txtId = new JTextField(6);
    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtDomicilio = new JTextField(25);
    private final JTextField txtTelefono = new JTextField(15);
    private final JTextField txtBuscar = new JTextField(14);

    private final JButton btnNuevo = new JButton("Nuevo / Limpiar");
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnListar = new JButton("Listar todo");
    private final JButton btnVolver = new JButton("Volver al menú");

    private List<ItemCatalogo> datos = new ArrayList<>();
    private boolean cargando;

    /** @param tipo GestionService.RUBRO o GestionService.PROVEEDOR */
    public VentanaCatalogoCRUD(GestionService servicio, String tipo, Runnable volverAlMenu) {
        super(GestionService.PROVEEDOR.equals(tipo) ? "Gestión de proveedores" : "Gestión de rubros");
        this.servicio = servicio;
        this.tipo = tipo;
        this.esProveedor = GestionService.PROVEEDOR.equals(tipo);
        this.puedeEditar = servicio.persona().esAdministrador();

        setSize(esProveedor ? 780 : 640, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(GuiUtil.encabezado(esProveedor ? "Proveedores" : "Rubros", servicio.persona()),
                BorderLayout.NORTH);

        String[] columnas = esProveedor
                ? new String[]{"ID", "Razón social", "Domicilio", "Teléfono"}
                : new String[]{"ID", "Nombre"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columna) {
                return columna == 0 ? Integer.class : String.class;
            }
        };
        tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(600, 200));
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        add(scroll, BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(construirFormulario(), BorderLayout.CENTER);
        sur.add(construirBotones(), BorderLayout.SOUTH);
        add(sur, BorderLayout.SOUTH);

        conectarEventos(volverAlMenu);
        GuiUtil.ejecutar(this, this::listar);
        limpiar();
    }

    private JPanel construirFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        txtId.setEditable(false);

        int fila = 0;
        fila = campo(p, fila, "ID:", txtId);
        fila = campo(p, fila, esProveedor ? "Razón social:" : "Nombre:", txtNombre);
        if (esProveedor) {
            fila = campo(p, fila, "Domicilio:", txtDomicilio);
            campo(p, fila, "Teléfono:", txtTelefono);
        }
        return p;
    }

    private int campo(JPanel panel, int fila, String etiqueta, JTextField campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.gridy = fila;
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(campo, c);
        return fila + 1;
    }

    private JPanel construirBotones() {
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        acciones.add(btnNuevo);
        acciones.add(btnGuardar);
        acciones.add(btnEliminar);
        acciones.add(btnVolver);

        JPanel consulta = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        consulta.add(new JLabel("Buscar (ID o nombre):"));
        consulta.add(txtBuscar);
        consulta.add(btnBuscar);
        consulta.add(btnListar);

        JPanel p = new JPanel(new java.awt.GridLayout(2, 1));
        p.setBorder(BorderFactory.createEmptyBorder(4, 0, 6, 0));
        p.add(acciones);
        p.add(consulta);
        return p;
    }

    private void conectarEventos(Runnable volverAlMenu) {
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || cargando) return;
            int fila = tabla.getSelectedRow();
            if (fila < 0) return;
            int id = (Integer) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
            for (ItemCatalogo item : datos) {
                if (item.getId() == id) {
                    mostrar(item);
                    return;
                }
            }
        });

        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnBuscar.addActionListener(e -> buscar());
        txtBuscar.addActionListener(e -> buscar());
        btnListar.addActionListener(e -> GuiUtil.ejecutar(this, () -> {
            txtBuscar.setText("");
            limpiar();
            listar();
        }));
        btnVolver.addActionListener(e -> dispose());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                volverAlMenu.run();
            }
        });
    }

    // ------------------------------------------------------------------

    /** Consulta el servicio y muestra todo. */
    private void listar() throws Exception {
        datos = servicio.listarCatalogo(tipo);
        mostrarEnTabla(datos);
    }

    private void mostrarEnTabla(List<ItemCatalogo> items) {
        cargando = true;
        try {
            quitarSeleccion();
            modelo.setRowCount(0);
            for (ItemCatalogo i : items) {
                if (esProveedor) {
                    modelo.addRow(new Object[]{i.getId(), i.getNombre(),
                            i.getDomicilio(), i.getTelefono()});
                } else {
                    modelo.addRow(new Object[]{i.getId(), i.getNombre()});
                }
            }
        } finally {
            cargando = false;
        }
    }

    /** Filtra por ID exacto o por texto contenido en el nombre. */
    private void buscar() {
        GuiUtil.ejecutar(this, () -> {
            listar();
            String texto = txtBuscar.getText().trim().toLowerCase();
            if (texto.isEmpty()) return;
            List<ItemCatalogo> filtrados = new ArrayList<>();
            for (ItemCatalogo i : datos) {
                if (i.getNombre().toLowerCase().contains(texto)
                        || String.valueOf(i.getId()).equals(texto)) {
                    filtrados.add(i);
                }
            }
            mostrarEnTabla(filtrados);
            limpiar();
            if (filtrados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron resultados",
                        "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void mostrar(ItemCatalogo item) {
        txtId.setText(String.valueOf(item.getId()));
        txtNombre.setText(item.getNombre());
        txtDomicilio.setText(item.getDomicilio() == null ? "" : item.getDomicilio());
        txtTelefono.setText(item.getTelefono() == null ? "" : item.getTelefono());
        aplicarEstado();
    }

    private void limpiar() {
        txtId.setText("");
        txtNombre.setText("");
        txtDomicilio.setText("");
        txtTelefono.setText("");
        cargando = true;
        try {
            quitarSeleccion();
        } finally {
            cargando = false;
        }
        aplicarEstado();
    }

    private void aplicarEstado() {
        boolean haySeleccion = !txtId.getText().isEmpty();
        txtNombre.setEditable(puedeEditar);
        txtDomicilio.setEditable(puedeEditar);
        txtTelefono.setEditable(puedeEditar);
        btnNuevo.setEnabled(true);
        btnGuardar.setEnabled(puedeEditar);
        btnEliminar.setEnabled(puedeEditar && haySeleccion);
    }

    private String nullSiVacio(String s) {
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void guardar() {
        GuiUtil.ejecutar(this, () -> {
            int id = txtId.getText().isEmpty() ? 0 : Integer.parseInt(txtId.getText());
            ItemCatalogo item = new ItemCatalogo(id, txtNombre.getText().trim(),
                    nullSiVacio(txtDomicilio.getText()), nullSiVacio(txtTelefono.getText()));
            servicio.guardarCatalogo(tipo, item);
            JOptionPane.showMessageDialog(this, id == 0 ? "Registro agregado" : "Registro modificado");
            limpiar();
            listar();
        });
    }

    private void eliminar() {
        GuiUtil.ejecutar(this, () -> {
            int r = JOptionPane.showConfirmDialog(this,
                    "¿Querés eliminar \"" + txtNombre.getText() + "\"?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
            servicio.eliminarCatalogo(tipo, Integer.parseInt(txtId.getText()));
            JOptionPane.showMessageDialog(this, "Registro eliminado");
            limpiar();
            listar();
        });
    }

    /** Desmarca la tabla y también el ancla/cabeza de la selección (evita avisos internos del sorter). */
    private void quitarSeleccion() {
        tabla.clearSelection();
        tabla.getSelectionModel().setAnchorSelectionIndex(-1);
        tabla.getSelectionModel().setLeadSelectionIndex(-1);
    }
}
