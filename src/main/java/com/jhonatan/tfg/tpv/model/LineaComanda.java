package com.jhonatan.tfg.tpv.model;

/**
 * Entidad que representa una línea de una comanda.
 *
 * Una línea de comanda representa un producto concreto añadido
 * a una comanda, incluyendo la cantidad solicitada y el precio
 * unitario en el momento de la venta.
 *
 * Es importante almacenar el precio unitario aquí para evitar
 * inconsistencias si el precio del producto cambia posteriormente.
 */
public class LineaComanda {

    private int idLinea;
    private int idComanda;
    private int idProducto;
    private int cantidad;
    private double precioUnitario;

    /**
     * Constructor vacío.
     *
     * Permite crear instancias sin inicializar atributos,
     * útil para procesos de carga desde base de datos.
     */
    public LineaComanda() {
    }

    /**
     * Constructor completo.
     *
     * @param idLinea identificador único de la línea
     * @param idComanda identificador de la comanda asociada
     * @param idProducto identificador del producto
     * @param cantidad cantidad de unidades del producto
     * @param precioUnitario precio del producto en el momento de la venta
     */
    public LineaComanda(int idLinea, int idComanda, int idProducto, int cantidad, double precioUnitario) {
        this.idLinea = idLinea;
        this.idComanda = idComanda;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    /**
     * Obtiene el identificador de la línea de comanda.
     *
     * @return identificador único de la línea
     */
    public int getIdLinea() {
        return idLinea;
    }

    /**
     * Obtiene el identificador de la comanda asociada.
     *
     * @return identificador de la comanda
     */
    public int getIdComanda() {
        return idComanda;
    }

    /**
     * Obtiene el identificador del producto.
     *
     * @return identificador del producto
     */
    public int getIdProducto() {
        return idProducto;
    }

    /**
     * Obtiene la cantidad del producto.
     *
     * @return número de unidades
     */
    public int getCantidad() {
        return cantidad;
    }

    /**
     * Obtiene el precio unitario del producto.
     *
     * @return precio unitario
     */
    public double getPrecioUnitario() {
        return precioUnitario;
    }

    /**
     * Establece el identificador de la línea.
     *
     * @param idLinea identificador único de la línea
     */
    public void setIdLinea(int idLinea) {
        this.idLinea = idLinea;
    }

    /**
     * Establece el identificador de la comanda.
     *
     * @param idComanda identificador de la comanda
     */
    public void setIdComanda(int idComanda) {
        this.idComanda = idComanda;
    }

    /**
     * Establece el identificador del producto.
     *
     * @param idProducto identificador del producto
     */
    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    /**
     * Establece la cantidad del producto.
     *
     * @param cantidad número de unidades
     */
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    /**
     * Establece el precio unitario del producto.
     *
     * @param precioUnitario precio del producto en el momento de la venta
     */
    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    /**
     * Devuelve una representación legible de la línea de comanda.
     *
     * Se utiliza principalmente en componentes de interfaz cuando
     * no se emplean estructuras más complejas como TableView.
     *
     * @return texto descriptivo de la línea
     */
    @Override
    public String toString() {
        return "Producto ID: " + idProducto
                + " | Cantidad: " + cantidad
                + " | Precio unitario: " + precioUnitario;
    }
}