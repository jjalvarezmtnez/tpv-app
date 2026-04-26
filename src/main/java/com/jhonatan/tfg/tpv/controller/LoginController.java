package com.jhonatan.tfg.tpv.controller;

import com.jhonatan.tfg.tpv.model.Usuario;
import com.jhonatan.tfg.tpv.service.UsuarioService;
import com.jhonatan.tfg.tpv.util.MessageUtils;
import com.jhonatan.tfg.tpv.util.NavigationManager;
import com.jhonatan.tfg.tpv.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controlador de la vista de login.
 *
 * Gestiona la autenticación inicial del usuario y crea la sesión
 * de aplicación cuando las credenciales son correctas.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label mensajeLabel;

    private final UsuarioService usuarioService = new UsuarioService();

    /**
     * Gestiona el inicio de sesión.
     *
     * Valida los campos del formulario, autentica al usuario
     * y navega al menú principal si las credenciales son válidas.
     */
    @FXML
    private void handleLogin() {
        MessageUtils.clear(mensajeLabel);

        String username = obtenerUsername();
        String password = obtenerPassword();

        if (username == null || password == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Introduce usuario y contraseña.", 3);
            return;
        }

        Usuario usuario = usuarioService.autenticarUsuario(username, password);

        if (usuario == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Credenciales incorrectas.", 3);
            return;
        }

        iniciarSesion(usuario);
        navegarAMenuPrincipal();
    }

    /**
     * Obtiene y valida el username introducido.
     *
     * @return username limpio o null si está vacío
     */
    private String obtenerUsername() {
        String username = usernameField.getText();

        if (username == null || username.isBlank()) {
            return null;
        }

        return username.trim();
    }

    /**
     * Obtiene y valida la contraseña introducida.
     *
     * @return contraseña o null si está vacía
     */
    private String obtenerPassword() {
        String password = passwordField.getText();

        if (password == null || password.isBlank()) {
            return null;
        }

        return password;
    }

    /**
     * Guarda el usuario autenticado en la sesión actual.
     *
     * @param usuario usuario autenticado
     */
    private void iniciarSesion(Usuario usuario) {
        SessionManager.setUsuarioActual(usuario);
        MessageUtils.showSuccessAuto(mensajeLabel, "Bienvenido " + usuario.getNombre() + ".", 2);
    }

    /**
     * Navega al menú principal de la aplicación.
     */
    private void navegarAMenuPrincipal() {
        try {
            NavigationManager.cambiarVista(
                    (Node) usernameField,
                    "/fxml/MainMenuView.fxml",
                    "TPV - Menú Principal"
            );
        } catch (Exception e) {
            MessageUtils.showErrorAuto(mensajeLabel, "Error al abrir el menú principal.", 3.5);
            e.printStackTrace();
        }
    }
}