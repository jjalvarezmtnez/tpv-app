package com.jhonatan.tfg.tpv.controller;

import com.jhonatan.tfg.tpv.dao.MesaDAO;
import com.jhonatan.tfg.tpv.model.Mesa;
import com.jhonatan.tfg.tpv.model.enums.EstadoMesa;
import com.jhonatan.tfg.tpv.model.enums.Rol;
import com.jhonatan.tfg.tpv.util.AccessManager;
import com.jhonatan.tfg.tpv.util.MessageUtils;
import com.jhonatan.tfg.tpv.util.NavigationManager;
import com.jhonatan.tfg.tpv.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controlador de la vista de mesas.
 *
 * Permite consultar mesas, abrir/ver comandas y gestionar mesas
 * si el usuario autenticado tiene rol de administrador.
 */
public class MesasController {

    @FXML
    private TableView<Mesa> mesasTableView;

    @FXML
    private TableColumn<Mesa, Integer> numeroMesaColumn;

    @FXML
    private TableColumn<Mesa, EstadoMesa> estadoMesaColumn;

    @FXML
    private Label mesaSeleccionadaLabel;

    @FXML
    private Label mensajeLabel;

    @FXML
    private VBox gestionMesasBox;

    @FXML
    private TextField numeroMesaField;

    @FXML
    private Button abrirComandaButton;

    @FXML
    private Button actualizarMesaButton;

    @FXML
    private Button eliminarMesaButton;

    private final MesaDAO mesaDAO = new MesaDAO();

    private Mesa mesaSeleccionadaAdmin;

    /**
     * Método de inicialización automático de JavaFX.
     *
     * Configura la tabla, carga las mesas y adapta la vista según el rol.
     */
    @FXML
    public void initialize() {
        configurarTablaMesas();
        configurarVista();
        configurarSeleccionMesa();
        configurarVistaSegunRol();
        configurarTooltips();

        cargarMesas();
        actualizarEstadoBotones();
    }

    /**
     * Configura elementos visuales iniciales de la vista.
     */
    private void configurarVista() {
        mesasTableView.setPlaceholder(new Label("No hay mesas disponibles."));
    }

    /**
     * Configura los tooltips de los botones.
     */
    private void configurarTooltips() {
        actualizarMesaButton.setTooltip(new Tooltip("Actualiza la mesa seleccionada"));
        eliminarMesaButton.setTooltip(new Tooltip("Elimina la mesa seleccionada"));
        abrirComandaButton.setTooltip(new Tooltip("Abre o consulta la comanda de la mesa seleccionada"));
    }

    /**
     * Configura las columnas y estilos de la tabla de mesas.
     */
    private void configurarTablaMesas() {
        mesasTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        mesasTableView.setFixedCellSize(34);

        numeroMesaColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getNumero()).asObject()
        );

        estadoMesaColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getEstado())
        );

        estadoMesaColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(EstadoMesa estado, boolean empty) {
                super.updateItem(estado, empty);

                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(estado.toString());

                if (estado == EstadoMesa.OCUPADA) {
                    setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                }
            }
        });
    }

    /**
     * Carga todas las mesas desde la base de datos.
     */
    private void cargarMesas() {
        List<Mesa> mesas = mesaDAO.obtenerTodasLasMesas();
        mesasTableView.setItems(FXCollections.observableArrayList(mesas));
    }

    /**
     * Muestra u oculta el formulario de administración según el rol.
     */
    private void configurarVistaSegunRol() {
        boolean esAdmin = SessionManager.haySesionActiva()
                && SessionManager.getUsuarioActual().getRol() == Rol.ADMIN;

        gestionMesasBox.setVisible(esAdmin);
        gestionMesasBox.setManaged(esAdmin);
    }

    /**
     * Configura el comportamiento al seleccionar una mesa.
     */
    private void configurarSeleccionMesa() {
        mesasTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, nuevaMesa) -> {
                    if (nuevaMesa == null) {
                        return;
                    }

                    cargarMesaSeleccionada(nuevaMesa);
                    MessageUtils.clear(mensajeLabel);
                    actualizarEstadoBotones();
                }
        );
    }

    /**
     * Carga visualmente la mesa seleccionada y rellena el formulario admin.
     *
     * @param mesa mesa seleccionada
     */
    private void cargarMesaSeleccionada(Mesa mesa) {
        mesaSeleccionadaAdmin = mesa;

        mesaSeleccionadaLabel.setText(
                "Mesa seleccionada: " + mesa.getNumero() + " (" + mesa.getEstado() + ")"
        );

        if (AccessManager.esAdmin()) {
            numeroMesaField.setText(String.valueOf(mesa.getNumero()));
        }
    }

    /**
     * Abre la vista de comanda de la mesa seleccionada.
     *
     * @param event evento del botón
     */
    @FXML
    private void handleAbrirComanda(ActionEvent event) {
        MessageUtils.clear(mensajeLabel);

        Mesa mesaSeleccionada = mesasTableView.getSelectionModel().getSelectedItem();

        if (mesaSeleccionada == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Selecciona una mesa primero.", 3);
            return;
        }

        try {
            abrirVistaComanda(event, mesaSeleccionada);
        } catch (Exception e) {
            MessageUtils.showErrorAuto(mensajeLabel, "Error al abrir la vista de comandas.", 3.5);
            e.printStackTrace();
        }
    }

    /**
     * Crea una nueva mesa con estado inicial LIBRE.
     */
    @FXML
    private void handleCrearMesa() {
        MessageUtils.clear(mensajeLabel);

        Integer numero = obtenerNumeroMesaFormulario();

        if (numero == null) {
            return;
        }

        if (mesaDAO.existeMesa(numero)) {
            MessageUtils.showErrorAuto(mensajeLabel, "Ya existe una mesa con ese número.", 3);
            return;
        }

        Mesa mesa = new Mesa();
        mesa.setNumero(numero);
        mesa.setEstado(EstadoMesa.LIBRE);

        boolean creada = mesaDAO.crearMesa(mesa);

        if (creada) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Mesa creada correctamente.", 2.5);
            limpiarFormularioAdmin();
            cargarMesas();
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "No se pudo crear la mesa.", 3.5);
        }
    }

    /**
     * Actualiza el número de la mesa seleccionada.
     */
    @FXML
    private void handleActualizarMesa() {
        MessageUtils.clear(mensajeLabel);

        if (mesaSeleccionadaAdmin == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Primero carga o selecciona una mesa.", 3);
            return;
        }

        Integer numero = obtenerNumeroMesaFormulario();

        if (numero == null) {
            return;
        }

        if (mesaDAO.existeMesaExcluyendoId(numero, mesaSeleccionadaAdmin.getIdMesa())) {
            MessageUtils.showErrorAuto(mensajeLabel, "Ya existe otra mesa con ese número.", 3);
            return;
        }

        mesaSeleccionadaAdmin.setNumero(numero);

        boolean actualizada = mesaDAO.actualizarMesa(mesaSeleccionadaAdmin);

        if (actualizada) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Mesa actualizada correctamente.", 2.5);
            limpiarFormularioAdmin();
            cargarMesas();
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "No se pudo actualizar la mesa.", 3.5);
        }
    }

    /**
     * Elimina la mesa seleccionada si no tiene comandas asociadas.
     */
    @FXML
    private void handleEliminarMesa() {
        MessageUtils.clear(mensajeLabel);

        Mesa seleccionada = mesasTableView.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            MessageUtils.showErrorAuto(mensajeLabel, "Selecciona una mesa.", 3);
            return;
        }

        if (mesaDAO.mesaTieneComandas(seleccionada.getIdMesa())) {
            MessageUtils.showErrorAuto(mensajeLabel, "No se puede eliminar una mesa que ya ha sido utilizada.", 3.5);
            return;
        }

        boolean eliminada = mesaDAO.eliminarMesa(seleccionada.getIdMesa());

        if (eliminada) {
            MessageUtils.showSuccessAuto(mensajeLabel, "Mesa eliminada correctamente.", 2.5);
            limpiarFormularioAdmin();
            cargarMesas();
        } else {
            MessageUtils.showErrorAuto(mensajeLabel, "No se pudo eliminar la mesa.", 3.5);
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
        } catch (Exception e) {
            MessageUtils.showErrorAuto(mensajeLabel, "Error al volver al menú principal.", 3.5);
            e.printStackTrace();
        }
    }

    /**
     * Obtiene y valida el número de mesa introducido.
     *
     * @return número válido o null si hay error de validación
     */
    private Integer obtenerNumeroMesaFormulario() {
        String numeroTexto = numeroMesaField.getText();

        if (numeroTexto == null || numeroTexto.isBlank()) {
            MessageUtils.showErrorAuto(mensajeLabel, "Introduce un número de mesa.", 3);
            return null;
        }

        int numero;

        try {
            numero = Integer.parseInt(numeroTexto.trim());
        } catch (NumberFormatException e) {
            MessageUtils.showErrorAuto(mensajeLabel, "El número de mesa debe ser un entero.", 3);
            return null;
        }

        if (numero <= 0) {
            MessageUtils.showErrorAuto(mensajeLabel, "El número de mesa debe ser mayor que cero.", 3);
            return null;
        }

        return numero;
    }

    /**
     * Limpia el formulario administrativo y la selección actual.
     */
    private void limpiarFormularioAdmin() {
        numeroMesaField.clear();
        mesaSeleccionadaAdmin = null;
        mesasTableView.getSelectionModel().clearSelection();
        mesaSeleccionadaLabel.setText("Ninguna mesa seleccionada");
        actualizarEstadoBotones();
    }

    /**
     * Abre la vista de comanda pasando la mesa seleccionada al controlador destino.
     *
     * @param event evento que origina la navegación
     * @param mesaSeleccionada mesa que se va a gestionar
     * @throws Exception si ocurre un error al cargar la vista
     */
    private void abrirVistaComanda(ActionEvent event, Mesa mesaSeleccionada) throws Exception {
        NavigationManager.cambiarVista(
                (Node) event.getSource(),
                "/fxml/ComandaView.fxml",
                "TPV - Comanda",
                (ComandaController controller) -> controller.setMesaActual(mesaSeleccionada)
        );
    }

    /**
     * Actualiza el estado de los botones según exista una mesa seleccionada.
     */
    private void actualizarEstadoBotones() {
        boolean hayMesaSeleccionada = mesaSeleccionadaAdmin != null;

        abrirComandaButton.setDisable(!hayMesaSeleccionada);
        actualizarMesaButton.setDisable(!hayMesaSeleccionada);
        eliminarMesaButton.setDisable(!hayMesaSeleccionada);
    }
}