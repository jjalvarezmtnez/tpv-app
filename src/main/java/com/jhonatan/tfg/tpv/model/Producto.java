package com.jhonatan.tfg.tpv.model;

/**
 * Entidad que representa un producto del sistema.
 *
 * Un producto es un elemento vendible dentro del negocio.
 * Contiene información clave para la lógica de ventas:
 * - precio → usado para calcular totales
 * - stock → usado para validar disponibilidad
 * - categoría → permite organizar y filtrar productos
 *
 * Nota de diseño:
 * El stock aquí es crítico para evitar ventas inválidas,
 * por lo que siempre debe validarse antes de operar con él.
 */
public class Producto {

    private int idProducto;
    private String nombre;
    private double precio;
    private int stock;
    private int idCategoria;

    /**
     * Constructor vacío.
     *
     * Útil para frameworks, mapeos de base de datos
     * y creación progresiva del objeto.
     */
    public Producto(){}

    /**
     * Constructor completo.
     *
     * @param idProducto identificador único del producto
     * @param nombre nombre del producto
     * @param precio precio unitario del producto
     * @param stock cantidad disponible en inventario
     * @param idCategoria identificador de la categoría asociada
     */
    public Producto(int idProducto, String nombre, double precio, int stock, int idCategoria) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.idCategoria = idCategoria;
    }

    /**
     * Obtiene el identificador del producto.
     *
     * @return id del producto
     */
    public int getIdProducto() {
        return idProducto;
    }

    /**
     * Obtiene el nombre del producto.
     *
     * @return nombre del producto
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el precio del producto.
     *
     * @return precio unitario
     */
    public double getPrecio() {
        return precio;
    }

    /**
     * Obtiene el stock disponible del producto.
     *
     * @return cantidad disponible
     */
    public int getStock() {
        return stock;
    }

    /**
     * Obtiene el identificador de la categoría.
     *
     * @return id de la categoría
     */
    public int getIdCategoria() {
        return idCategoria;
    }

    /**
     * Establece el identificador del producto.
     *
     * @param idProducto id único
     */
    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    /**
     * Establece el nombre del producto.
     *
     * @param nombre nombre descriptivo
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Establece el precio del producto.
     *
     * Importante:
     * No se valida aquí si es negativo → esa validación
     * debería hacerse en la capa de servicio.
     *
     * @param precio precio unitario
     */
    public void setPrecio(double precio) {
        this.precio = precio;
    }

    /**
     * Establece el stock del producto.
     *
     * Importante:
     * No se controla aquí si es negativo → esa lógica
     * pertenece a la capa de negocio.
     *
     * @param stock cantidad disponible
     */
    public void setStock(int stock) {
        this.stock = stock;
    }

    /**
     * Establece la categoría del producto.
     *
     * @param idCategoria identificador de la categoría
     */
    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    /**
     * Representación textual del producto.
     *
     * Se usa en componentes como ComboBox o ListView.
     * Solo se muestra el nombre para simplificar la UI.
     *
     * @return nombre del producto
     */
    @Override
    public String toString() {
        return nombre;
    }
}