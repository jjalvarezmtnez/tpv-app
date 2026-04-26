package com.jhonatan.tfg.tpv.model;

import com.jhonatan.tfg.tpv.model.enums.EstadoMesa;

/**
 * Entidad que representa una mesa del establecimiento.
 *
 * Una mesa es un recurso físico del negocio que puede encontrarse
 * en distintos estados (por ejemplo: LIBRE u OCUPADA).
 *
 * Este estado es clave en la lógica de negocio, ya que determina
 * si se puede abrir una nueva comanda o no.
 */
public class Mesa {

    private int idMesa;
    private int numero;
    private EstadoMesa estado;

    /**
     * Constructor vacío.
     *
     * Permite instanciar objetos sin inicializar,
     * útil en procesos de carga desde base de datos.
     */
    public Mesa() {
    }

    /**
     * Constructor completo.
     *
     * @param idMesa identificador único de la mesa
     * @param numero número visible de la mesa
     * @param estado estado actual de la mesa (LIBRE, OCUPADA, etc.)
     */
    public Mesa(int idMesa, int numero, EstadoMesa estado) {
        this.idMesa = idMesa;
        this.numero = numero;
        this.estado = estado;
    }

    /**
     * Obtiene el identificador de la mesa.
     *
     * @return id de la mesa
     */
    public int getIdMesa() {
        return idMesa;
    }

    /**
     * Obtiene el número de la mesa.
     *
     * @return número visible de la mesa
     */
    public int getNumero() {
        return numero;
    }

    /**
     * Obtiene el estado actual de la mesa.
     *
     * @return estado de la mesa
     */
    public EstadoMesa getEstado() {
        return estado;
    }

    /**
     * Establece el identificador de la mesa.
     *
     * @param idMesa identificador único
     */
    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    /**
     * Establece el número de la mesa.
     *
     * @param numero número visible de la mesa
     */
    public void setNumero(int numero) {
        this.numero = numero;
    }

    /**
     * Establece el estado de la mesa.
     *
     * @param estado nuevo estado de la mesa
     */
    public void setEstado(EstadoMesa estado) {
        this.estado = estado;
    }

    /**
     * Devuelve una representación legible de la mesa.
     *
     * Se utiliza principalmente en componentes simples como ListView.
     * En TableView normalmente se accede directamente a los atributos.
     *
     * @return texto descriptivo de la mesa
     */
    @Override
    public String toString() {
        return "Mesa " + numero + " | Estado: " + estado;
    }
}