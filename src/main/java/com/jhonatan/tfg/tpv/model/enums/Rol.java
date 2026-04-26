package com.jhonatan.tfg.tpv.model.enums;

/**
 * Enum que representa los roles disponibles dentro del sistema.
 *
 * Modelo mental:
 * Un rol define QUÉ puede hacer un usuario, no QUIÉN es.
 * Es decir, separa identidad (usuario) de permisos (rol).
 *
 * Roles:
 * - ADMIN → acceso completo al sistema (gestión de usuarios, mesas, informes…)
 * - CAMARERO → acceso operativo (gestión de comandas, productos en servicio)
 *
 * Por qué usar enum:
 * - Evita errores de texto ("admin", "Admin", etc.)
 * - Centraliza los permisos en un conjunto cerrado
 * - Facilita validaciones tipo: if (rol == Rol.ADMIN)
 *
 * Consecuencias en la aplicación:
 * - Determina qué vistas puede abrir el usuario
 * - Controla qué botones/acciones están habilitados
 * - Se usa en AccessManager para aplicar restricciones
 *
 * Error común:
 * Usar Strings en vez de enum → rompe seguridad y consistencia
 *
 * Otro error común:
 * Meter lógica de permisos directamente en la UI → debe centralizarse
 * (ej: AccessManager o capa de servicio)
 *
 * Cuándo NO usar este enum:
 * - Si necesitas permisos granulares (ej: permisos por acción)
 *   → usar un sistema de permisos más avanzado (RBAC)
 */
public enum Rol {
    ADMIN,
    CAMARERO
}