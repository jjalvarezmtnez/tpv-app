package com.jhonatan.tfg.tpv.model;

import com.jhonatan.tfg.tpv.model.enums.EstadoComanda;

/**
 * Entidad que representa una comanda dentro del sistema.
 *
 * Una comanda agrupa los productos consumidos por una mesa
 * y está asociada a un usuario que la gestiona.
 * Contiene información sobre su estado y momento de apertura.
 */
public class Comanda {

    private int idComanda;
    private int idMesa;
    private int idUsuario;
    private String fechaApertura;
    private EstadoComanda estado;

    /**
     * Constructor vacío.
     *
     * Permite crear instancias sin inicializar atributos,
     * útil en procesos de carga desde base de datos.
     */
    public Comanda() {
    }

    /**
     * Constructor completo.
     *
     * @param idComanda identificador único de la comanda
     * @param idMesa identificador de la mesa asociada
     * @param idUsuario identificador del usuario que crea la comanda
     * @param fechaApertura fecha y hora de apertura de la comanda
     * @param estado estado actual de la comanda
     */
    public Comanda(int idComanda, int idMesa, int idUsuario, String fechaApertura, EstadoComanda estado) {
        this.idComanda = idComanda;
        this.idMesa = idMesa;
        this.idUsuario = idUsuario;
        this.fechaApertura = fechaApertura;
        this.estado = estado;
    }

    /**
     * Obtiene el identificador de la comanda.
     *
     * @return identificador único de la comanda
     */
    public int getIdComanda() {
        return idComanda;
    }

    /**
     * Obtiene el identificador de la mesa asociada.
     *
     * @return identificador de la mesa
     */
    public int getIdMesa() {
        return idMesa;
    }

    /**
     * Obtiene el identificador del usuario asociado.
     *
     * @return identificador del usuario
     */
    public int getIdUsuario() {
        return idUsuario;
    }

    /**
     * Obtiene la fecha de apertura de la comanda.
     *
     * @return fecha y hora en formato texto
     */
    public String getFechaApertura() {
        return fechaApertura;
    }

    /**
     * Obtiene el estado actual de la comanda.
     *
     * @return estado de la comanda
     */
    public EstadoComanda getEstado() {
        return estado;
    }

    /**
     * Establece el identificador de la comanda.
     *
     * @param idComanda identificador único de la comanda
     */
    public void setIdComanda(int idComanda) {
        this.idComanda = idComanda;
    }

    /**
     * Establece el identificador de la mesa asociada.
     *
     * @param idMesa identificador de la mesa
     */
    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    /**
     * Establece el identificador del usuario asociado.
     *
     * @param idUsuario identificador del usuario
     */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Establece la fecha de apertura de la comanda.
     *
     * @param fechaApertura fecha y hora en formato texto
     */
    public void setFechaApertura(String fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    /**
     * Establece el estado de la comanda.
     *
     * @param estado estado actual de la comanda
     */
    public void setEstado(EstadoComanda estado) {
        this.estado = estado;
    }

    /**
     * Devuelve una representación textual de la comanda.
     *
     * @return cadena con los datos principales de la comanda
     */
    @Override
    public String toString() {
        return "Comanda " + idComanda +
                " | Mesa: " + idMesa +
                " | Estado: " + estado;
    }
}