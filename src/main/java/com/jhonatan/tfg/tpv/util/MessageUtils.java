package com.jhonatan.tfg.tpv.util;

import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Clase utilitaria para mostrar mensajes visuales en la interfaz.
 *
 * Centraliza la lógica de presentación de mensajes en etiquetas JavaFX,
 * aplicando estilos consistentes y evitando duplicación en controladores.
 */
public final class MessageUtils {

    /**
     * Constructor privado para evitar instanciación.
     */
    private MessageUtils() {
    }

    // =========================
    // MÉTODOS PÚBLICOS (MANUALES)
    // =========================

    /**
     * Muestra un mensaje de éxito.
     *
     * @param label etiqueta donde se mostrará el mensaje
     * @param mensaje texto a mostrar
     */
    public static void showSuccess(Label label, String mensaje) {
        aplicarEstilo(label, "message-success", mensaje);
    }

    /**
     * Muestra un mensaje de error.
     *
     * @param label etiqueta donde se mostrará el mensaje
     * @param mensaje texto a mostrar
     */
    public static void showError(Label label, String mensaje) {
        aplicarEstilo(label, "message-error", mensaje);
    }

    /**
     * Muestra un mensaje de advertencia.
     *
     * @param label etiqueta donde se mostrará el mensaje
     * @param mensaje texto a mostrar
     */
    public static void showWarning(Label label, String mensaje) {
        aplicarEstilo(label, "message-warning", mensaje);
    }

    /**
     * Muestra un mensaje informativo.
     *
     * @param label etiqueta donde se mostrará el mensaje
     * @param mensaje texto a mostrar
     */
    public static void showInfo(Label label, String mensaje) {
        aplicarEstilo(label, "message-info", mensaje);
    }

    // =========================
    // MÉTODOS PÚBLICOS (AUTO CLEAR)
    // =========================

    /**
     * Muestra un mensaje de éxito y lo elimina automáticamente.
     *
     * @param label etiqueta destino
     * @param mensaje texto a mostrar
     * @param segundos tiempo antes de limpiar
     */
    public static void showSuccessAuto(Label label, String mensaje, double segundos) {
        showSuccess(label, mensaje);
        autoClear(label, segundos);
    }

    /**
     * Muestra un mensaje de error y lo elimina automáticamente.
     *
     * @param label etiqueta destino
     * @param mensaje texto a mostrar
     * @param segundos tiempo antes de limpiar
     */
    public static void showErrorAuto(Label label, String mensaje, double segundos) {
        showError(label, mensaje);
        autoClear(label, segundos);
    }

    /**
     * Muestra un mensaje de advertencia y lo elimina automáticamente.
     *
     * @param label etiqueta destino
     * @param mensaje texto a mostrar
     * @param segundos tiempo antes de limpiar
     */
    public static void showWarningAuto(Label label, String mensaje, double segundos) {
        showWarning(label, mensaje);
        autoClear(label, segundos);
    }

    /**
     * Muestra un mensaje informativo y lo elimina automáticamente.
     *
     * @param label etiqueta destino
     * @param mensaje texto a mostrar
     * @param segundos tiempo antes de limpiar
     */
    public static void showInfoAuto(Label label, String mensaje, double segundos) {
        showInfo(label, mensaje);
        autoClear(label, segundos);
    }

    /**
     * Limpia el contenido y estilos de la etiqueta.
     *
     * @param label etiqueta a limpiar
     */
    public static void clear(Label label) {
        label.setText("");
        eliminarEstilos(label);
    }

    // =========================
    // MÉTODOS PRIVADOS
    // =========================

    /**
     * Aplica estilo visual y texto a la etiqueta.
     *
     * Este método evita duplicación en los métodos públicos.
     *
     * @param label etiqueta destino
     * @param estilo clase CSS a aplicar
     * @param mensaje texto a mostrar
     */
    private static void aplicarEstilo(Label label, String estilo, String mensaje) {
        eliminarEstilos(label);
        label.getStyleClass().add(estilo);
        label.setText(mensaje);
    }

    /**
     * Elimina todas las clases de estilo de mensaje.
     *
     * @param label etiqueta a limpiar
     */
    private static void eliminarEstilos(Label label) {
        label.getStyleClass().removeAll(
                "message-success",
                "message-error",
                "message-warning",
                "message-info"
        );
    }

    /**
     * Programa la limpieza automática del mensaje.
     *
     * @param label etiqueta que contiene el mensaje
     * @param segundos tiempo de espera antes de limpiar
     */
    private static void autoClear(Label label, double segundos) {
        PauseTransition pause = new PauseTransition(Duration.seconds(segundos));
        pause.setOnFinished(event -> clear(label));
        pause.play();
    }
}