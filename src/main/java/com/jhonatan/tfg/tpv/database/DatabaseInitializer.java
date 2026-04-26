package com.jhonatan.tfg.tpv.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase encargada de inicializar la estructura de la base de datos.
 *
 * Su responsabilidad es crear las tablas necesarias si todavía no existen,
 * permitiendo que la aplicación pueda arrancar desde una base de datos vacía.
 */
public final class DatabaseInitializer {

    /**
     * Constructor privado para evitar instanciación.
     */
    private DatabaseInitializer() {
    }

    /**
     * Inicializa la base de datos creando las tablas principales del sistema.
     *
     * Las tablas se crean en un orden lógico para respetar las dependencias
     * entre claves primarias y claves foráneas.
     */
    public static void initializeDatabase() {
        final String createUsuariosTable = """
                CREATE TABLE IF NOT EXISTS usuarios (
                    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    nombre TEXT NOT NULL,
                    password_hash TEXT NOT NULL,
                    rol TEXT NOT NULL CHECK (rol IN ('ADMIN', 'CAMARERO')),
                    activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1))
                );
                """;

        final String createCategoriasTable = """
                CREATE TABLE IF NOT EXISTS categorias (
                    id_categoria INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL UNIQUE
                );
                """;

        final String createProductosTable = """
                CREATE TABLE IF NOT EXISTS productos (
                    id_producto INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    precio REAL NOT NULL CHECK (precio >= 0),
                    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
                    id_categoria INTEGER NOT NULL,
                    FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
                );
                """;

        final String createMesasTable = """
                CREATE TABLE IF NOT EXISTS mesas (
                    id_mesa INTEGER PRIMARY KEY AUTOINCREMENT,
                    numero INTEGER NOT NULL UNIQUE,
                    estado TEXT NOT NULL CHECK (estado IN ('LIBRE', 'OCUPADA'))
                );
                """;

        final String createComandasTable = """
                CREATE TABLE IF NOT EXISTS comandas (
                    id_comanda INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_mesa INTEGER NOT NULL,
                    id_usuario INTEGER NOT NULL,
                    fecha_apertura TEXT NOT NULL,
                    estado TEXT NOT NULL CHECK (estado IN ('ABIERTA', 'CERRADA')),
                    FOREIGN KEY (id_mesa) REFERENCES mesas(id_mesa),
                    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
                );
                """;

        final String createLineasComandaTable = """
                CREATE TABLE IF NOT EXISTS lineas_comanda (
                    id_linea INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_comanda INTEGER NOT NULL,
                    id_producto INTEGER NOT NULL,
                    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
                    precio_unitario REAL NOT NULL CHECK (precio_unitario >= 0),
                    FOREIGN KEY (id_comanda) REFERENCES comandas(id_comanda),
                    FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
                );
                """;

        try (Connection connection = DatabaseConnection.connect();
             Statement statement = connection.createStatement()) {

            /*
             * Se utiliza Statement porque son sentencias DDL fijas,
             * sin parámetros externos introducidos por el usuario.
             *
             * PreparedStatement se reserva para operaciones DML
             * con datos dinámicos: INSERT, UPDATE, DELETE o SELECT.
             */
            statement.execute(createUsuariosTable);
            statement.execute(createCategoriasTable);
            statement.execute(createProductosTable);
            statement.execute(createMesasTable);
            statement.execute(createComandasTable);
            statement.execute(createLineasComandaTable);

            System.out.println("Base de datos inicializada correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }
}