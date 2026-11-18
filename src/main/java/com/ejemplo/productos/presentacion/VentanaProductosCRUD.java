package com.ejemplo.productos.presentacion;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import com.ejemplo.productos.modelo.Producto;
import com.ejemplo.productos.modelo.Proveedor;
import com.ejemplo.productos.modelo.Rubro;
import com.ejemplo.productos.servicio.GestionService;

/**
 * ABM de productos. Toda operación pasa por GestionService (sin SQL en la ventana).
 * Un producto inactivo se muestra bloqueado: solo puede reactivarse tildando "Activo".
 */
public class VentanaProductosCRUD extends JFrame {

    private static final String[] COLUMNAS =
            {"ID", "Nombre", "Descripción", "Precio", "Stock", "Activo", "Rubro", "Proveedor"};

    private final GestionService servicio;
    private final boolean puedeEditar;

    private final DefaultTableModel modelo;
    private final JTable tabla;

    private final JTextField txtId = new JTextField(6);
    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtDescripcion = new JTextField(20);
    private final JTextField txtPrecio = new JTextField(10);
    private final JTextField txtStock = new JTextField(6);
    private final JComboBox<ItemCatalogo> cmbRubro = new JComboBox<>();
    private final JComboBox<ItemCatalogo> cmbProveedor = new JComboBox<>();
    private final JCheckBox chkActivo = new JCheckBox("Producto activo", true);

    private final JTextField txtBuscarId = new JTextField(5);
    private final JCheckBox chkVerInactivos = new JCheckBox("Ver inactivos", true);

    private final JButton btnNuevo = new JButton("Nuevo / Limpiar");
    private final JButton btnGuardar = new JButton("Guardar (alta)");
    private final JButton btnModificar = new JButton("Modificar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnBuscar = new JButton("Buscar por ID");
    private final JButton btnActualizar = new JButton("Actualizar listado");
    private final JButton btnVolver = new JButton("Volver al menú");

    private Producto cargado;      // producto mostrado en el formulario (null = alta nueva)
    private boolean cargando;      // evita reacciones en cadena de los listeners

    public VentanaProductosCRUD(GestionService servicio, Runnable volverAlMenu) {
        super("Gestión de Productos");
        this.servicio = servicio;
        this.puedeEditar = !servicio.persona().esInvitado();

        setSize(1020, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(GuiUtil.encabezado("Productos", servicio.persona()), BorderLayout.NORTH);

        modelo = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columna) {
                return switch (columna) {
                    case 0, 4 -> Integer.class;
                    case 3 -> BigDecimal.class;
                    case 5 -> Boolean.class;
                    default -> String.class;
                };
            }
        };
        tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.getColumnModel().getColumn(4).setMaxWidth(70);
        tabla.getColumnModel().getColumn(5).setMaxWidth(60);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(900, 230));
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        add(scroll, BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(construirFormulario(), BorderLayout.CENTER);
        sur.add(construirBotones(), BorderLayout.SOUTH);
        add(sur, BorderLayout.SOUTH);

        conectarEventos(volverAlMenu);

        GuiUtil.ejecutar(this, () -> {
            cargarCombos();
            cargar();
        });
        limpiar();
    }

    // ------------------------------------------------------------------
    // Construcción de la pantalla
    // ------------------------------------------------------------------

    private JPanel construirFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        txtId.setEditable(false); // el ID siempre es de solo lectura

        campo(p, 0, 0, "ID:", txtId);
        campo(p, 0, 1, "Nombre:", txtNombre);
        campo(p, 1, 0, "Descripción:", txtDescripcion);
        campo(p, 1, 1, "Precio:", txtPrecio);
        campo(p, 2, 0, "Stock:", txtStock);
        campo(p, 2, 1, "Rubro:", cmbRubro);
        campo(p, 3, 0, "Proveedor:", cmbProveedor);
        campo(p, 3, 1, "Estado:", chkActivo);
        return p;
    }

    private void campo(JPanel panel, int fila, int columna, String etiqueta, JComponent componente) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.gridy = fila;
        c.anchor = GridBagConstraints.WEST;

        c.gridx = columna * 2;
        panel.add(new JLabel(etiqueta), c);

        c.gridx = columna * 2 + 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(componente, c);
    }

    private JPanel construirBotones() {
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        acciones.add(btnNuevo);
        acciones.add(btnGuardar);
        acciones.add(btnModificar);
        acciones.add(btnEliminar);
        acciones.add(btnVolver);

        JPanel consulta = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        consulta.add(new JLabel("ID:"));
        consulta.add(txtBuscarId);
        consulta.add(btnBuscar);
        consulta.add(btnActualizar);
        consulta.add(chkVerInactivos);

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
            GuiUtil.ejecutar(this, () -> {
                Producto p = servicio.buscarProducto(id);
                if (p != null) mostrar(p);
            });
        });

        // Producto destildado: se restauran los datos originales y quedan bloqueados.
        chkActivo.addActionListener(e -> {
            if (cargado != null && !chkActivo.isSelected()) {
                restaurarCampos(cargado);
            }
            aplicarEstado();
        });

        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnModificar.addActionListener(e -> modificar());
        btnEliminar.addActionListener(e -> eliminar());
        btnBuscar.addActionListener(e -> buscar());
        txtBuscarId.addActionListener(e -> buscar());
        btnActualizar.addActionListener(e -> GuiUtil.ejecutar(this, () -> {
            cargarCombos();
            limpiar();
            cargar();
        }));
        chkVerInactivos.addActionListener(e -> GuiUtil.ejecutar(this, () -> {
            limpiar();
            cargar();
        }));
        btnVolver.addActionListener(e -> dispose());

        // Tanto "Volver" como la X cierran la ventana y reabren el menú.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                volverAlMenu.run();
            }
        });
    }

    // ------------------------------------------------------------------
    // Carga de datos
    // ------------------------------------------------------------------

    private void cargarCombos() throws Exception {
        rellenar(cmbRubro, servicio.listarRubros());
        rellenar(cmbProveedor, servicio.listarProveedores());
    }

    private void rellenar(JComboBox<ItemCatalogo> combo, java.util.List<ItemCatalogo> items) {
        combo.removeAllItems();
        for (ItemCatalogo i : items) {
            combo.addItem(i);
        }
        combo.setSelectedIndex(-1);
    }

    private void cargar() throws Exception {
        cargando = true;
        try {
            quitarSeleccion();
            modelo.setRowCount(0);
            for (Producto p : servicio.listarProductos(chkVerInactivos.isSelected())) {
                modelo.addRow(new Object[]{p.getId(), p.getNombre(), p.getDescripcion(),
                        p.getPrecio(), p.getStock(), p.isActivo(),
                        p.getRubro().getNombre(), p.getProveedor().getRazonSocial()});
            }
        } finally {
            cargando = false;
        }
    }

    private void seleccionarFila(int id) {
        cargando = true;
        try {
            for (int i = 0; i < modelo.getRowCount(); i++) {
                if ((Integer) modelo.getValueAt(i, 0) == id) {
                    int vista = tabla.convertRowIndexToView(i);
                    tabla.setRowSelectionInterval(vista, vista);
                    tabla.scrollRectToVisible(tabla.getCellRect(vista, 0, true));
                    return;
                }
            }
            quitarSeleccion();
        } finally {
            cargando = false;
        }
    }

    // ------------------------------------------------------------------
    // Formulario
    // ------------------------------------------------------------------

    private void mostrar(Producto p) {
        cargado = p;
        restaurarCampos(p);
        chkActivo.setSelected(p.isActivo());
        aplicarEstado();
    }

    private void restaurarCampos(Producto p) {
        txtId.setText(String.valueOf(p.getId()));
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion() == null ? "" : p.getDescripcion());
        txtPrecio.setText(p.getPrecio().toPlainString());
        txtStock.setText(String.valueOf(p.getStock()));
        seleccionar(cmbRubro, p.getRubro().getId());
        seleccionar(cmbProveedor, p.getProveedor().getId());
    }

    private void seleccionar(JComboBox<ItemCatalogo> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(-1);
    }

    /** Vacía el formulario, desmarca la tabla y vuelve a "Activo". */
    private void limpiar() {
        cargado = null;
        txtId.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        cmbRubro.setSelectedIndex(-1);
        cmbProveedor.setSelectedIndex(-1);
        chkActivo.setSelected(true);
        cargando = true;
        try {
            quitarSeleccion();
        } finally {
            cargando = false;
        }
        aplicarEstado();
    }

    /**
     * Habilita o bloquea controles según el perfil y el estado del producto.
     * Esto es solo ayuda visual: GestionService vuelve a verificar permisos y bloqueo.
     */
    private void aplicarEstado() {
        boolean haySeleccion = cargado != null;
        boolean activo = chkActivo.isSelected();
        boolean camposEditables = puedeEditar && activo;

        txtNombre.setEditable(camposEditables);
        txtDescripcion.setEditable(camposEditables);
        txtPrecio.setEditable(camposEditables);
        txtStock.setEditable(camposEditables);
        cmbRubro.setEnabled(camposEditables);
        cmbProveedor.setEnabled(camposEditables);

        chkActivo.setEnabled(puedeEditar && haySeleccion);
        btnGuardar.setEnabled(puedeEditar && !haySeleccion);
        btnModificar.setEnabled(puedeEditar && haySeleccion && (activo || cargado.isActivo()));
        btnEliminar.setEnabled(puedeEditar && haySeleccion && cargado.isActivo());
    }

    private Producto leerFormulario() {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingresá un precio numérico válido");
        }
        int stock;
        try {
            stock = Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingresá un stock entero válido");
        }
        ItemCatalogo rubro = (ItemCatalogo) cmbRubro.getSelectedItem();
        if (rubro == null) {
            throw new IllegalArgumentException("Seleccioná un rubro");
        }
        ItemCatalogo proveedor = (ItemCatalogo) cmbProveedor.getSelectedItem();
        if (proveedor == null) {
            throw new IllegalArgumentException("Seleccioná un proveedor");
        }

        Producto p = new Producto(nombre, descripcion, precio, stock);
        p.setRubro(new Rubro(rubro.getId(), rubro.getNombre()));
        p.setProveedor(new Proveedor(proveedor.getId(), proveedor.getNombre(), null, null));
        p.setActivo(chkActivo.isSelected());
        return p;
    }

    // ------------------------------------------------------------------
    // Operaciones (siempre a través del servicio)
    // ------------------------------------------------------------------

    private void guardar() {
        GuiUtil.ejecutar(this, () -> {
            servicio.agregarProducto(leerFormulario());
            JOptionPane.showMessageDialog(this, "Producto registrado");
            limpiar();
            cargar();
        });
    }

    private void modificar() {
        GuiUtil.ejecutar(this, () -> {
            Producto p = leerFormulario();
            p.setId(cargado.getId());
            servicio.modificarProducto(p);
            JOptionPane.showMessageDialog(this, "Producto modificado");
            limpiar();
            cargar();
        });
    }

    private void eliminar() {
        GuiUtil.ejecutar(this, () -> {
            int r = JOptionPane.showConfirmDialog(this,
                    "¿Querés dar de baja el producto \"" + cargado.getNombre() + "\"?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
            servicio.eliminarProducto(cargado.getId());
            JOptionPane.showMessageDialog(this, "Producto dado de baja");
            limpiar();
            cargar();
        });
    }

    private void buscar() {
        GuiUtil.ejecutar(this, () -> {
            int id;
            try {
                id = Integer.parseInt(txtBuscarId.getText().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Ingresá un ID numérico");
            }
            Producto p = servicio.buscarProducto(id);
            if (p == null) {
                JOptionPane.showMessageDialog(this, "No existe un producto con ese ID",
                        "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (!p.isActivo() && !chkVerInactivos.isSelected()) {
                chkVerInactivos.setSelected(true);
                cargar();
            }
            seleccionarFila(id);
            mostrar(p);
        });
    }

    /** Desmarca la tabla y también el ancla/cabeza de la selección (evita avisos internos del sorter). */
    private void quitarSeleccion() {
        tabla.clearSelection();
        tabla.getSelectionModel().setAnchorSelectionIndex(-1);
        tabla.getSelectionModel().setLeadSelectionIndex(-1);
    }
}
