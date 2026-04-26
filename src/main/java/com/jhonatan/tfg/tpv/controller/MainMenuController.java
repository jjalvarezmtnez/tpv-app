package com.jhonatan.tfg.tpv.controller;

import com.jhonatan.tfg.tpv.model.Usuario;
import com.jhonatan.tfg.tpv.model.enums.Rol;
import com.jhonatan.tfg.tpv.util.NavigationManager;
import com.jhonatan.tfg.tpv.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import java.io.IOException;

/**
 * Controlador del menú principal.
 *
 * Gestiona la navegación hacia los módulos principales de la aplicación
 * y adapta las opciones visibles según el rol del usuario autenticado.
 */
public class MainMenuController {

    @FXML
    private Label usuarioActualLabel;

    @FXML
    private Button usuariosButton;

    @FXML
    private Button categoriasButton;

    @FXML
    private Button productosButton;

    @FXML
    private Button informesButton;

    /**
     * Método de inicialización automático de JavaFX.
     *
     * Muestra la información del usuario actual y configura
     * los accesos según su rol.
     */
    @FXML
    public void initialize() {
        if (!SessionManager.haySesionActiva()) {
            usuarioActualLabel.setText("Usuario: sin sesión");
            return;
        }

        Usuario usuario = SessionManager.getUsuarioActual();

        usuarioActualLabel.setText(
                "Usuario: " + usuario.getNombre() + " (" + usuario.getRol() + ")"
        );

        configurarTooltips();
        configurarAccesosSegunRol(usuario.getRol());
    }

    /**
     * Navega a la vista de mesas.
     *
     * @param event evento del botón
     */
    @FXML
    private void irAMesas(ActionEvent event) {
        navegar(event, "/fxml/MesasView.fxml", "TPV - Mesas");
    }

    /**
     * Navega a la vista de productos si el usuario es administrador.
     *
     * @param event evento del botón
     */
    @FXML
    private void irAProductos(ActionEvent event) {
        if (!esAdmin()) {
            System.out.println("Acceso denegado a Productos.");
            return;
        }

        navegar(event, "/fxml/ProductosView.fxml", "TPV - Productos");
    }

    /**
     * Navega a la vista de categorías si el usuario es administrador.
     *
     * @param event evento del botón
     */
    @FXML
    private void irACategorias(ActionEvent event) {
        if (!esAdmin()) {
            System.out.println("Acceso denegado a Categorías.");
            return;
        }

        navegar(event, "/fxml/CategoriasView.fxml", "TPV - Categorías");
    }

    /**
     * Navega a la vista de usuarios si el usuario es administrador.
     *
     * @param event evento del botón
     */
    @FXML
    private void irAUsuarios(ActionEvent event) {
        if (!esAdmin()) {
            System.out.println("Acceso denegado a Usuarios.");
            return;
        }

        navegar(event, "/fxml/UsuariosView.fxml", "TPV - Usuarios");
    }

    /**
     * Navega a la vista de informes si el usuario es administrador.
     *
     * @param event evento del botón
     */
    @FXML
    private void irAInformes(ActionEvent event) {
        if (!esAdmin()) {
            System.out.println("Acceso denegado a Informes.");
            return;
        }

        navegar(event, "/fxml/InformesView.fxml", "TPV - Informes");
    }

    /**
     * Cierra la sesión actual y vuelve a la pantalla de login.
     *
     * @param event evento del botón
     */
    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        boolean confirmada = confirmarAccion(
                "Cerrar sesión",
                "¿Seguro que deseas cerrar la sesión actual?"
        );

        if (!confirmada) {
            return;
        }

        SessionManager.cerrarSesion();
        navegar(event, "/fxml/LoginView.fxml", "TPV - Login");
    }

    /**
     * Configura los tooltips generales del menú.
     */
    private void configurarTooltips() {
        productosButton.setTooltip(new Tooltip("Gestiona el catálogo de productos"));
        categoriasButton.setTooltip(new Tooltip("Administra las categorías"));
        usuariosButton.setTooltip(new Tooltip("Gestión de usuarios"));
        informesButton.setTooltip(new Tooltip("Consulta estadísticas del negocio"));
    }

    /**
     * Aplica restricciones visuales según el rol del usuario.
     *
     * @param rol rol del usuario autenticado
     */
    private void configurarAccesosSegunRol(Rol rol) {
        if (rol == Rol.CAMARERO) {
            aplicarRestriccionVisual(productosButton);
            aplicarRestriccionVisual(categoriasButton);
            aplicarRestriccionVisual(usuariosButton);
            aplicarRestriccionVisual(informesButton);
        }
    }

    /**
     * Aplica estilo visual de acceso restringido a un botón.
     *
     * @param button botón a modificar visualmente
     */
    private void aplicarRestriccionVisual(Button button) {
        button.setOpacity(0.55);
        button.setTooltip(new Tooltip("Acceso restringido a administradores"));
    }

    /**
     * Comprueba si el usuario actual tiene rol de administrador.
     *
     * @return true si existe sesión activa y el usuario es ADMIN
     */
    private boolean esAdmin() {
        return SessionManager.haySesionActiva()
                && SessionManager.getUsuarioActual().getRol() == Rol.ADMIN;
    }

    /**
     * Navega entre vistas usando el NavigationManager.
     *
     * @param event evento que origina la navegación
     * @param rutaFXML ruta del archivo FXML
     * @param titulo título de la ventana
     */
    private void navegar(ActionEvent event, String rutaFXML, String titulo) {
        try {
            NavigationManager.cambiarVista((Node) event.getSource(), rutaFXML, titulo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Muestra un cuadro de confirmación antes de ejecutar una acción sensible.
     *
     * @param titulo título de la ventana
     * @param mensaje mensaje de confirmación
     * @return true si el usuario confirma, false en caso contrario
     */
    private boolean confirmarAccion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }
}