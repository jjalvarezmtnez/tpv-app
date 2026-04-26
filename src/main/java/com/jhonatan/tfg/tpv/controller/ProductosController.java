package com.jhonatan.tfg.tpv.controller;

import com.jhonatan.tfg.tpv.dao.CategoriaDAO;
import com.jhonatan.tfg.tpv.dao.ProductoDAO;
import com.jhonatan.tfg.tpv.model.Categoria;
import com.jhonatan.tfg.tpv.model.Producto;
import com.jhonatan.tfg.tpv.util.AccessManager;
import com.jhonatan.tfg.tpv.util.MessageUtils;
import com.jhonatan.tfg.tpv.util.MoneyUtils;
import com.jhonatan.tfg.tpv.util.NavigationManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.io.IOException;

/**
 * Controlador de la vista de productos.
 *
 * Permite consultar, crear, actualizar y eliminar productos del catálogo.
 * También carga las categorías disponibles para asociarlas a cada producto.
 */
public class ProductosController {

    @FXML
    private TableView<Producto> productosTableView;

    @FXML
    private TableColumn<Producto, String> nombreColumn;

    @FXML
    private TableColumn<Producto, String> precioColumn;

    @FXML
    private TableColumn<Producto, Integer> stockColumn;

    @FXML
    private TableColumn<Producto, String> categoriaColumn;

    @FXML
    private TextField nombreField;

    @FXML
    private TextField precioField;

    @FXML
    private TextField stockField;

    @FXML
    private ComboBox<Categoria> categoriaComboBox;

    @FXML
    private Button actualizarProductoButton;

    @FXML
    private Button eliminarProductoButton;

    @FXML
    private Label mensajeLabel;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private Producto productoSeleccionado;

    /**
     * Método de inicialización automático de JavaFX.
     *
     * Valida permisos, configura la tabla, carga productos/categorías
     * y prepara los eventos de selección.
     */
    @FXML
    public void initialize() {
        if (!AccessManager.esAdmin()) {
            redirigirAlMenuPrincipal();
            return;
        }

        configurarTablaProductos();
        configurarVista();
        configurarTooltips();
        configurarSeleccionProducto();

        cargarProductos();
        cargarCategorias();
        actualizarEstadoBotones();
    }

    /**
     * Configura elementos visuales iniciales de la vista.
     */
    private void configurarVista() {
        productosTableView.setPlaceholder(new Label("No hay productos registrados."));
    }

    /**
     * Configura los textos de ayuda de los controles principales.
     */
    private void configurarTooltips() {
        actualizarProductoButton.setTooltip(new Tooltip("Actualiza el producto seleccionado"));
        eliminarProductoButton.setTooltip(new Tooltip("Elimina el producto seleccionado"));
        categoriaComboBox.setTooltip(new Tooltip("Selecciona la categoría del producto"));
    }

    /**
     * Configura las columnas de la tabla de productos.
     */
    private void configurarTablaProductos() {
        productosTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productosTableView.setFixedCellSize(34);

        nombreColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombre())
        );

        precioColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(MoneyUtils.formatearEuros(cellData.getValue().getPrecio()))
        );

        stockColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getStock())
        );

        configurarColumnaStock();

        categoriaColumn.setCellValueFactory(cellData -> {
            Categoria categoria = categoriaDAO.buscarPorId(cellData.getValue().getIdCategoria());
            String nombreCategoria = categoria != null ? categoria.getNombre() : "Sin categoría";
            return new SimpleStringProperty(nombreCategoria);
        });
    }

    /**
     * Configura el estilo visual de la columna stock.
     *
     * Aplica estilos distintos para stock normal, bajo o agotado.
     */
    private void configurarColumnaStock() {
        stockColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);

                getStyleClass().removeAll("stock-normal", "stock-low", "stock-empty");

                if (empty || stock == null) {
                    setText(null);
                    return;
                }

                setText(String.valueOf(stock));

                if (stock <= 0) {
                    getStyleClass().add("stock-empty");
                } else if (stock <= 10) {
                    getStyleClass().add("stock-low");
                } else {
                    getStyleClass().add("stock-normal");
                }
            }
        });
    }

    /**
     * Carga todos los productos desde la base de datos.
     */
    private void cargarProductos() {
        productosTableView.setItems(
                FXCollections.observableArrayList(productoDAO.obtenerTodosLosProductos())
        );
    }

    /**
     * Carga todas las categorías disponibles.
     */
    private void cargarCategorias() {
        categoriaComboBox.setItems(
                FXCollections.observableArrayList(categoriaDAO.obtenerTodasLasCategorias())
        );
    }

    /**
     * Configura la selección de productos desde la tabla.
     */
    private void configurarSeleccionProducto() {
        productosTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, nuevoProducto) -> {
                    if (nuevoProducto != null) {
                        cargarProductoEnFormulario(nuevoProducto);
                    }

                    actualizarEstadoBotones();
                }
        );
    }

    /**
     * Carga los datos del producto seleccionado en el formulario.
     *
     * @param producto producto seleccionado
     */
    private void cargarProductoEnFormulario(Producto producto) {
        productoSeleccionado = producto;

        nombreField.setText(producto.getNombre());
        precioField.setText(String.valueOf(producto.getPrecio()));
        stockField.setText(String.valueOf(producto.getStock()));

        Categoria categoria = categoriaDAO.buscarPorId(producto.getIdCategoria());
        categoriaComboBox.setValue(categoria);
    }

    /**
     * Crea un nuevo producto a partir de los datos del formulario.
     */
    @FXML
    private void handleCrearProducto() {
        MessageUtils.clear(mensajeLabel);

        Producto producto = construirProductoDesdeFormulario(null);

        if (producto == null) {
            return;
        }

        if (productoDAO.existeProducto(producto.getNombre())) {
            MessageUtils.showErrorAuto(mensajeLabel, "Ya existe un producto con ese nombre.", 3);
            return;
        }

        boolean creado = productoDAO.crearProducto(producto);

        if (creado) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Producto guardado correctamente.", 2.5);
            limpiarFormulario();
            cargarProductos();
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "No se pudo guardar el producto.", 3.5);
        }
    }

    /**
     * Actualiza los datos del producto seleccionado.
     */
    @FXML
    private void handleActualizarProducto() {
        MessageUtils.clear(mensajeLabel);

        if (productoSeleccionado == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Primero carga o selecciona un producto.", 3);
            return;
        }

        Producto producto = construirProductoDesdeFormulario(productoSeleccionado);

        if (producto == null) {
            return;
        }

        if (productoDAO.existeProductoConNombreExcluyendoId(producto.getNombre(), producto.getIdProducto())) {
            MessageUtils.showErrorAuto(mensajeLabel, "Ya existe otro producto con ese nombre.", 3);
            return;
        }

        boolean actualizado = productoDAO.actualizarProducto(producto);

        if (actualizado) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Producto actualizado.", 2.5);
            limpiarFormulario();
            cargarProductos();
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "No se pudo actualizar el producto.", 3.5);
        }
    }

    /**
     * Elimina el producto seleccionado.
     *
     * Puede fallar si el producto está asociado a líneas de comanda.
     */
    @FXML
    private void handleEliminarProducto() {
        MessageUtils.clear(mensajeLabel);

        Producto seleccionado = productosTableView.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Selecciona un producto.", 3);
            return;
        }

        boolean eliminado = productoDAO.eliminarProducto(seleccionado.getIdProducto());

        if (eliminado) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Producto eliminado.", 2.5);
            limpiarFormulario();
            cargarProductos();
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "No se pudo eliminar (puede estar en uso).", 3.5);
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
     * Construye un objeto Producto a partir de los datos del formulario.
     *
     * Si el parámetro productoBase es null, crea un producto nuevo.
     * Si contiene un producto existente, modifica ese mismo objeto.
     *
     * @param productoBase producto existente o null para crear uno nuevo
     * @return producto validado o null si hay errores de validación
     */
    private Producto construirProductoDesdeFormulario(Producto productoBase) {
        String nombre = nombreField.getText();
        String precioTexto = precioField.getText();
        String stockTexto = stockField.getText();
        Categoria categoria = categoriaComboBox.getValue();

        if (nombre == null || nombre.isBlank()
                || precioTexto == null || precioTexto.isBlank()
                || stockTexto == null || stockTexto.isBlank()
                || categoria == null) {

            MessageUtils.showErrorAuto(mensajeLabel, "Completa todos los campos.", 3);
            return null;
        }

        double precio;
        int stock;

        try {
            precio = Double.parseDouble(precioTexto.trim());
            stock = Integer.parseInt(stockTexto.trim());
        } catch (NumberFormatException e) {
            MessageUtils.showErrorAuto(mensajeLabel, "Precio o stock con formato inválido.", 3.5);
            return null;
        }

        if (precio < 0) {
            MessageUtils.showErrorAuto(mensajeLabel, "El precio no puede ser negativo.", 3);
            return null;
        }

        if (stock < 0) {
            MessageUtils.showErrorAuto(mensajeLabel, "El stock no puede ser negativo.", 3);
            return null;
        }

        Producto producto = productoBase != null ? productoBase : new Producto();

        producto.setNombre(nombre.trim());
        producto.setPrecio(MoneyUtils.redondear(precio));
        producto.setStock(stock);
        producto.setIdCategoria(categoria.getIdCategoria());

        return producto;
    }

    /**
     * Limpia los campos del formulario y reinicia la selección.
     */
    private void limpiarFormulario() {
        nombreField.clear();
        precioField.clear();
        stockField.clear();
        categoriaComboBox.setValue(null);
        productoSeleccionado = null;
        productosTableView.getSelectionModel().clearSelection();
        actualizarEstadoBotones();
    }

    /**
     * Actualiza el estado de los botones según exista un producto seleccionado.
     */
    private void actualizarEstadoBotones() {
        boolean hayProductoSeleccionado = productoSeleccionado != null;

        actualizarProductoButton.setDisable(!hayProductoSeleccionado);
        eliminarProductoButton.setDisable(!hayProductoSeleccionado);
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