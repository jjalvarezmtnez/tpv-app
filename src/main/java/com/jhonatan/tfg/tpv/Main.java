package com.jhonatan.tfg.tpv;

import com.jhonatan.tfg.tpv.database.DatabaseInitializer;
import com.jhonatan.tfg.tpv.database.DatabaseResetter;
import com.jhonatan.tfg.tpv.database.DatabaseSeeder;

/**
 * Punto de entrada del backend en modo consola.
 *
 * Se utiliza principalmente durante desarrollo para validar
 * el comportamiento del sistema sin necesidad de interfaz gráfica.
 *
 * Flujo que ejecuta:
 *
 * 1. Inicializa la base de datos → crea tablas si no existen
 * 2. Resetea datos → elimina contenido previo (entorno controlado)
 * 3. Inserta datos semilla → garantiza datos consistentes para pruebas
 * 4. Ejecuta pruebas → valida funcionamiento del sistema
 *
 * ⚠️ Importante:
 * Este flujo NO es válido para producción porque elimina datos.
 */
public class Main {

    public static void main(String[] args) {

        inicializarBaseDeDatos();

        prepararEntornoDePruebas();

        ejecutarPruebasBackend();
    }

    /**
     * Crea la estructura de base de datos si aún no existe.
     *
     * Por qué:
     * - Evita errores por tablas inexistentes
     * - Permite ejecutar el sistema en cualquier máquina sin instalación previa
     */
    private static void inicializarBaseDeDatos() {
        DatabaseInitializer.initializeDatabase();
    }

    /**
     * Prepara un entorno limpio y controlado para pruebas.
     *
     * Incluye:
     * - Borrado de datos previos
     * - Inserción de datos base
     *
     * Por qué:
     * - Garantiza reproducibilidad
     * - Evita estados inconsistentes
     *
     * Cuándo NO usar:
     * - En producción
     * - Cuando quieras persistir datos reales
     */
    private static void prepararEntornoDePruebas() {
        DatabaseResetter.resetDatabase();
        DatabaseSeeder.seedDatabase();
    }

    /**
     * Ejecuta pruebas manuales del backend.
     *
     * Por qué:
     * - Verifica lógica de negocio sin UI
     * - Detecta errores antes de usar JavaFX
     */
    private static void ejecutarPruebasBackend() {
        ConsoleTestRunner.runAllTests();
    }
}