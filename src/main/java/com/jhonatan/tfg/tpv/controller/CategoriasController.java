package com.jhonatan.tfg.tpv.controller;

import com.jhonatan.tfg.tpv.dao.CategoriaDAO;
import com.jhonatan.tfg.tpv.model.Categoria;
import com.jhonatan.tfg.tpv.util.AccessManager;
import com.jhonatan.tfg.tpv.util.MessageUtils;
import com.jhonatan.tfg.tpv.util.NavigationManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;

/**
 * Controlador de la vista de categorías.
 *
 * Gestiona la interacción entre la interfaz y la capa DAO,
 * permitiendo listar, crear, actualizar y eliminar categorías.
 */
public class CategoriasController {

    @FXML
    private ListView<Categoria> categoriasListView;

    @FXML
    private TextField nombreField;

    @FXML
    private Button actualizarCategoriaButton;

    @FXML
    private Button eliminarCategoriaButton;

    @FXML
    private Label mensajeLabel;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private Categoria categoriaSeleccionada;

    /**
     * Método de inicialización automático de JavaFX.
     *
     * Valida permisos de administrador, carga los datos iniciales
     * y configura el comportamiento de selección.
     */
    @FXML
    public void initialize() {
        if (!AccessManager.esAdmin()) {
            redirigirAlMenuPrincipal();
            return;
        }

        cargarCategorias();
        configurarVista();
        configurarSeleccionCategoria();
        actualizarEstadoBotones();
    }

    /**
     * Configura elementos visuales iniciales de la vista.
     */
    private void configurarVista() {
        categoriasListView.setPlaceholder(new Label("No hay categorías registradas."));
    }

    /**
     * Carga las categorías desde la base de datos.
     */
    private void cargarCategorias() {
        categoriasListView.setItems(
                FXCollections.observableArrayList(categoriaDAO.obtenerTodasLasCategorias())
        );
    }

    /**
     * Configura la selección de categorías desde la lista.
     */
    private void configurarSeleccionCategoria() {
        categoriasListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, nuevaCategoria) -> {
                    if (nuevaCategoria != null) {
                        cargarCategoriaEnFormulario(nuevaCategoria);
                    }

                    actualizarEstadoBotones();
                }
        );
    }

    /**
     * Guarda una nueva categoría.
     */
    @FXML
    private void handleCrearCategoria() {
        MessageUtils.clear(mensajeLabel);

        String nombre = obtenerNombreFormulario();

        if (nombre == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Introduce un nombre de categoría.", 3);
            return;
        }

        if (categoriaDAO.existeCategoria(nombre)) {
            MessageUtils.showErrorAuto(mensajeLabel, "Ya existe una categoría con ese nombre.", 3);
            return;
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);

        boolean creada = categoriaDAO.crearCategoria(categoria);

        if (creada) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Categoría guardada correctamente.", 2.5);
            limpiarFormulario();
            cargarCategorias();
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "No se pudo guardar la categoría.", 3.5);
        }
    }

    /**
     * Actualiza la categoría seleccionada.
     */
    @FXML
    private void handleActualizarCategoria() {
        MessageUtils.clear(mensajeLabel);

        if (categoriaSeleccionada == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Primero carga o selecciona una categoría.", 3);
            return;
        }

        String nombre = obtenerNombreFormulario();

        if (nombre == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Introduce un nombre de categoría.", 3);
            return;
        }

        if (categoriaDAO.existeCategoriaExcluyendoId(nombre, categoriaSeleccionada.getIdCategoria())) {
            MessageUtils.showErrorAuto(mensajeLabel, "Ya existe otra categoría con ese nombre.", 3);
            return;
        }

        categoriaSeleccionada.setNombre(nombre);

        boolean actualizada = categoriaDAO.actualizarCategoria(categoriaSeleccionada);

        if (actualizada) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Categoría actualizada correctamente.", 2.5);
            limpiarFormulario();
            cargarCategorias();
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "No se pudo actualizar la categoría.", 3.5);
        }
    }

    /**
     * Elimina la categoría seleccionada.
     *
     * Puede fallar si existen productos asociados.
     */
    @FXML
    private void handleEliminarCategoria() {
        MessageUtils.clear(mensajeLabel);

        Categoria seleccionada = categoriasListView.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Selecciona una categoría.", 3);
            return;
        }

        boolean eliminada = categoriaDAO.eliminarCategoria(seleccionada.getIdCategoria());

        if (eliminada) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Categoría eliminada correctamente.", 2.5);
            limpiarFormulario();
            cargarCategorias();
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "No se pudo eliminar la categoría (puede estar en uso).", 3.5);
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
     * Carga una categoría en el formulario.
     *
     * @param categoria categoría seleccionada
     */
    private void cargarCategoriaEnFormulario(Categoria categoria) {
        categoriaSeleccionada = categoria;
        nombreField.setText(categoria.getNombre());
    }

    /**
     * Obtiene y valida el nombre introducido.
     *
     * @return nombre limpio o null si está vacío
     */
    private String obtenerNombreFormulario() {
        String nombre = nombreField.getText();

        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        return nombre.trim();
    }

    /**
     * Limpia el formulario y reinicia la selección.
     */
    private void limpiarFormulario() {
        nombreField.clear();
        categoriaSeleccionada = null;
        categoriasListView.getSelectionModel().clearSelection();
        actualizarEstadoBotones();
    }

    /**
     * Actualiza el estado de los botones según haya selección activa.
     */
    private void actualizarEstadoBotones() {
        boolean hayCategoriaSeleccionada = categoriaSeleccionada != null;

        actualizarCategoriaButton.setDisable(!hayCategoriaSeleccionada);
        eliminarCategoriaButton.setDisable(!hayCategoriaSeleccionada);
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