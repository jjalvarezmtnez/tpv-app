package com.jhonatan.tfg.tpv.dao;

import com.jhonatan.tfg.tpv.database.DatabaseConnection;
import com.jhonatan.tfg.tpv.model.LineaComanda;
import com.jhonatan.tfg.tpv.util.MoneyUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de gestionar el acceso a datos de las líneas de comanda.
 *
 * Representa el nivel más granular de una venta:
 * cada línea corresponde a un producto dentro de una comanda.
 *
 * Responsabilidades principales:
 * - CRUD de líneas
 * - Cálculo de totales
 * - Búsquedas específicas (por comanda, producto, ID)
 */
public class LineaComandaDAO {

    // =========================
    // CREATE
    // =========================

    /**
     * Inserta una nueva línea de comanda en la base de datos.
     *
     * @param linea objeto LineaComanda a registrar
     * @return true si la inserción se realiza correctamente, false en caso contrario
     */
    public boolean crearLineaComanda(LineaComanda linea) {
        final String sql = """
                INSERT INTO lineas_comanda (id_comanda, id_producto, cantidad, precio_unitario)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, linea.getIdComanda());
            ps.setInt(2, linea.getIdProducto());
            ps.setInt(3, linea.getCantidad());
            ps.setDouble(4, linea.getPrecioUnitario());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear línea de comanda: " + e.getMessage());
            return false;
        }
    }

    // =========================
    // READ
    // =========================

    /**
     * Obtiene todas las líneas asociadas a una comanda.
     *
     * @param idComanda identificador de la comanda
     * @return lista de líneas (vacía si no hay)
     */
    public List<LineaComanda> obtenerLineasPorComanda(int idComanda) {
        final String sql = """
                SELECT id_linea, id_comanda, id_producto, cantidad, precio_unitario
                FROM lineas_comanda
                WHERE id_comanda = ?
                ORDER BY id_linea
                """;

        List<LineaComanda> lineas = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idComanda);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lineas.add(mapearLinea(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener líneas de la comanda: " + e.getMessage());
        }

        return lineas;
    }

    /**
     * Busca una línea de comanda por su identificador.
     *
     * @param idLinea identificador de la línea
     * @return objeto LineaComanda si existe, null en caso contrario
     */
    public LineaComanda buscarLineaPorId(int idLinea) {
        final String sql = """
                SELECT id_linea, id_comanda, id_producto, cantidad, precio_unitario
                FROM lineas_comanda
                WHERE id_linea = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idLinea);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearLinea(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar línea por ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Busca una línea dentro de una comanda por producto.
     *
     * Se utiliza para evitar duplicados y acumular cantidades.
     *
     * @param idComanda identificador de la comanda
     * @param idProducto identificador del producto
     * @return línea existente o null si no existe
     */
    public LineaComanda buscarLineaPorComandaYProducto(int idComanda, int idProducto) {
        final String sql = """
                SELECT id_linea, id_comanda, id_producto, cantidad, precio_unitario
                FROM lineas_comanda
                WHERE id_comanda = ? AND id_producto = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idComanda);
            ps.setInt(2, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearLinea(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar línea por comanda y producto: " + e.getMessage());
        }

        return null;
    }

    // =========================
    // UPDATE
    // =========================

    /**
     * Actualiza la cantidad de una línea existente.
     *
     * @param idLinea identificador de la línea
     * @param nuevaCantidad nueva cantidad
     * @return true si la actualización se realiza correctamente
     */
    public boolean actualizarCantidadLinea(int idLinea, int nuevaCantidad) {
        final String sql = """
                UPDATE lineas_comanda
                SET cantidad = ?
                WHERE id_linea = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, nuevaCantidad);
            ps.setInt(2, idLinea);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar cantidad de línea: " + e.getMessage());
            return false;
        }
    }

    // =========================
    // DELETE
    // =========================

    /**
     * Elimina una línea de comanda.
     *
     * @param idLinea identificador de la línea
     * @return true si se elimina correctamente
     */
    public boolean eliminarLinea(int idLinea) {
        final String sql = """
                DELETE FROM lineas_comanda
                WHERE id_linea = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idLinea);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar la línea de comanda: " + e.getMessage());
            return false;
        }
    }

    // =========================
    // CÁLCULOS
    // =========================

    /**
     * Calcula el total de una comanda directamente en base de datos.
     *
     * Se utiliza SUM(cantidad * precio_unitario) para evitar traer datos a memoria.
     *
     * @param idComanda identificador de la comanda
     * @return total redondeado a 2 decimales
     */
    public double calcularTotalComanda(int idComanda) {
        final String sql = """
                SELECT SUM(cantidad * precio_unitario) AS total
                FROM lineas_comanda
                WHERE id_comanda = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idComanda);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return MoneyUtils.redondear(rs.getDouble("total"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al calcular el total de la comanda: " + e.getMessage());
        }

        return 0.0;
    }

    // =========================
    // MÉTODOS PRIVADOS
    // =========================

    /**
     * Mapea un ResultSet a objeto LineaComanda.
     *
     * Centralizar este método evita duplicación de código
     * y posibles inconsistencias en el mapeo.
     *
     * @param rs resultado de la consulta
     * @return objeto LineaComanda
     * @throws SQLException si ocurre un error de lectura
     */
    private LineaComanda mapearLinea(ResultSet rs) throws SQLException {
        LineaComanda linea = new LineaComanda();
        linea.setIdLinea(rs.getInt("id_linea"));
        linea.setIdComanda(rs.getInt("id_comanda"));
        linea.setIdProducto(rs.getInt("id_producto"));
        linea.setCantidad(rs.getInt("cantidad"));
        linea.setPrecioUnitario(rs.getDouble("precio_unitario"));
        return linea;
    }
}