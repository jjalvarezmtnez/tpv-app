package com.jhonatan.tfg.tpv.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase encargada de gestionar la conexión con la base de datos SQLite.
 *
 * Centraliza la creación de conexiones JDBC para evitar duplicar lógica
 * en los distintos DAOs del sistema.
 *
 * DECISIÓN DE DISEÑO:
 * Se utiliza SQLite por ser una base de datos ligera, embebida y sin necesidad
 * de instalación, ideal para aplicaciones de escritorio como este TPV.
 *
 * El archivo de base de datos se genera automáticamente en la raíz del proyecto.
 */
public class DatabaseConnection {

    /**
     * URL de conexión JDBC para SQLite.
     *
     * FORMATO:
     * jdbc:sqlite:<nombre_archivo>
     *
     * En este caso:
     * - Se crea (si no existe) el archivo "database.db"
     * - Se ubica en el directorio de ejecución del proyecto
     *
     * IMPORTANTE:
     * Si en el futuro se distribuye la aplicación, esta ruta debería
     * gestionarse dinámicamente (por ejemplo, en AppData o carpeta del usuario).
     */
    private static final String URL = "jdbc:sqlite:database.db";

    /**
     * Constructor privado para evitar instanciación.
     *
     * Esta clase actúa como utilitaria (solo métodos estáticos).
     */
    private DatabaseConnection() {
    }

    /**
     * Crea y devuelve una conexión activa con la base de datos.
     *
     * RESPONSABILIDAD:
     * - Abrir conexión JDBC
     * - Configurar parámetros necesarios de SQLite
     *
     * DECISIÓN IMPORTANTE:
     * Se activa manualmente el soporte de claves foráneas mediante:
     * PRAGMA foreign_keys = ON;
     *
     * ¿POR QUÉ?
     * SQLite no activa las foreign keys por defecto, lo que puede provocar:
     * - Inserciones inconsistentes
     * - Eliminaciones sin control (sin ON DELETE)
     *
     * ERROR COMÚN:
     * Olvidar este PRAGMA provoca que las relaciones entre tablas
     * no se respeten, aunque estén definidas en el esquema.
     *
     * @return Connection conexión activa a la base de datos
     * @throws SQLException si ocurre un error al abrir la conexión
     */
    public static Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
        }

        return connection;
    }
}