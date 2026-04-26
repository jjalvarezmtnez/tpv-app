package com.jhonatan.tfg.tpv.model;

/**
 * Entidad que representa una categoría de productos.
 *
 * Se utiliza para agrupar productos dentro del sistema,
 * facilitando su organización y selección desde la interfaz.
 */
public class Categoria {

    private int idCategoria;
    private String nombre;

    /**
     * Constructor vacío.
     *
     * Necesario para crear objetos Categoria sin inicializar
     * sus atributos de forma inmediata.
     */
    public Categoria() {
    }

    /**
     * Constructor completo.
     *
     * @param idCategoria identificador único de la categoría
     * @param nombre nombre de la categoría
     */
    public Categoria(int idCategoria, String nombre) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
    }

    /**
     * Obtiene el identificador de la categoría.
     *
     * @return identificador único de la categoría
     */
    public int getIdCategoria() {
        return idCategoria;
    }

    /**
     * Obtiene el nombre de la categoría.
     *
     * @return nombre de la categoría
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el identificador de la categoría.
     *
     * @param idCategoria identificador único de la categoría
     */
    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    /**
     * Establece el nombre de la categoría.
     *
     * @param nombre nombre de la categoría
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve una representación legible de la categoría.
     *
     * Se utiliza principalmente en componentes de interfaz,
     * como ComboBox, para mostrar únicamente el nombre.
     *
     * @return nombre de la categoría
     */
    @Override
    public String toString() {
        return nombre;
    }
}