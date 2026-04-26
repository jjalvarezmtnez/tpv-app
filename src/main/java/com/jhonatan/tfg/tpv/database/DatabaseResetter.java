package com.jhonatan.tfg.tpv.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase encargada de resetear los datos de la base de datos.
 *
 * PROPÓSITO:
 * Dejar la base de datos en un estado limpio y reproducible durante el desarrollo,
 * facilitando la repetición de pruebas manuales.
 *
 * IMPORTANTE:
 * Esta clase NO debe utilizarse en entorno de producción, ya que elimina
 * completamente los datos del sistema.
 */
public final class DatabaseResetter {

    /**
     * Constructor privado para evitar instanciación.
     */
    private DatabaseResetter() {
    }

    /**
     * Elimina los datos de todas las tablas principales y reinicia
     * los contadores AUTOINCREMENT de SQLite.
     *
     * PROCESO:
     * 1. Se desactivan temporalmente las claves foráneas
     * 2. Se eliminan los datos respetando dependencias
     * 3. Se reinician los contadores internos de SQLite
     * 4. Se reactivan las claves foráneas
     *
     * ERROR COMÚN:
     * Intentar borrar tablas con relaciones activas sin desactivar
     * foreign keys provoca errores de integridad.
     */
    public static void resetDatabase() {
        try (Connection connection = DatabaseConnection.connect();
             Statement statement = connection.createStatement()) {

            /*
             * Se usa Statement porque:
             * - Son sentencias fijas controladas por el desarrollador
             * - No existen parámetros externos (sin riesgo de SQL Injection)
             */

            // 1. Desactivar restricciones de integridad referencial temporalmente
            statement.execute("PRAGMA foreign_keys = OFF;");

            // 2. Eliminación de datos en orden dependiente (hijos → padres)
            statement.executeUpdate("DELETE FROM lineas_comanda;");
            statement.executeUpdate("DELETE FROM comandas;");
            statement.executeUpdate("DELETE FROM productos;");
            statement.executeUpdate("DELETE FROM categorias;");
            statement.executeUpdate("DELETE FROM mesas;");
            statement.executeUpdate("DELETE FROM usuarios;");

            // 3. Reinicio de contadores AUTOINCREMENT (SQLite)
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='lineas_comanda';");
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='comandas';");
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='productos';");
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='categorias';");
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='mesas';");
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='usuarios';");

            // 4. Reactivar restricciones de integridad
            statement.execute("PRAGMA foreign_keys = ON;");

            System.out.println("Base de datos reseteada correctamente para pruebas.");

        } catch (SQLException e) {
            System.err.println("Error al resetear la base de datos: " + e.getMessage());
        }
    }
}