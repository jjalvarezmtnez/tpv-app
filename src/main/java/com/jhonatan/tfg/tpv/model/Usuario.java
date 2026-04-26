package com.jhonatan.tfg.tpv.model;

import com.jhonatan.tfg.tpv.model.enums.Rol;

/**
 * Entidad que representa a un usuario del sistema.
 *
 * Un usuario es quien interactúa con la aplicación y puede tener distintos roles
 * (por ejemplo: ADMIN o CAMARERO), lo que determina los permisos dentro del sistema.
 *
 * Aspectos clave de diseño:
 * - username → identificador único para login
 * - password → puede estar en texto plano temporalmente o como hash persistido
 * - rol → controla acceso a funcionalidades
 * - activo → permite deshabilitar usuarios sin eliminarlos
 */
public class Usuario {

    private int idUsuario;
    private String username;
    private String nombre;
    private String password;
    private Rol rol;
    private boolean activo;

    /**
     * Constructor vacío.
     *
     * Útil para frameworks, mapeo desde base de datos
     * o creación progresiva del objeto.
     */
    public Usuario() {}

    /**
     * Constructor completo.
     *
     * @param idUsuario identificador único del usuario
     * @param username nombre de usuario (único en el sistema)
     * @param nombre nombre real del usuario (no necesariamente único)
     * @param password contraseña (texto plano o hash según contexto)
     * @param rol rol del usuario dentro del sistema
     */
    public Usuario(int idUsuario, String username, String nombre, String password, Rol rol) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.nombre = nombre;
        this.password = password;
        this.rol = rol;
    }

    /**
     * Obtiene el identificador del usuario.
     *
     * @return id del usuario
     */
    public int getIdUsuario() {
        return idUsuario;
    }

    /**
     * Obtiene el nombre de usuario (login).
     *
     * @return username único
     */
    public String getUsername() {
        return username;
    }

    /**
     * Obtiene el nombre real del usuario.
     *
     * @return nombre descriptivo
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene la contraseña asociada al usuario.
     *
     * Importante:
     * - Puede contener texto plano (antes de persistir)
     * - Puede contener hash (después de leer de BD)
     *
     * ERROR COMÚN:
     * Usar este valor directamente para autenticación sin aplicar hash → inseguro.
     *
     * @return contraseña o hash
     */
    public String getPassword() {
        return password;
    }

    /**
     * Obtiene el rol del usuario.
     *
     * @return rol (ADMIN, CAMARERO, etc.)
     */
    public Rol getRol() {
        return rol;
    }

    /**
     * Indica si el usuario está activo en el sistema.
     *
     * @return true si está activo, false si está deshabilitado
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Establece el identificador del usuario.
     *
     * @param idUsuario id único
     */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Establece el username.
     *
     * Debe ser único en el sistema.
     *
     * @param username nombre de usuario
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Establece el nombre real del usuario.
     *
     * @param nombre nombre descriptivo
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Establece la contraseña del usuario.
     *
     * Nota:
     * No se realiza hash aquí → responsabilidad del DAO o servicio.
     *
     * @param password contraseña en texto plano o hash
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Establece el rol del usuario.
     *
     * @param rol rol del sistema
     */
    public void setRol(Rol rol) {
        this.rol = rol;
    }

    /**
     * Establece si el usuario está activo.
     *
     * Permite aplicar "borrado lógico" en lugar de eliminar registros.
     *
     * @param activo true para activar, false para desactivar
     */
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /**
     * Representación textual del usuario.
     *
     * Se utiliza en componentes de interfaz como listas o tablas simples.
     *
     * @return texto descriptivo del usuario
     */
    @Override
    public String toString() {
        return username + " | " + nombre + " | " + rol;
    }
}