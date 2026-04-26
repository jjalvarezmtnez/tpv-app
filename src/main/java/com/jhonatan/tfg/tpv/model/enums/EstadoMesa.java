package com.jhonatan.tfg.tpv.model.enums;

/**
 * Enum que representa los posibles estados de una mesa.
 *
 * Modelo mental:
 * Una mesa no es solo un número, es un recurso del sistema cuyo estado
 * condiciona qué acciones están permitidas.
 *
 * Estados:
 * - LIBRE → no hay comanda activa, se puede abrir una nueva
 * - OCUPADA → existe una comanda ABIERTA asociada
 *
 * Relación con la lógica de negocio:
 * - Una mesa pasa a OCUPADA cuando se abre una comanda
 * - Vuelve a LIBRE cuando la comanda se cierra o se anula
 *
 * Por qué usar enum y no boolean:
 * - Más expresivo (no es solo true/false)
 * - Escalable (ej: RESERVADA, FUERA_DE_SERVICIO en el futuro)
 * - Evita errores de interpretación
 *
 * Error común:
 * Cambiar el estado manualmente desde la UI sin pasar por la lógica de negocio
 * → rompe la coherencia con las comandas.
 *
 * Cuándo NO usar este enum:
 * - Si necesitas estados temporales complejos → ampliar el enum, no usar Strings
 */
public enum EstadoMesa {
    LIBRE,
    OCUPADA
}