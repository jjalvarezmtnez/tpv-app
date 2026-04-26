package com.jhonatan.tfg.tpv.model.enums;

/**
 * Enum que representa los estados posibles de una comanda.
 *
 * Modelo mental:
 * Una comanda es un flujo con estados bien definidos.
 * No es un booleano (abierta/cerrada), porque:
 * - Permite escalar en el futuro (ej: CANCELADA, EN_PREPARACION…)
 * - Evita ambigüedad en la lógica de negocio
 *
 * Estados:
 * - ABIERTA → la comanda está activa, se pueden añadir/eliminar productos
 * - CERRADA → la comanda está finalizada, no admite modificaciones
 *
 * Consecuencias en el sistema:
 * - Solo las comandas ABIERTAS permiten operaciones de modificación
 * - Solo las comandas CERRADAS se usan para informes/estadísticas
 *
 * Error común:
 * Usar String en lugar de enum → rompe consistencia y permite valores inválidos.
 *
 * Cuándo NO usar este enum:
 * - Si necesitas estados intermedios complejos → ampliar el enum, no sustituirlo
 */
public enum EstadoComanda {
    ABIERTA,
    CERRADA
}