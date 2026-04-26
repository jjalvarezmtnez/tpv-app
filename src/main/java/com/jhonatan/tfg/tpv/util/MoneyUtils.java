package com.jhonatan.tfg.tpv.util;

import java.text.DecimalFormat;

/**
 * Clase utilitaria para el manejo de valores monetarios.
 *
 * Centraliza operaciones como redondeo, suma y formateo,
 * garantizando coherencia en toda la aplicación.
 *
 * NOTA:
 * Se utiliza double por simplicidad, pero se aplican estrategias
 * de redondeo para minimizar errores de precisión.
 */
public final class MoneyUtils {

    /**
     * Formateador reutilizable para evitar crear instancias repetidas.
     */
    private static final DecimalFormat FORMATO_MONEDA = new DecimalFormat("0.00");

    /**
     * Constructor privado para evitar instanciación.
     */
    private MoneyUtils() {
    }

    /**
     * Redondea un valor a 2 decimales.
     *
     * Se utiliza para mitigar errores de precisión de double
     * en operaciones monetarias.
     *
     * @param valor valor a redondear
     * @return valor redondeado a 2 decimales
     */
    public static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    /**
     * Formatea un valor monetario a String con 2 decimales.
     *
     * Ejemplo: 2.5 → "2.50"
     *
     * @param valor valor a formatear
     * @return String con 2 decimales
     */
    public static String formatear(double valor) {
        return FORMATO_MONEDA.format(valor);
    }

    /**
     * Formatea un valor monetario con símbolo de euro.
     *
     * Ejemplo: 2.5 → "2.50 €"
     *
     * @param valor valor a formatear
     * @return String con formato monetario en euros
     */
    public static String formatearEuros(double valor) {
        return formatear(valor) + " €";
    }

    /**
     * Suma dos valores monetarios aplicando redondeo.
     *
     * Evita acumulación de errores de precisión al encadenar operaciones.
     *
     * @param a primer valor
     * @param b segundo valor
     * @return suma redondeada a 2 decimales
     */
    public static double sumar(double a, double b) {
        return redondear(a + b);
    }
}