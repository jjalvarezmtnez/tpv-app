package com.jhonatan.tfg.tpv.util;

import com.jhonatan.tfg.tpv.model.enums.Rol;

/**
 * Clase utilitaria encargada de centralizar las comprobaciones
 * de permisos de acceso basadas en la sesión actual.
 *
 * Se utiliza para desacoplar la lógica de autorización de los controladores,
 * evitando duplicación de condiciones y facilitando el mantenimiento.
 */
public final class AccessManager {

    /**
     * Constructor privado para evitar la instanciación de la clase.
     *
     * Esta clase está diseñada como utilidad estática.
     */
    private AccessManager() {
    }

    /**
     * Comprueba si el usuario actual tiene rol de administrador.
     *
     * Internamente valida:
     * - Que exista una sesión activa
     * - Que el rol del usuario sea ADMIN
     *
     * @return true si el usuario autenticado es administrador, false en caso contrario
     */
    public static boolean esAdmin() {
        return SessionManager.haySesionActiva()
                && SessionManager.getUsuarioActual().getRol() == Rol.ADMIN;
    }
}