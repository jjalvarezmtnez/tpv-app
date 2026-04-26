package com.jhonatan.tfg.tpv.util;

import com.jhonatan.tfg.tpv.model.Usuario;

/**
 * Clase utilitaria encargada de gestionar la sesión actual del usuario.
 *
 * Mantiene en memoria el usuario autenticado durante la ejecución
 * de la aplicación (sesión en memoria).
 *
 * IMPORTANTE:
 * No persiste la sesión en disco ni soporta múltiples usuarios simultáneos.
 * Está diseñada para aplicaciones de escritorio monousuario.
 */
public final class SessionManager {

    /**
     * Usuario actualmente autenticado en la aplicación.
     */
    private static Usuario usuarioActual;

    /**
     * Constructor privado para evitar instanciación.
     */
    private SessionManager() {
    }

    /**
     * Establece el usuario autenticado en la sesión actual.
     *
     * @param usuario usuario que ha iniciado sesión
     */
    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    /**
     * Obtiene el usuario autenticado actualmente.
     *
     * @return usuario actual o null si no hay sesión iniciada
     */
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Indica si existe una sesión activa.
     *
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    public static boolean haySesionActiva() {
        return usuarioActual != null;
    }

    /**
     * Cierra la sesión actual eliminando el usuario almacenado.
     *
     * Se utiliza al cerrar sesión o al volver a la pantalla de login.
     */
    public static void cerrarSesion() {
        usuarioActual = null;
    }
}