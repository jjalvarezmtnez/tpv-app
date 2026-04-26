package com.jhonatan.tfg.tpv.model.informe;

/**
 * Modelo de datos para representar una fila del historial de ventas
 * en la vista de informes.
 *
 * Modelo mental:
 * Esta clase es un DTO (Data Transfer Object) orientado a la UI.
 * NO es una entidad de base de datos, sino una proyección de datos ya procesados.
 *
 * Problema que resuelve:
 * Antes se usaban Strings tipo:
 * "Mesa 4 | 22/04/2026 20:15 | Total: 35,50 €"
 *
 * Problemas de ese enfoque:
 * - No separa columnas → imposible usar TableView correctamente
 * - No permite ordenar por fecha o total
 * - No es reutilizable ni tipado
 *
 * Esta clase divide esos datos en:
 * - mesa → número de mesa
 * - fecha → fecha ya formateada para UI
 * - total → importe ya formateado (con €)
 *
 * Decisión importante:
 * fecha y total son String, no tipos nativos.
 *
 * ¿Por qué?
 * - Ya vienen formateados desde el DAO (dd/MM/yyyy, €)
 * - La UI no tiene que transformar nada
 *
 * Trade-off:
 * - ✔ Simplicidad en la vista
 * - ✖ Pierdes capacidad de ordenar correctamente (por fecha real o valor numérico)
 *
 * Cuándo usar este enfoque:
 * - Interfaces simples
 * - Cuando el formato visual es prioritario
 *
 * Cuándo NO usarlo:
 * - Si necesitas ordenar/filtrar por fecha o importe
 *   → usar LocalDateTime y double en lugar de String
 *
 * Por qué los atributos son final:
 * - Representa datos ya calculados (inmutables)
 * - Evita inconsistencias en la tabla
 *
 * Error común:
 * Mezclar este modelo con entidades reales (Comanda, LineaComanda)
 * → rompe la separación entre capa de datos y presentación
 */
public class HistorialVentaInforme {

    private final int mesa;
    private final String fecha;
    private final String total;

    /**
     * Constructor completo.
     *
     * @param mesa número de la mesa
     * @param fecha fecha de la venta (formateada para UI)
     * @param total importe total (formateado con moneda)
     */
    public HistorialVentaInforme(int mesa, String fecha, String total) {
        this.mesa = mesa;
        this.fecha = fecha;
        this.total = total;
    }

    /**
     * Obtiene el número de mesa asociado a la venta.
     *
     * @return número de mesa
     */
    public int getMesa() {
        return mesa;
    }

    /**
     * Obtiene la fecha de la venta.
     *
     * @return fecha formateada
     */
    public String getFecha() {
        return fecha;
    }

    /**
     * Obtiene el importe total de la venta.
     *
     * @return total formateado (ej: 35,50 €)
     */
    public String getTotal() {
        return total;
    }
}