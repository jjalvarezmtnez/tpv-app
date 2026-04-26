package com.jhonatan.tfg.tpv.dao;

import com.jhonatan.tfg.tpv.database.DatabaseConnection;
import com.jhonatan.tfg.tpv.model.Usuario;
import com.jhonatan.tfg.tpv.model.enums.Rol;
import com.jhonatan.tfg.tpv.util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de gestionar el acceso a datos de la entidad Usuario.
 *
 * Responsabilidades:
 * - Registro de usuarios
 * - Autenticación
 * - Consulta de usuarios activos
 * - Actualización de datos
 * - Desactivación lógica
 * - Comprobaciones de duplicados y administradores activos
 */
public class UsuarioDAO {

    /**
     * Registra un nuevo usuario en la base de datos.
     *
     * La contraseña se almacena como hash, nunca en texto plano.
     *
     * @param usuario objeto Usuario con los datos a registrar
     * @return true si el registro se realiza correctamente, false en caso contrario
     */
    public boolean registrarUsuario(Usuario usuario) {
        final String sql = """
                INSERT INTO usuarios (username, nombre, password_hash, rol, activo)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, PasswordHasher.hashPassword(usuario.getPassword()));
            ps.setString(4, usuario.getRol().name());
            ps.setInt(5, usuario.isActivo() ? 1 : 0);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un usuario por su identificador.
     *
     * @param idUsuario identificador del usuario
     * @return Usuario si existe, null en caso contrario
     */
    public Usuario buscarPorId(int idUsuario) {
        final String sql = """
                SELECT id_usuario, username, nombre, password_hash, rol, activo
                FROM usuarios
                WHERE id_usuario = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Busca un usuario por su username.
     *
     * @param username identificador único de acceso
     * @return Usuario si existe, null en caso contrario
     */
    public Usuario buscarPorUsername(String username) {
        final String sql = """
                SELECT id_usuario, username, nombre, password_hash, rol, activo
                FROM usuarios
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por username: " + e.getMessage());
        }

        return null;
    }

    /**
     * Obtiene todos los usuarios activos.
     *
     * @return lista de usuarios activos ordenados por username
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        final String sql = """
                SELECT id_usuario, username, nombre, password_hash, rol, activo
                FROM usuarios
                WHERE activo = 1
                ORDER BY username
                """;

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener todos los usuarios: " + e.getMessage());
        }

        return usuarios;
    }

    /**
     * Autentica un usuario a partir de sus credenciales.
     *
     * Solo permite iniciar sesión a usuarios activos.
     *
     * @param username username introducido en el login
     * @param password contraseña en texto plano introducida por el usuario
     * @return Usuario autenticado si las credenciales son correctas, null en caso contrario
     */
    public Usuario autenticarUsuario(String username, String password) {
        Usuario usuario = buscarPorUsername(username);

        if (usuario == null || !usuario.isActivo()) {
            return null;
        }

        if (PasswordHasher.verifyPassword(password, usuario.getPassword())) {
            return usuario;
        }

        return null;
    }

    /**
     * Comprueba si ya existe un usuario con el username indicado.
     *
     * @param username username a comprobar
     * @return true si existe, false en caso contrario
     */
    public boolean existeUsername(String username) {
        return buscarPorUsername(username) != null;
    }

    /**
     * Comprueba si existe otro usuario con el mismo username,
     * excluyendo un usuario concreto.
     *
     * @param username username a comprobar
     * @param idUsuarioExcluir identificador del usuario a excluir
     * @return true si existe otro usuario con ese username, false en caso contrario
     */
    public boolean existeUsernameExcluyendoId(String username, int idUsuarioExcluir) {
        final String sql = """
                SELECT 1
                FROM usuarios
                WHERE username = ? AND id_usuario <> ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setInt(2, idUsuarioExcluir);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error al comprobar username excluyendo ID: " + e.getMessage());
        }

        return false;
    }

    /**
     * Cuenta cuántos administradores activos existen.
     *
     * Se usa para impedir que el sistema quede sin ningún administrador activo.
     *
     * @return número de usuarios activos con rol ADMIN
     */
    public int contarAdminsActivos() {
        final String sql = """
                SELECT COUNT(*) AS total
                FROM usuarios
                WHERE rol = 'ADMIN' AND activo = 1
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error al contar administradores activos: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * La contraseña se vuelve a hashear antes de guardarse.
     *
     * @param usuario objeto Usuario con la información actualizada
     * @return true si la actualización se realiza correctamente, false en caso contrario
     */
    public boolean actualizarUsuario(Usuario usuario) {
        final String sql = """
                UPDATE usuarios
                SET username = ?, nombre = ?, password_hash = ?, rol = ?
                WHERE id_usuario = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, PasswordHasher.hashPassword(usuario.getPassword()));
            ps.setString(4, usuario.getRol().name());
            ps.setInt(5, usuario.getIdUsuario());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Desactiva un usuario sin eliminarlo físicamente.
     *
     * Esto permite conservar la trazabilidad histórica en comandas ya registradas.
     *
     * @param idUsuario identificador del usuario
     * @return true si la desactivación se realiza correctamente, false en caso contrario
     */
    public boolean desactivarUsuario(int idUsuario) {
        final String sql = """
                UPDATE usuarios
                SET activo = 0
                WHERE id_usuario = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al desactivar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Convierte una fila del ResultSet en un objeto Usuario.
     *
     * @param rs resultado SQL posicionado en una fila válida
     * @return objeto Usuario mapeado
     * @throws SQLException si ocurre un error al leer los datos
     */
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setUsername(rs.getString("username"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setPassword(rs.getString("password_hash"));
        usuario.setRol(Rol.valueOf(rs.getString("rol")));
        usuario.setActivo(rs.getInt("activo") == 1);
        return usuario;
    }
}