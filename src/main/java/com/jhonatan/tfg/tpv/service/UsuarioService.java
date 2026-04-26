package com.jhonatan.tfg.tpv.service;

import com.jhonatan.tfg.tpv.dao.UsuarioDAO;
import com.jhonatan.tfg.tpv.model.Usuario;
import com.jhonatan.tfg.tpv.model.enums.Rol;

/**
 * Servicio encargado de coordinar la lógica de negocio relacionada con usuarios.
 *
 * Actúa como intermediario entre la capa de presentación y el acceso a datos,
 * aplicando validaciones y reglas de negocio antes de delegar en el DAO.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    /**
     * Constructor del servicio.
     *
     * Inicializa el DAO necesario para realizar operaciones sobre usuarios.
     */
    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Autentica un usuario a partir de sus credenciales.
     *
     * La validación real de contraseña (hash) se delega al DAO,
     * pero aquí se filtran entradas inválidas.
     *
     * @param username identificador de acceso introducido por el usuario
     * @param password contraseña en texto plano introducida por el usuario
     * @return objeto Usuario autenticado si las credenciales son correctas;
     *         null en caso contrario
     */
    public Usuario autenticarUsuario(String username, String password) {
        if (username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            return null;
        }

        return usuarioDAO.autenticarUsuario(username, password);
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * Aplica validaciones de integridad y evita duplicados antes de persistir.
     *
     * @param usuario objeto Usuario con los datos de alta
     * @return true si el registro se realiza correctamente, false en caso contrario
     */
    public boolean registrarUsuario(Usuario usuario) {
        if (usuario == null) {
            return false;
        }

        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            return false;
        }

        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            return false;
        }

        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            return false;
        }

        if (usuario.getRol() == null) {
            return false;
        }

        // Regla de negocio: username único
        if (usuarioDAO.existeUsername(usuario.getUsername())) {
            return false;
        }

        // Regla de negocio: todo usuario nuevo nace activo
        usuario.setActivo(true);

        return usuarioDAO.registrarUsuario(usuario);
    }

    /**
     * Actualiza un usuario existente en el sistema.
     *
     * Incluye validaciones completas para evitar inconsistencias
     * y duplicados de username.
     *
     * @param usuario objeto Usuario con los datos actualizados
     * @return true si la actualización se realiza correctamente, false en caso contrario
     */
    public boolean actualizarUsuario(Usuario usuario) {
        if (usuario == null) {
            return false;
        }

        if (usuario.getIdUsuario() <= 0) {
            return false;
        }

        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            return false;
        }

        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            return false;
        }

        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            return false;
        }

        if (usuario.getRol() == null) {
            return false;
        }

        // Evitar duplicados excluyendo el propio usuario
        if (usuarioDAO.existeUsernameExcluyendoId(
                usuario.getUsername(),
                usuario.getIdUsuario()
        )) {
            return false;
        }

        return usuarioDAO.actualizarUsuario(usuario);
    }

    /**
     * Desactiva un usuario sin eliminarlo físicamente.
     *
     * Se aplica una restricción crítica: no se puede desactivar
     * el último administrador activo del sistema.
     *
     * @param idUsuario identificador del usuario a desactivar
     * @return true si la desactivación se realiza correctamente, false en caso contrario
     */
    public boolean desactivarUsuario(int idUsuario) {
        if (idUsuario <= 0) {
            return false;
        }

        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);

        if (usuario == null) {
            return false;
        }

        // Regla crítica: evitar dejar el sistema sin administradores
        if (usuario.getRol() == Rol.ADMIN && usuarioDAO.contarAdminsActivos() <= 1) {
            return false;
        }

        return usuarioDAO.desactivarUsuario(idUsuario);
    }
}