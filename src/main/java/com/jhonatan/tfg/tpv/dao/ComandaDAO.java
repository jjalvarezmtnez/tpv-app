package com.jhonatan.tfg.tpv.dao;

import com.jhonatan.tfg.tpv.database.DatabaseConnection;
import com.jhonatan.tfg.tpv.model.Comanda;
import com.jhonatan.tfg.tpv.model.enums.EstadoComanda;
import com.jhonatan.tfg.tpv.model.enums.EstadoMesa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de gestionar las operaciones de acceso a datos
 * relacionadas con la entidad Comanda.
 *
 * Además de operaciones CRUD, contiene operaciones transaccionales
 * importantes, como el cierre de una comanda y la actualización del stock.
 */
public class ComandaDAO {

    /**
     * Crea una nueva comanda y marca la mesa asociada como OCUPADA.
     *
     * @param comanda objeto Comanda a registrar
     * @return true si la operación se completa correctamente, false en caso contrario
     */
    public boolean crearComanda(Comanda comanda) {
        final String sql = """
                INSERT INTO comandas (id_mesa, id_usuario, fecha_apertura, estado)
                VALUES (?, ?, ?, ?)
                """;

        MesaDAO mesaDAO = new MesaDAO();

        if (!mesaDAO.mesaEstaLibre(comanda.getIdMesa())) {
            System.err.println("No se puede crear la comanda: la mesa no está libre.");
            return false;
        }

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, comanda.getIdMesa());
            preparedStatement.setInt(2, comanda.getIdUsuario());
            preparedStatement.setString(3, comanda.getFechaApertura());
            preparedStatement.setString(4, comanda.getEstado().name());

            boolean creada = preparedStatement.executeUpdate() > 0;

            if (creada) {
                mesaDAO.actualizarEstadoMesa(comanda.getIdMesa(), EstadoMesa.OCUPADA);
            }

            return creada;

        } catch (SQLException e) {
            System.err.println("Error al crear comanda: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca una comanda por su identificador.
     *
     * @param idComanda identificador de la comanda
     * @return objeto Comanda si existe, null en caso contrario
     */
    public Comanda buscarPorId(int idComanda) {
        final String sql = """
                SELECT id_comanda, id_mesa, id_usuario, fecha_apertura, estado
                FROM comandas
                WHERE id_comanda = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, idComanda);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearComanda(resultSet);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar comanda por ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Busca la comanda abierta asociada a una mesa.
     *
     * @param idMesa identificador de la mesa
     * @return comanda abierta si existe, null en caso contrario
     */
    public Comanda buscarComandaAbiertaPorMesa(int idMesa) {
        final String sql = """
                SELECT id_comanda, id_mesa, id_usuario, fecha_apertura, estado
                FROM comandas
                WHERE id_mesa = ? AND estado = 'ABIERTA'
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, idMesa);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearComanda(resultSet);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar comanda abierta por mesa: " + e.getMessage());
        }

        return null;
    }

    /**
     * Obtiene todas las comandas abiertas.
     *
     * @return lista de comandas abiertas ordenadas por fecha de apertura
     */
    public List<Comanda> obtenerComandasAbiertas() {
        final String sql = """
                SELECT id_comanda, id_mesa, id_usuario, fecha_apertura, estado
                FROM comandas
                WHERE estado = 'ABIERTA'
                ORDER BY fecha_apertura
                """;

        List<Comanda> comandas = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                comandas.add(mapearComanda(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener comandas abiertas: " + e.getMessage());
        }

        return comandas;
    }

    /**
     * Comprueba si existe una comanda abierta para una mesa concreta.
     *
     * @param idMesa identificador de la mesa
     * @return true si existe una comanda abierta, false en caso contrario
     */
    public boolean existeComandaAbiertaParaMesa(int idMesa) {
        final String sql = """
                SELECT 1
                FROM comandas
                WHERE id_mesa = ? AND estado = 'ABIERTA'
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, idMesa);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.err.println("Error al comprobar comanda abierta para mesa: " + e.getMessage());
        }

        return false;
    }

    /**
     * Cierra una comanda, descuenta el stock de sus productos
     * y libera la mesa asociada.
     *
     * Esta operación se ejecuta dentro de una transacción porque afecta
     * a varias tablas. Si una parte falla, se revierte toda la operación.
     *
     * @param idComanda identificador de la comanda
     * @return true si la operación se realiza correctamente, false en caso contrario
     */
    public boolean cerrarComanda(int idComanda) {
        final String sqlCerrarComanda = """
                UPDATE comandas
                SET estado = 'CERRADA'
                WHERE id_comanda = ? AND estado = 'ABIERTA'
                """;

        final String sqlObtenerLineas = """
                SELECT id_producto, cantidad
                FROM lineas_comanda
                WHERE id_comanda = ?
                """;

        final String sqlObtenerProducto = """
                SELECT stock
                FROM productos
                WHERE id_producto = ?
                """;

        final String sqlActualizarStock = """
                UPDATE productos
                SET stock = ?
                WHERE id_producto = ?
                """;

        final String sqlLiberarMesa = """
                UPDATE mesas
                SET estado = 'LIBRE'
                WHERE id_mesa = ?
                """;

        Comanda comanda = buscarPorId(idComanda);

        if (comanda == null) {
            System.err.println("No se puede cerrar la comanda: no existe.");
            return false;
        }

        if (comanda.getEstado() != EstadoComanda.ABIERTA) {
            System.err.println("No se puede cerrar la comanda: no está abierta.");
            return false;
        }

        try (Connection connection = DatabaseConnection.connect()) {
            connection.setAutoCommit(false);

            try (
                    PreparedStatement psLineas = connection.prepareStatement(sqlObtenerLineas);
                    PreparedStatement psObtenerProducto = connection.prepareStatement(sqlObtenerProducto);
                    PreparedStatement psActualizarStock = connection.prepareStatement(sqlActualizarStock);
                    PreparedStatement psCerrarComanda = connection.prepareStatement(sqlCerrarComanda);
                    PreparedStatement psLiberarMesa = connection.prepareStatement(sqlLiberarMesa)
            ) {
                psLineas.setInt(1, idComanda);

                boolean tieneLineas = false;

                try (ResultSet rsLineas = psLineas.executeQuery()) {
                    while (rsLineas.next()) {
                        tieneLineas = true;

                        int idProducto = rsLineas.getInt("id_producto");
                        int cantidadVendida = rsLineas.getInt("cantidad");

                        actualizarStockProducto(
                                psObtenerProducto,
                                psActualizarStock,
                                idProducto,
                                cantidadVendida
                        );
                    }
                }

                if (!tieneLineas) {
                    throw new SQLException("No se puede cerrar una comanda sin líneas.");
                }

                psCerrarComanda.setInt(1, idComanda);
                int filasActualizadas = psCerrarComanda.executeUpdate();

                if (filasActualizadas == 0) {
                    throw new SQLException("No se pudo cerrar la comanda.");
                }

                psLiberarMesa.setInt(1, comanda.getIdMesa());
                psLiberarMesa.executeUpdate();

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                System.err.println("Error al cerrar la comanda: " + e.getMessage());
                return false;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Error general en la operación de cierre de comanda: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina una comanda por su identificador.
     *
     * Este método se usa para anular comandas abiertas sin líneas.
     *
     * @param idComanda identificador de la comanda
     * @return true si la eliminación se realiza correctamente, false en caso contrario
     */
    public boolean eliminarComanda(int idComanda) {
        final String sql = """
                DELETE FROM comandas
                WHERE id_comanda = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, idComanda);

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar la comanda: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza el stock de un producto restando la cantidad vendida.
     *
     * @param psObtenerProducto PreparedStatement para consultar stock actual
     * @param psActualizarStock PreparedStatement para actualizar stock
     * @param idProducto identificador del producto
     * @param cantidadVendida cantidad vendida en la comanda
     * @throws SQLException si el producto no existe o el stock resultante es negativo
     */
    private void actualizarStockProducto(PreparedStatement psObtenerProducto,
                                         PreparedStatement psActualizarStock,
                                         int idProducto,
                                         int cantidadVendida) throws SQLException {

        psObtenerProducto.setInt(1, idProducto);

        try (ResultSet rsProducto = psObtenerProducto.executeQuery()) {
            if (!rsProducto.next()) {
                throw new SQLException("No se encontró el producto con ID " + idProducto);
            }

            int stockActual = rsProducto.getInt("stock");
            int nuevoStock = stockActual - cantidadVendida;

            if (nuevoStock < 0) {
                throw new SQLException("Stock insuficiente para el producto con ID " + idProducto);
            }

            psActualizarStock.setInt(1, nuevoStock);
            psActualizarStock.setInt(2, idProducto);
            psActualizarStock.executeUpdate();
        }
    }

    /**
     * Convierte una fila del ResultSet en un objeto Comanda.
     *
     * @param resultSet resultado SQL posicionado en una fila válida
     * @return objeto Comanda mapeado
     * @throws SQLException si ocurre un error al leer los datos
     */
    private Comanda mapearComanda(ResultSet resultSet) throws SQLException {
        Comanda comanda = new Comanda();
        comanda.setIdComanda(resultSet.getInt("id_comanda"));
        comanda.setIdMesa(resultSet.getInt("id_mesa"));
        comanda.setIdUsuario(resultSet.getInt("id_usuario"));
        comanda.setFechaApertura(resultSet.getString("fecha_apertura"));
        comanda.setEstado(EstadoComanda.valueOf(resultSet.getString("estado")));
        return comanda;
    }
}