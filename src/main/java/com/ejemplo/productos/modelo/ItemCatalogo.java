package com.ejemplo.productos.modelo;

/**
 * Elemento de catálogo (rubro o proveedor) pensado para la interfaz gráfica:
 * sirve para llenar JComboBox y las tablas de gestión sin exponer las entidades.
 * Para un rubro, "nombre" es el nombre; para un proveedor, la razón social.
 */
public class ItemCatalogo {
    private final int id;
    private final String nombre;
    private final String domicilio;
    private final String telefono;

    public ItemCatalogo(int id, String nombre) {
        this(id, nombre, null, null);
    }

    public ItemCatalogo(int id, String nombre, String domicilio, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.domicilio = domicilio;
        this.telefono = telefono;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDomicilio() { return domicilio; }
    public String getTelefono() { return telefono; }

    /** Es lo que muestra un JComboBox. */
    @Override
    public String toString() {
        return nombre;
    }
}
