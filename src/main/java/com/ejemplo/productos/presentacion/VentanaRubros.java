package com.ejemplo.productos.presentacion;

import com.ejemplo.productos.servicio.GestionService;

/** Gestión de rubros: especialización de VentanaCatalogoCRUD. */
public class VentanaRubros extends VentanaCatalogoCRUD {

    public VentanaRubros(GestionService servicio, Runnable volverAlMenu) {
        super(servicio, GestionService.RUBRO, volverAlMenu);
    }
}
