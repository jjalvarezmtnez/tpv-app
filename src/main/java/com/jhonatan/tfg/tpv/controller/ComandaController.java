package com.jhonatan.tfg.tpv.controller;

import com.jhonatan.tfg.tpv.dao.ComandaDAO;
import com.jhonatan.tfg.tpv.dao.ProductoDAO;
import com.jhonatan.tfg.tpv.model.Comanda;
import com.jhonatan.tfg.tpv.model.LineaComanda;
import com.jhonatan.tfg.tpv.model.Mesa;
import com.jhonatan.tfg.tpv.model.Producto;
import com.jhonatan.tfg.tpv.model.Usuario;
import com.jhonatan.tfg.tpv.service.ComandaService;
import com.jhonatan.tfg.tpv.util.MessageUtils;
import com.jhonatan.tfg.tpv.util.MoneyUtils;
import com.jhonatan.tfg.tpv.util.NavigationManager;
import com.jhonatan.tfg.tpv.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.List;

/**
 * Controlador de la vista de comandas.
 *
 * Gestiona la comanda asociada a una mesa concreta, permitiendo:
 * - abrir una comanda automáticamente al añadir productos
 * - añadir productos con control de stock
 * - reducir o eliminar líneas desde la tabla
 * - cerrar o anular comandas según su estado
 */
public class ComandaController {

    @FXML
    private Label mesaLabel;

    @FXML
    private Label estadoComandaLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Label mensajeLabel;

    @FXML
    private Label stockDisponibleLabel;

    @FXML
    private Label precioProductoLabel;

    @FXML
    private ComboBox<Producto> productoComboBox;

    @FXML
    private TextField cantidadField;

    @FXML
    private Button anadirProductoButton;

    @FXML
    private Button anularComandaButton;

    @FXML
    private Button cerrarComandaButton;

    @FXML
    private TableView<LineaComanda> lineasTableView;

    @FXML
    private TableColumn<LineaComanda, String> productoLineaColumn;

    @FXML
    private TableColumn<LineaComanda, Number> cantidadLineaColumn;

    @FXML
    private TableColumn<LineaComanda, String> precioLineaColumn;

    @FXML
    private TableColumn<LineaComanda, String> subtotalLineaColumn;

    @FXML
    private TableColumn<LineaComanda, Void> accionesLineaColumn;

    private final ComandaService comandaService = new ComandaService();
    private final ComandaDAO comandaDAO = new ComandaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    private Mesa mesaActual;

    /**
     * Método de inicialización automático de JavaFX.
     *
     * Configura la tabla, listeners, tooltips y estado inicial de los controles.
     */
    @FXML
    public void initialize() {
        cantidadField.setText("1");

        configurarTablaLineas();
        configurarListeners();
        configurarTooltips();

        lineasTableView.setPlaceholder(new Label("No hay productos en la comanda."));

        actualizarEstadoBotones();
    }

    /**
     * Recibe la mesa seleccionada desde la vista de mesas.
     *
     * @param mesa mesa que se va a gestionar
     */
    public void setMesaActual(Mesa mesa) {
        this.mesaActual = mesa;

        cargarProductosDisponibles();
        cargarDatosMesaYComanda();
        actualizarInfoProductoSeleccionado(productoComboBox.getValue());
        actualizarEstadoBotones();
    }

    /**
     * Configura los listeners de la vista.
     */
    private void configurarListeners() {
        productoComboBox.valueProperty().addListener((observable, oldValue, nuevoProducto) -> {
            actualizarInfoProductoSeleccionado(nuevoProducto);
            actualizarEstadoBotones();
        });

        lineasTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                actualizarEstadoBotones()
        );
    }

    /**
     * Configura los textos de ayuda emergentes.
     */
    private void configurarTooltips() {
        anadirProductoButton.setTooltip(new Tooltip("Añade el producto seleccionado a la comanda"));
        anularComandaButton.setTooltip(new Tooltip("Anula la comanda si está abierta y no tiene productos"));
        cerrarComandaButton.setTooltip(new Tooltip("Cierra la comanda y registra la venta"));
    }

    /**
     * Configura la tabla de líneas de comanda.
     *
     * Cada línea muestra producto, cantidad, precio unitario,
     * subtotal y acciones directas.
     */
    private void configurarTablaLineas() {
        lineasTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        lineasTableView.setFixedCellSize(34);

        productoLineaColumn.setCellValueFactory(cellData -> {
            Producto producto = productoDAO.buscarPorId(cellData.getValue().getIdProducto());
            String nombreProducto = producto != null ? producto.getNombre() : "Producto desconocido";
            return new SimpleStringProperty(nombreProducto);
        });

        cantidadLineaColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCantidad())
        );

        precioLineaColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(MoneyUtils.formatearEuros(cellData.getValue().getPrecioUnitario()))
        );

        subtotalLineaColumn.setCellValueFactory(cellData -> {
            LineaComanda linea = cellData.getValue();
            double subtotal = linea.getCantidad() * linea.getPrecioUnitario();
            return new SimpleStringProperty(MoneyUtils.formatearEuros(subtotal));
        });

        configurarColumnaAcciones();

        productoLineaColumn.setMaxWidth(1f * Integer.MAX_VALUE * 30);
        cantidadLineaColumn.setMaxWidth(1f * Integer.MAX_VALUE * 12);
        precioLineaColumn.setMaxWidth(1f * Integer.MAX_VALUE * 13);
        subtotalLineaColumn.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        accionesLineaColumn.setMaxWidth(1f * Integer.MAX_VALUE * 30);
    }

    /**
     * Configura la columna de acciones de la tabla.
     */
    private void configurarColumnaAcciones() {
        accionesLineaColumn.setCellFactory(column -> new TableCell<>() {

            private final Button reducirButton = new Button("−");
            private final Button eliminarButton = new Button("×");
            private final HBox accionesBox = new HBox(3, reducirButton, eliminarButton);

            {
                reducirButton.getStyleClass().add("table-action-secondary");
                eliminarButton.getStyleClass().add("table-action-danger");

                reducirButton.setTooltip(new Tooltip("Reducir cantidad"));
                eliminarButton.setTooltip(new Tooltip("Eliminar línea"));

                accionesBox.setAlignment(Pos.CENTER);

                reducirButton.setOnAction(event -> {
                    LineaComanda linea = getTableView().getItems().get(getIndex());
                    reducirLineaDesdeTabla(linea);
                });

                eliminarButton.setOnAction(event -> {
                    LineaComanda linea = getTableView().getItems().get(getIndex());
                    eliminarLineaDesdeTabla(linea);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : accionesBox);
                setAlignment(Pos.CENTER);
            }
        });
    }

    /**
     * Carga los productos disponibles en el ComboBox.
     */
    private void cargarProductosDisponibles() {
        List<Producto> productos = productoDAO.obtenerTodosLosProductos();
        productoComboBox.setItems(FXCollections.observableArrayList(productos));
    }

    /**
     * Carga la información general de la mesa y su comanda abierta.
     */
    private void cargarDatosMesaYComanda() {
        if (mesaActual == null) {
            return;
        }

        mesaLabel.setText("Mesa: " + mesaActual.getNumero());

        Comanda comandaAbierta = comandaDAO.buscarComandaAbiertaPorMesa(mesaActual.getIdMesa());

        if (comandaAbierta != null) {
            estadoComandaLabel.setText("Estado de la comanda: ABIERTA");

            double total = comandaService.obtenerTotalComandaAbierta(mesaActual.getIdMesa());
            totalLabel.setText("Total: " + MoneyUtils.formatearEuros(total));

            cargarLineasComanda();
        } else {
            estadoComandaLabel.setText("Estado de la comanda: SIN ABRIR");
            totalLabel.setText("Total: " + MoneyUtils.formatearEuros(0.0));
            lineasTableView.getItems().clear();
        }

        actualizarEstadoBotones();
    }

    /**
     * Carga las líneas de la comanda abierta en la tabla.
     */
    private void cargarLineasComanda() {
        List<LineaComanda> lineas = comandaService.obtenerLineasDeComandaAbierta(mesaActual.getIdMesa());
        lineasTableView.setItems(FXCollections.observableArrayList(lineas));
        actualizarEstadoBotones();
    }

    /**
     * Actualiza la información visual del producto seleccionado.
     *
     * Muestra precio y stock actual, aplicando estilos según nivel de stock.
     *
     * @param producto producto seleccionado
     */
    private void actualizarInfoProductoSeleccionado(Producto producto) {
        precioProductoLabel.setText("Precio: -");
        stockDisponibleLabel.setText("Stock disponible: -");

        stockDisponibleLabel.getStyleClass().removeAll("stock-normal", "stock-low", "stock-empty");

        if (producto == null) {
            stockDisponibleLabel.getStyleClass().add("stock-normal");
            return;
        }

        Producto productoActualizado = productoDAO.buscarPorId(producto.getIdProducto());

        if (productoActualizado == null) {
            precioProductoLabel.setText("Precio: no disponible");
            stockDisponibleLabel.setText("Stock disponible: no disponible");
            stockDisponibleLabel.getStyleClass().add("stock-empty");
            return;
        }

        precioProductoLabel.setText("Precio: " +
                MoneyUtils.formatearEuros(productoActualizado.getPrecio()));

        int stock = productoActualizado.getStock();

        if (stock <= 0) {
            stockDisponibleLabel.setText("Stock disponible: 0 (sin stock)");
            stockDisponibleLabel.getStyleClass().add("stock-empty");
        } else if (stock <= 10) {
            stockDisponibleLabel.setText("Stock disponible: " + stock + " (stock bajo)");
            stockDisponibleLabel.getStyleClass().add("stock-low");
        } else {
            stockDisponibleLabel.setText("Stock disponible: " + stock);
            stockDisponibleLabel.getStyleClass().add("stock-normal");
        }
    }

    /**
     * Añade un producto a la comanda.
     *
     * Flujo:
     * 1. Validación de entrada
     * 2. Validación de stock
     * 3. Creación automática de comanda si no existe
     * 4. Inserción de línea
     */
    @FXML
    private void handleAnadirProducto() {
        MessageUtils.clear(mensajeLabel);

        if (mesaActual == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "No hay ninguna mesa seleccionada.", 3);
            return;
        }

        Producto productoSeleccionado = productoComboBox.getValue();
        String cantidadTexto = cantidadField.getText();

        if (productoSeleccionado == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Selecciona un producto.", 3);
            return;
        }

        if (cantidadTexto == null || cantidadTexto.isBlank()) {
            MessageUtils.showErrorAuto(mensajeLabel, "Introduce una cantidad.", 3);
            return;
        }

        int cantidad;

        try {
            cantidad = Integer.parseInt(cantidadTexto);
        } catch (NumberFormatException e) {
            MessageUtils.showErrorAuto(mensajeLabel, "La cantidad debe ser un número entero.", 3.5);
            return;
        }

        if (cantidad <= 0) {
            MessageUtils.showErrorAuto(mensajeLabel, "La cantidad debe ser mayor que cero.", 3);
            return;
        }

        Producto productoActualizado = productoDAO.buscarPorId(productoSeleccionado.getIdProducto());

        if (productoActualizado == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Producto no disponible.", 3);
            return;
        }

        if (productoActualizado.getStock() <= 0) {
            MessageUtils.showErrorAuto(mensajeLabel, "Producto sin stock.", 3);
            return;
        }

        if (cantidad > productoActualizado.getStock()) {
            MessageUtils.showErrorAuto(mensajeLabel, "Stock insuficiente.", 3);
            return;
        }

        Comanda comandaAbierta = comandaDAO.buscarComandaAbiertaPorMesa(mesaActual.getIdMesa());

        // Creación automática de comanda
        if (comandaAbierta == null) {
            Usuario usuarioActual = SessionManager.getUsuarioActual();

            if (usuarioActual == null) {
                MessageUtils.showErrorAuto(mensajeLabel, "No hay sesión activa.", 3);
                return;
            }

            boolean abierta = comandaService.abrirComanda(
                    mesaActual.getIdMesa(),
                    usuarioActual.getIdUsuario()
            );

            if (!abierta) {
                MessageUtils.showErrorAuto(mensajeLabel, "No se pudo abrir la comanda.", 3.5);
                return;
            }
        }

        boolean anadido = comandaService.anadirProductoAComanda(
                mesaActual.getIdMesa(),
                productoSeleccionado.getIdProducto(),
                cantidad
        );

        if (anadido) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Producto añadido correctamente.", 2.5);
            cantidadField.setText("1");
            productoComboBox.setValue(null);
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "Error al añadir producto.", 3.5);
        }

        cargarDatosMesaYComanda();
        actualizarEstadoBotones();
    }

    /**
     * Cierra la comanda actual.
     */
    @FXML
    private void handleCerrarComanda() {
        MessageUtils.clear(mensajeLabel);

        if (!confirmarAccion("Cerrar comanda", "¿Seguro que deseas cerrar la comanda?")) {
            return;
        }

        boolean cerrada = comandaService.cerrarComanda(mesaActual.getIdMesa());

        if (cerrada) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Comanda cerrada.", 2.5);
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "Error al cerrar comanda.", 3.5);
        }

        cargarDatosMesaYComanda();
        actualizarEstadoBotones();
    }

    /**
     * Anula la comanda si está vacía.
     */
    @FXML
    private void handleAnularComanda() {
        MessageUtils.clear(mensajeLabel);

        if (!confirmarAccion("Anular comanda", "¿Seguro que deseas anular la comanda?")) {
            return;
        }

        boolean anulada = comandaService.anularComanda(mesaActual.getIdMesa());

        if (anulada) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Comanda anulada.", 2.5);
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "No se pudo anular.", 3.5);
        }

        cargarDatosMesaYComanda();
        actualizarEstadoBotones();
    }

    /**
     * Vuelve a la vista de mesas.
     */
    @FXML
    private void handleVolverMesas(ActionEvent event) {
        try {
            NavigationManager.cambiarVista(
                    (Node) event.getSource(),
                    "/fxml/MesasView.fxml",
                    "TPV - Mesas"
            );
        } catch (IOException e) {
            MessageUtils.showErrorAuto(mensajeLabel, "Error de navegación.", 3.5);
        }
    }

    /**
     * Controla el estado de los botones según el contexto.
     */
    private void actualizarEstadoBotones() {
        boolean hayMesa = mesaActual != null;
        boolean hayProducto = productoComboBox.getValue() != null;

        Comanda comanda = hayMesa
                ? comandaDAO.buscarComandaAbiertaPorMesa(mesaActual.getIdMesa())
                : null;

        boolean hayComanda = comanda != null;

        int lineas = hayComanda
                ? comandaService.obtenerLineasDeComandaAbierta(mesaActual.getIdMesa()).size()
                : 0;

        anadirProductoButton.setDisable(!hayMesa || !hayProducto);
        cerrarComandaButton.setDisable(!hayComanda || lineas == 0);
        anularComandaButton.setDisable(!(hayComanda && lineas == 0));
    }

    /**
     * Confirmación genérica de acciones.
     */
    private boolean confirmarAccion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);

        return alert.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    /**
     * Reduce una unidad de una línea.
     */
    private void reducirLineaDesdeTabla(LineaComanda linea) {
        if (linea == null) return;

        comandaService.reducirCantidadLinea(linea.getIdLinea());

        cargarDatosMesaYComanda();
    }

    /**
     * Elimina una línea.
     */
    private void eliminarLineaDesdeTabla(LineaComanda linea) {
        if (linea == null) return;

        if (!confirmarAccion("Eliminar", "¿Eliminar línea?")) return;

        comandaService.eliminarLineaDeComanda(linea.getIdLinea());

        cargarDatosMesaYComanda();
    }
}