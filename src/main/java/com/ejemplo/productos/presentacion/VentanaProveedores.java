package com.ejemplo.productos.presentacion;

import com.ejemplo.productos.servicio.GestionService;

/** Gestión de proveedores: especialización de VentanaCatalogoCRUD. */
public class VentanaProveedores extends VentanaCatalogoCRUD {

    public VentanaProveedores(GestionService servicio, Runnable volverAlMenu) {
        super(servicio, GestionService.PROVEEDOR, volverAlMenu);
    }
}
