package com.jhonatan.tfg.tpv.util;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Clase utilitaria para gestionar la navegación entre vistas JavaFX.
 *
 * Centraliza el cambio de escenas, la aplicación de estilos globales
 * y la configuración inicial de la ventana principal.
 */
public final class NavigationManager {

    private static final String GLOBAL_CSS = "/css/app.css";
    private static final double MIN_WIDTH = 1000;
    private static final double MIN_HEIGHT = 700;
    private static final double MARGEN_HORIZONTAL = 12;
    private static final double MARGEN_VERTICAL = 10;
    private static final double DURACION_FADE_MS = 120;

    /**
     * Constructor privado para evitar instanciación.
     */
    private NavigationManager() {
    }

    /**
     * Cambia la vista actual cargando un archivo FXML.
     *
     * @param nodoOrigen nodo desde el que se obtiene la ventana actual
     * @param rutaFXML ruta del archivo FXML a cargar
     * @param titulo nuevo título de la ventana
     * @throws IOException si ocurre un error al cargar el FXML
     */
    public static void cambiarVista(Node nodoOrigen, String rutaFXML, String titulo) throws IOException {
        cambiarVista(nodoOrigen, rutaFXML, titulo, null);
    }

    /**
     * Cambia la vista actual cargando un archivo FXML y permite configurar
     * el controlador de la nueva vista antes de mostrarla.
     *
     * Este método se utiliza, por ejemplo, cuando una vista necesita recibir
     * información desde la pantalla anterior, como una mesa seleccionada.
     *
     * @param nodoOrigen nodo desde el que se obtiene la ventana actual
     * @param rutaFXML ruta del archivo FXML a cargar
     * @param titulo nuevo título de la ventana
     * @param configuradorControlador acción opcional para configurar el controlador cargado
     * @param <T> tipo del controlador de la vista cargada
     * @throws IOException si ocurre un error al cargar el FXML
     */
    public static <T> void cambiarVista(Node nodoOrigen,
                                        String rutaFXML,
                                        String titulo,
                                        Consumer<T> configuradorControlador) throws IOException {

        FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(rutaFXML));
        Parent root = loader.load();

        configurarControlador(loader, configuradorControlador);

        Scene scene = new Scene(root);
        aplicarEstilos(scene);

        Stage stage = obtenerStage(nodoOrigen);

        stage.setTitle(titulo);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);

        root.setOpacity(0);

        prepararTamanoCasiMaximizado(stage);

        stage.setScene(scene);
        stage.show();

        Platform.runLater(() -> {
            stage.setMaximized(true);
            aplicarTransicionEntrada(root);
        });
    }

    /**
     * Aplica la hoja de estilos global a la escena.
     *
     * @param scene escena a configurar
     */
    private static void aplicarEstilos(Scene scene) {
        scene.getStylesheets().add(
                NavigationManager.class.getResource(GLOBAL_CSS).toExternalForm()
        );
    }

    /**
     * Obtiene la ventana principal a partir del nodo que origina la navegación.
     *
     * @param nodoOrigen nodo perteneciente a la escena actual
     * @return ventana principal de la aplicación
     */
    private static Stage obtenerStage(Node nodoOrigen) {
        return (Stage) nodoOrigen.getScene().getWindow();
    }

    /**
     * Configura el controlador cargado si se ha proporcionado una acción externa.
     *
     * @param loader cargador FXML utilizado para obtener el controlador
     * @param configuradorControlador acción opcional de configuración
     * @param <T> tipo del controlador
     */
    private static <T> void configurarControlador(FXMLLoader loader, Consumer<T> configuradorControlador) {
        if (configuradorControlador != null) {
            T controller = loader.getController();
            configuradorControlador.accept(controller);
        }
    }

    /**
     * Ajusta la ventana a un tamaño muy próximo al maximizado.
     *
     * Esto reduce el salto visual antes de aplicar el maximizado nativo
     * de JavaFX.
     *
     * @param stage ventana principal
     */
    private static void prepararTamanoCasiMaximizado(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        stage.setMaximized(false);
        stage.setX(bounds.getMinX() + MARGEN_HORIZONTAL / 2);
        stage.setY(bounds.getMinY() + MARGEN_VERTICAL / 2);
        stage.setWidth(bounds.getWidth() - MARGEN_HORIZONTAL);
        stage.setHeight(bounds.getHeight() - MARGEN_VERTICAL);
    }

    /**
     * Aplica una transición de entrada suave a la nueva vista.
     *
     * @param root nodo raíz de la vista cargada
     */
    private static void aplicarTransicionEntrada(Parent root) {
        FadeTransition fade = new FadeTransition(Duration.millis(DURACION_FADE_MS), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}