package com.jhonatan.tfg.tpv.controller;

import com.jhonatan.tfg.tpv.dao.UsuarioDAO;
import com.jhonatan.tfg.tpv.model.Usuario;
import com.jhonatan.tfg.tpv.model.enums.Rol;
import com.jhonatan.tfg.tpv.service.UsuarioService;
import com.jhonatan.tfg.tpv.util.AccessManager;
import com.jhonatan.tfg.tpv.util.MessageUtils;
import com.jhonatan.tfg.tpv.util.NavigationManager;
import com.jhonatan.tfg.tpv.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.io.IOException;

/**
 * Controlador de la vista de usuarios.
 *
 * Permite a los administradores crear, actualizar y desactivar usuarios.
 * La desactivación es lógica, no física, para conservar trazabilidad histórica.
 */
public class UsuariosController {

    @FXML
    private TableView<Usuario> usuariosTableView;

    @FXML
    private TableColumn<Usuario, String> usernameColumn;

    @FXML
    private TableColumn<Usuario, String> nombreColumn;

    @FXML
    private TableColumn<Usuario, String> rolColumn;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField nombreField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<Rol> rolComboBox;

    @FXML
    private Button actualizarUsuarioButton;

    @FXML
    private Button desactivarUsuarioButton;

    @FXML
    private Label mensajeLabel;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final UsuarioService usuarioService = new UsuarioService();

    private Usuario usuarioSeleccionado;

    /**
     * Método de inicialización automático de JavaFX.
     *
     * Valida permisos de administrador, configura la tabla,
     * carga usuarios y prepara los controles de la vista.
     */
    @FXML
    public void initialize() {
        if (!AccessManager.esAdmin()) {
            redirigirAlMenuPrincipal();
            return;
        }

        configurarTablaUsuarios();
        configurarVista();
        configurarTooltips();
        configurarSeleccionUsuario();

        cargarUsuarios();
        cargarRoles();
        actualizarEstadoBotones();
    }

    /**
     * Configura elementos visuales iniciales de la vista.
     */
    private void configurarVista() {
        usuariosTableView.setPlaceholder(new Label("No hay usuarios activos."));
    }

    /**
     * Configura los textos de ayuda de los botones.
     */
    private void configurarTooltips() {
        actualizarUsuarioButton.setTooltip(new Tooltip("Actualiza el usuario seleccionado"));
        desactivarUsuarioButton.setTooltip(new Tooltip("Desactiva el usuario seleccionado sin eliminarlo"));
    }

    /**
     * Configura las columnas de la tabla de usuarios.
     */
    private void configurarTablaUsuarios() {
        usuariosTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        usuariosTableView.setFixedCellSize(34);

        usernameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUsername())
        );

        nombreColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombre())
        );

        rolColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRol().name())
        );
    }

    /**
     * Carga los usuarios activos desde la base de datos.
     */
    private void cargarUsuarios() {
        usuariosTableView.setItems(
                FXCollections.observableArrayList(usuarioDAO.obtenerTodosLosUsuarios())
        );
    }

    /**
     * Carga los roles disponibles en el ComboBox.
     */
    private void cargarRoles() {
        rolComboBox.setItems(FXCollections.observableArrayList(Rol.values()));
    }

    /**
     * Configura la selección de usuarios desde la tabla.
     */
    private void configurarSeleccionUsuario() {
        usuariosTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, nuevoUsuario) -> {
                    if (nuevoUsuario != null) {
                        cargarUsuarioEnFormulario(nuevoUsuario);
                    }

                    actualizarEstadoBotones();
                }
        );
    }

    /**
     * Carga en el formulario los datos del usuario seleccionado.
     *
     * La contraseña no se muestra por seguridad.
     *
     * @param usuario usuario seleccionado
     */
    private void cargarUsuarioEnFormulario(Usuario usuario) {
        usuarioSeleccionado = usuario;

        usernameField.setText(usuario.getUsername());
        nombreField.setText(usuario.getNombre());
        passwordField.clear();
        rolComboBox.setValue(usuario.getRol());
    }

    /**
     * Crea un nuevo usuario.
     */
    @FXML
    private void handleCrearUsuario() {
        MessageUtils.clear(mensajeLabel);

        Usuario usuario = construirUsuarioDesdeFormulario(null);

        if (usuario == null) {
            return;
        }

        boolean creado = usuarioService.registrarUsuario(usuario);

        if (creado) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Usuario guardado correctamente.", 2.5);
            limpiarFormulario();
            cargarUsuarios();
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "No se pudo guardar el usuario.", 3.5);
        }
    }

    /**
     * Actualiza el usuario seleccionado.
     *
     * Requiere introducir una nueva contraseña porque la contraseña original
     * no se recupera ni se muestra por seguridad.
     */
    @FXML
    private void handleActualizarUsuario() {
        MessageUtils.clear(mensajeLabel);

        if (usuarioSeleccionado == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Primero carga o selecciona un usuario.", 3);
            return;
        }

        Usuario usuario = construirUsuarioDesdeFormulario(usuarioSeleccionado);

        if (usuario == null) {
            return;
        }

        boolean actualizado = usuarioService.actualizarUsuario(usuario);

        if (actualizado) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Usuario actualizado correctamente.", 2.5);
            limpiarFormulario();
            cargarUsuarios();
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "No se pudo actualizar el usuario.", 3.5);
        }
    }

    /**
     * Desactiva el usuario seleccionado sin eliminarlo físicamente.
     *
     * El usuario deja de poder iniciar sesión, pero permanece en la base
     * de datos para conservar referencias históricas.
     */
    @FXML
    private void handleDesactivarUsuario() {
        MessageUtils.clear(mensajeLabel);

        Usuario seleccionado = usuariosTableView.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Selecciona un usuario.", 3);
            return;
        }

        if (esUsuarioActual(seleccionado)) {
            MessageUtils.showErrorAuto(mensajeLabel, "No puedes desactivar tu propio usuario.", 3.5);
            return;
        }

        boolean confirmada = confirmarAccion(
                "Desactivar usuario",
                "¿Seguro que deseas desactivar el usuario seleccionado?"
        );

        if (!confirmada) {
            MessageUtils.showWarningAuto(mensajeLabel, "Desactivación cancelada.", 2.5);
            return;
        }

        boolean desactivado = usuarioService.desactivarUsuario(seleccionado.getIdUsuario());

        if (desactivado) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Usuario desactivado correctamente.", 2.5);
            limpiarFormulario();
            cargarUsuarios();
        } else {
            MessageUtils.showErrorAuto(
                    mensajeLabel,
                    "No se pudo desactivar el usuario. Debe quedar al menos un administrador activo.",
                    4
            );
        }
    }

    /**
     * Vuelve al menú principal.
     *
     * @param event evento del botón
     */
    @FXML
    private void handleVolverMenu(ActionEvent event) {
        MessageUtils.clear(mensajeLabel);

        try {
            NavigationManager.cambiarVista(
                    (Node) event.getSource(),
                    "/fxml/MainMenuView.fxml",
                    "TPV - Menú Principal"
            );
        } catch (IOException e) {
            MessageUtils.showErrorAuto(mensajeLabel, "Error al volver al menú principal.", 3.5);
            e.printStackTrace();
        }
    }

    /**
     * Construye un usuario a partir de los campos del formulario.
     *
     * @param usuarioBase usuario existente o null para crear uno nuevo
     * @return usuario validado o null si hay errores
     */
    private Usuario construirUsuarioDesdeFormulario(Usuario usuarioBase) {
        String username = usernameField.getText();
        String nombre = nombreField.getText();
        String password = passwordField.getText();
        Rol rol = rolComboBox.getValue();

        if (username == null || username.isBlank()
                || nombre == null || nombre.isBlank()
                || password == null || password.isBlank()
                || rol == null) {

            MessageUtils.showErrorAuto(mensajeLabel, "Completa todos los campos.", 3);
            return null;
        }

        Usuario usuario = usuarioBase != null ? usuarioBase : new Usuario();

        usuario.setUsername(username.trim());
        usuario.setNombre(nombre.trim());
        usuario.setPassword(password);
        usuario.setRol(rol);

        return usuario;
    }

    /**
     * Comprueba si el usuario indicado es el usuario actualmente autenticado.
     *
     * @param usuario usuario a comparar
     * @return true si coincide con la sesión actual
     */
    private boolean esUsuarioActual(Usuario usuario) {
        return SessionManager.haySesionActiva()
                && SessionManager.getUsuarioActual().getIdUsuario() == usuario.getIdUsuario();
    }

    /**
     * Limpia el formulario y reinicia la selección.
     */
    private void limpiarFormulario() {
        usernameField.clear();
        nombreField.clear();
        passwordField.clear();
        rolComboBox.setValue(null);
        usuarioSeleccionado = null;
        usuariosTableView.getSelectionModel().clearSelection();
        actualizarEstadoBotones();
    }

    /**
     * Actualiza el estado de los botones según exista un usuario seleccionado.
     */
    private void actualizarEstadoBotones() {
        boolean hayUsuarioSeleccionado = usuarioSeleccionado != null;

        actualizarUsuarioButton.setDisable(!hayUsuarioSeleccionado);
        desactivarUsuarioButton.setDisable(!hayUsuarioSeleccionado);
    }

    /**
     * Muestra una confirmación antes de ejecutar una acción sensible.
     *
     * @param titulo título de la ventana
     * @param mensaje mensaje de confirmación
     * @return true si el usuario confirma
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

    /**
     * Redirige al menú principal si el usuario no tiene permisos.
     */
    private void redirigirAlMenuPrincipal() {
        Platform.runLater(() -> {
            try {
                NavigationManager.cambiarVista(
                        (Node) mensajeLabel,
                        "/fxml/MainMenuView.fxml",
                        "TPV - Menú Principal"
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}