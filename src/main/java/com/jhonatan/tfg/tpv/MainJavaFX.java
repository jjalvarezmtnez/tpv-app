package com.jhonatan.tfg.tpv;

import com.jhonatan.tfg.tpv.database.DatabaseInitializer;
import com.jhonatan.tfg.tpv.database.DatabaseSeeder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación JavaFX.
 *
 * Responsabilidad:
 * - Inicializar entorno mínimo (BD + datos base)
 * - Cargar la primera vista (Login)
 * - Configurar ventana principal
 *
 * Diferencia clave respecto a Main (consola):
 * - Aquí NO se resetean datos → se preserva estado real de uso
 */
public class MainJavaFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        inicializarSistema();

        Scene scene = cargarEscenaInicial();

        configurarVentana(stage, scene);

        stage.show();
    }

    /**
     * Inicializa la base de datos y datos base.
     *
     * Por qué:
     * - Garantiza que la app siempre arranca en un estado válido
     * - Evita errores de "tabla no existe"
     *
     * Decisión importante:
     * - NO se usa reset → no se pierden datos del usuario
     */
    private void inicializarSistema() {
        DatabaseInitializer.initializeDatabase();
        DatabaseSeeder.seedDatabase();
    }

    /**
     * Carga la escena inicial (Login).
     *
     * Por qué:
     * - El login es el punto de entrada del sistema
     * - Centraliza autenticación antes de acceder al resto
     */
    private Scene cargarEscenaInicial() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/LoginView.fxml")
        );

        Scene scene = new Scene(loader.load());

        scene.getStylesheets().add(
                getClass().getResource("/css/app.css").toExternalForm()
        );

        return scene;
    }

    /**
     * Configura el tamaño y posición de la ventana.
     *
     * Modelo mental:
     * - Se adapta dinámicamente a la resolución del usuario
     * - Evita hardcodear tamaños fijos (mala práctica)
     *
     * Qué hace:
     * - Tamaño mínimo → evita UI rota
     * - Ajuste a pantalla → experiencia consistente
     * - Maximizado → app tipo escritorio profesional
     */
    private void configurarVentana(Stage stage, Scene scene) {
        stage.setTitle("TPV - Login");

        stage.setMinWidth(1000);
        stage.setMinHeight(700);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        stage.setX(bounds.getMinX() + 6);
        stage.setY(bounds.getMinY() + 5);
        stage.setWidth(bounds.getWidth() - 12);
        stage.setHeight(bounds.getHeight() - 10);

        stage.setScene(scene);

        stage.setMaximized(true);
    }

    /**
     * Método de arranque estándar de JavaFX.
     */
    public static void main(String[] args) {
        launch();
    }
}