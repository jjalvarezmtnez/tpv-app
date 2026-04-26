package com.jhonatan.tfg.tpv.controller;

import com.jhonatan.tfg.tpv.dao.InformeDAO;
import com.jhonatan.tfg.tpv.model.informe.HistorialVentaInforme;
import com.jhonatan.tfg.tpv.model.informe.ResumenProductoInforme;
import com.jhonatan.tfg.tpv.util.AccessManager;
import com.jhonatan.tfg.tpv.util.MessageUtils;
import com.jhonatan.tfg.tpv.util.MoneyUtils;
import com.jhonatan.tfg.tpv.util.NavigationManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Controlador de la vista de informes.
 *
 * Muestra métricas agregadas del sistema, tablas resumen,
 * historial de ventas y gráficos estadísticos.
 */
public class InformesController {

    @FXML
    private Label ingresosLabel;

    @FXML
    private Label comandasCerradasLabel;

    @FXML
    private Label productosVendidosLabel;

    @FXML
    private Label productoMasVendidoLabel;

    @FXML
    private Label ultimaActualizacionLabel;

    @FXML
    private Label mensajeLabel;

    @FXML
    private DatePicker fechaFiltroPicker;

    @FXML
    private TableView<ResumenProductoInforme> resumenProductosTableView;

    @FXML
    private TableColumn<ResumenProductoInforme, String> productoResumenColumn;

    @FXML
    private TableColumn<ResumenProductoInforme, Number> unidadesResumenColumn;

    @FXML
    private TableView<HistorialVentaInforme> historialVentasTableView;

    @FXML
    private TableColumn<HistorialVentaInforme, Number> mesaHistorialColumn;

    @FXML
    private TableColumn<HistorialVentaInforme, String> fechaHistorialColumn;

    @FXML
    private TableColumn<HistorialVentaInforme, String> totalHistorialColumn;

    @FXML
    private BarChart<String, Number> productosVendidosChart;

    @FXML
    private BarChart<String, Number> ingresosPorDiaChart;

    private final InformeDAO informeDAO = new InformeDAO();

    /**
     * Método de inicialización automático de JavaFX.
     *
     * Valida permisos de administrador, configura tablas
     * y carga los informes iniciales.
     */
    @FXML
    public void initialize() {
        if (!AccessManager.esAdmin()) {
            redirigirAlMenuPrincipal();
            return;
        }

        configurarTablas();
        configurarVista();
        cargarInformes();
    }

    /**
     * Configura elementos visuales iniciales de la vista.
     */
    private void configurarVista() {
        resumenProductosTableView.setPlaceholder(new Label("Sin datos."));
        historialVentasTableView.setPlaceholder(new Label("Sin historial disponible."));
    }

    /**
     * Configura las tablas auxiliares de informes.
     *
     * Estas tablas muestran modelos auxiliares preparados para la interfaz,
     * no entidades completas de la base de datos.
     */
    private void configurarTablas() {
        resumenProductosTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        resumenProductosTableView.setFixedCellSize(34);

        historialVentasTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historialVentasTableView.setFixedCellSize(34);

        productoResumenColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProducto())
        );

        unidadesResumenColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getUnidadesVendidas())
        );

        mesaHistorialColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getMesa())
        );

        fechaHistorialColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFecha())
        );

        totalHistorialColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTotal())
        );

        mesaHistorialColumn.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        fechaHistorialColumn.setMaxWidth(1f * Integer.MAX_VALUE * 55);
        totalHistorialColumn.setMaxWidth(1f * Integer.MAX_VALUE * 30);
    }

    /**
     * Carga las métricas, tablas y gráficos según el filtro seleccionado.
     */
    private void cargarInformes() {
        LocalDate fechaSeleccionada = fechaFiltroPicker.getValue();

        if (fechaSeleccionada == null) {
            cargarInformesGenerales();
            MessageUtils.showInfoAuto(mensajeLabel, "Informes cargados correctamente.", 2.5);
        } else {
            cargarInformesPorFecha(fechaSeleccionada);
            MessageUtils.showInfoAuto(
                    mensajeLabel,
                    "Filtro aplicado: " + fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".",
                    2.5
            );
        }

        actualizarFechaUltimaActualizacion();
    }

    /**
     * Carga informes sin filtro de fecha.
     */
    private void cargarInformesGenerales() {
        double ingresos = informeDAO.obtenerTotalIngresos();
        int comandasCerradas = informeDAO.contarComandasCerradas();
        int productosVendidos = informeDAO.obtenerTotalProductosVendidos();
        String productoMasVendido = informeDAO.obtenerProductoMasVendido();

        actualizarMetricas(ingresos, comandasCerradas, productosVendidos, productoMasVendido);

        resumenProductosTableView.setItems(
                FXCollections.observableArrayList(informeDAO.obtenerResumenProductosVendidosTabla())
        );

        historialVentasTableView.setItems(
                FXCollections.observableArrayList(informeDAO.obtenerHistorialVentasTabla())
        );

        cargarGraficoProductosVendidos();
        cargarGraficoIngresosPorDia();
    }

    /**
     * Carga informes filtrados por una fecha concreta.
     *
     * @param fechaSeleccionada fecha seleccionada en el DatePicker
     */
    private void cargarInformesPorFecha(LocalDate fechaSeleccionada) {
        String fecha = fechaSeleccionada.toString();

        double ingresos = informeDAO.obtenerTotalIngresosPorFecha(fecha);
        int comandasCerradas = informeDAO.contarComandasCerradasPorFecha(fecha);
        int productosVendidos = informeDAO.obtenerTotalProductosVendidosPorFecha(fecha);
        String productoMasVendido = informeDAO.obtenerProductoMasVendidoPorFecha(fecha);

        actualizarMetricas(ingresos, comandasCerradas, productosVendidos, productoMasVendido);

        resumenProductosTableView.setItems(
                FXCollections.observableArrayList(informeDAO.obtenerResumenProductosVendidosTablaPorFecha(fecha))
        );

        historialVentasTableView.setItems(
                FXCollections.observableArrayList(informeDAO.obtenerHistorialVentasTablaPorFecha(fecha))
        );

        cargarGraficoProductosVendidosPorFecha(fecha);
        cargarGraficoIngresosPorDiaPorFecha(fecha);
    }

    /**
     * Actualiza los indicadores principales de la vista.
     *
     * @param ingresos ingresos totales
     * @param comandasCerradas número de comandas cerradas
     * @param productosVendidos unidades vendidas
     * @param productoMasVendido producto más vendido
     */
    private void actualizarMetricas(double ingresos,
                                    int comandasCerradas,
                                    int productosVendidos,
                                    String productoMasVendido) {

        ingresosLabel.setText("Ingresos totales: " + MoneyUtils.formatearEuros(ingresos));
        comandasCerradasLabel.setText("Comandas cerradas: " + comandasCerradas);
        productosVendidosLabel.setText("Productos vendidos: " + productosVendidos);
        productoMasVendidoLabel.setText("Producto más vendido: " + productoMasVendido);
    }

    /**
     * Actualiza la fecha y hora de última actualización.
     */
    private void actualizarFechaUltimaActualizacion() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        ultimaActualizacionLabel.setText(
                "Última actualización: " + LocalDateTime.now().format(formatter)
        );
    }

    /**
     * Refresca los datos de informes.
     */
    @FXML
    private void handleRefrescar() {
        MessageUtils.clear(mensajeLabel);
        cargarInformes();
    }

    /**
     * Aplica el filtro por fecha.
     */
    @FXML
    private void handleAplicarFiltro() {
        MessageUtils.clear(mensajeLabel);
        cargarInformes();
    }

    /**
     * Limpia el filtro de fecha y recarga todos los informes.
     */
    @FXML
    private void handleLimpiarFiltro() {
        MessageUtils.clear(mensajeLabel);
        fechaFiltroPicker.setValue(null);
        cargarInformes();
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
     * Carga el gráfico de productos más vendidos sin filtro.
     */
    private void cargarGraficoProductosVendidos() {
        Map<String, Integer> datos = informeDAO.obtenerProductosMasVendidosParaGrafico();
        cargarGraficoProductos(datos);
    }

    /**
     * Carga el gráfico de productos más vendidos filtrado por fecha.
     *
     * @param fecha fecha en formato YYYY-MM-DD
     */
    private void cargarGraficoProductosVendidosPorFecha(String fecha) {
        Map<String, Integer> datos = informeDAO.obtenerProductosMasVendidosParaGraficoPorFecha(fecha);
        cargarGraficoProductos(datos);
    }

    /**
     * Carga datos en el gráfico de productos.
     *
     * @param datos mapa producto-unidades
     */
    private void cargarGraficoProductos(Map<String, Integer> datos) {
        productosVendidosChart.getData().clear();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();

        for (Map.Entry<String, Integer> entry : datos.entrySet()) {
            serie.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        productosVendidosChart.getData().add(serie);
    }

    /**
     * Carga el gráfico de ingresos agrupados por día.
     */
    private void cargarGraficoIngresosPorDia() {
        ingresosPorDiaChart.getData().clear();

        Map<String, Double> datos = informeDAO.obtenerIngresosPorDiaParaGrafico();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();

        for (Map.Entry<String, Double> entry : datos.entrySet()) {
            serie.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        ingresosPorDiaChart.getData().add(serie);
    }

    /**
     * Carga el gráfico de ingresos de una fecha concreta.
     *
     * @param fecha fecha en formato YYYY-MM-DD
     */
    private void cargarGraficoIngresosPorDiaPorFecha(String fecha) {
        ingresosPorDiaChart.getData().clear();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        double ingresos = informeDAO.obtenerTotalIngresosPorFecha(fecha);

        serie.getData().add(new XYChart.Data<>(fecha, ingresos));
        ingresosPorDiaChart.getData().add(serie);
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