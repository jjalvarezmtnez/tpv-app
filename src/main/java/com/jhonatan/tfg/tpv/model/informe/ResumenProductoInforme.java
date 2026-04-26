package com.jhonatan.tfg.tpv.model.informe;

/**
 * Modelo de datos para representar el resumen de productos vendidos
 * en la vista de informes.
 *
 * Modelo mental:
 * Esta clase NO es una entidad de base de datos.
 * Es un DTO (Data Transfer Object) o modelo de presentación.
 *
 * Problema que resuelve:
 * Antes se usaban Strings tipo:
 * "Coca-Cola | Unidades vendidas: 25"
 *
 * Eso tiene varios problemas:
 * - No es estructurado → no puedes separar columnas fácilmente
 * - No es reutilizable → solo sirve para mostrar texto
 * - Rompe TableView → necesita propiedades separadas por columna
 *
 * Esta clase soluciona eso separando:
 * - producto → columna 1
 * - unidadesVendidas → columna 2
 *
 * Por qué es importante para TableView:
 * TableView trabaja con objetos y propiedades,
 * no con texto plano.
 *
 * Esto permite:
 * - Ordenar columnas
 * - Formatear datos
 * - Aplicar estilos por columna
 *
 * Por qué los atributos son final:
 * - Son inmutables → representan una consulta ya calculada
 * - Evita inconsistencias en UI
 *
 * Cuándo usar esta clase:
 * - Cuando muestras resultados agregados (SUM, COUNT…)
 * - Cuando necesitas una tabla estructurada
 *
 * Cuándo NO usarla:
 * - Para insertar/actualizar datos → no es una entidad
 * - Para lógica de negocio → solo es representación
 *
 * Error común:
 * Mezclar este tipo de clases con entidades reales → rompe la arquitectura MVC
 */
public class ResumenProductoInforme {

    private final String producto;
    private final int unidadesVendidas;

    /**
     * Constructor completo.
     *
     * @param producto nombre del producto
     * @param unidadesVendidas total de unidades vendidas
     */
    public ResumenProductoInforme(String producto, int unidadesVendidas) {
        this.producto = producto;
        this.unidadesVendidas = unidadesVendidas;
    }

    /**
     * Obtiene el nombre del producto.
     *
     * @return nombre del producto
     */
    public String getProducto() {
        return producto;
    }

    /**
     * Obtiene el número de unidades vendidas.
     *
     * @return cantidad total vendida
     */
    public int getUnidadesVendidas() {
        return unidadesVendidas;
    }
}