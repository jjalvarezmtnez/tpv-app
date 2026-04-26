package com.jhonatan.tfg.tpv.dao;

import com.jhonatan.tfg.tpv.database.DatabaseConnection;
import com.jhonatan.tfg.tpv.model.Mesa;
import com.jhonatan.tfg.tpv.model.enums.EstadoMesa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de gestionar el acceso a datos de la entidad Mesa.
 *
 * Responsabilidades:
 * - CRUD de mesas
 * - Validaciones de existencia y duplicados
 * - Consultas de estado (libre/ocupada)
 */
public class MesaDAO {

    // =========================
    // CREATE
    // =========================

    /**
     * Inserta una nueva mesa en la base de datos.
     *
     * @param mesa objeto Mesa a registrar
     * @return true si la inserción se realiza correctamente, false en caso contrario
     */
    public boolean crearMesa(Mesa mesa) {
        final String sql = """
                INSERT INTO mesas (numero, estado)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, mesa.getNumero());
            ps.setString(2, mesa.getEstado().name());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear mesa: " + e.getMessage());
            return false;
        }
    }

    // =========================
    // READ
    // =========================

    /**
     * Busca una mesa por su identificador.
     *
     * @param idMesa identificador
     * @return Mesa o null
     */
    public Mesa buscarPorId(int idMesa) {
        final String sql = """
                SELECT id_mesa, numero, estado
                FROM mesas
                WHERE id_mesa = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idMesa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearMesa(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar mesa por ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Busca una mesa por su número físico.
     *
     * @param numero número visible de la mesa
     * @return Mesa o null
     */
    public Mesa buscarPorNumero(int numero) {
        final String sql = """
                SELECT id_mesa, numero, estado
                FROM mesas
                WHERE numero = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, numero);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearMesa(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar mesa por número: " + e.getMessage());
        }

        return null;
    }

    /**
     * Obtiene todas las mesas del sistema.
     *
     * @return lista de mesas
     */
    public List<Mesa> obtenerTodasLasMesas() {
        final String sql = """
                SELECT id_mesa, numero, estado
                FROM mesas
                ORDER BY numero
                """;

        List<Mesa> mesas = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                mesas.add(mapearMesa(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener mesas: " + e.getMessage());
        }

        return mesas;
    }

    /**
     * Comprueba si existe una mesa con el número indicado.
     *
     * @param numero número de mesa
     * @return true si existe
     */
    public boolean existeMesa(int numero) {
        return buscarPorNumero(numero) != null;
    }

    /**
     * Comprueba si existe otra mesa con el mismo número (excluyendo una).
     *
     * @param numero número a comprobar
     * @param idMesaExcluir id a excluir
     * @return true si existe duplicado
     */
    public boolean existeMesaExcluyendoId(int numero, int idMesaExcluir) {
        final String sql = """
                SELECT 1
                FROM mesas
                WHERE numero = ? AND id_mesa <> ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, numero);
            ps.setInt(2, idMesaExcluir);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error al comprobar duplicado de mesa: " + e.getMessage());
        }

        return false;
    }

    /**
     * Comprueba si la mesa está libre.
     *
     * @param idMesa identificador
     * @return true si está libre
     */
    public boolean mesaEstaLibre(int idMesa) {
        Mesa mesa = buscarPorId(idMesa);
        return mesa != null && mesa.getEstado() == EstadoMesa.LIBRE;
    }

    /**
     * Comprueba si la mesa tiene comandas asociadas.
     *
     * @param idMesa identificador
     * @return true si tiene comandas
     */
    public boolean mesaTieneComandas(int idMesa) {
        final String sql = """
                SELECT 1
                FROM comandas
                WHERE id_mesa = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idMesa);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error al comprobar comandas de mesa: " + e.getMessage());
        }

        return false;
    }

    // =========================
    // UPDATE
    // =========================

    /**
     * Actualiza el número de una mesa.
     *
     * @param mesa objeto con datos actualizados
     * @return true si se actualiza correctamente
     */
    public boolean actualizarMesa(Mesa mesa) {
        final String sql = """
                UPDATE mesas
                SET numero = ?
                WHERE id_mesa = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, mesa.getNumero());
            ps.setInt(2, mesa.getIdMesa());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar mesa: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza el estado de una mesa.
     *
     * @param idMesa identificador
     * @param nuevoEstado nuevo estado
     * @return true si se actualiza correctamente
     */
    public boolean actualizarEstadoMesa(int idMesa, EstadoMesa nuevoEstado) {
        final String sql = """
                UPDATE mesas
                SET estado = ?
                WHERE id_mesa = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idMesa);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar estado de mesa: " + e.getMessage());
            return false;
        }
    }

    // =========================
    // DELETE
    // =========================

    /**
     * Elimina una mesa.
     *
     * @param idMesa identificador
     * @return true si se elimina correctamente
     */
    public boolean eliminarMesa(int idMesa) {
        final String sql = """
                DELETE FROM mesas
                WHERE id_mesa = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idMesa);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar mesa: " + e.getMessage());
            return false;
        }
    }

    // =========================
    // MÉTODOS PRIVADOS
    // =========================

    /**
     * Mapea un ResultSet a objeto Mesa.
     *
     * @param rs resultado de la consulta
     * @return objeto Mesa
     * @throws SQLException error de lectura
     */
    private Mesa mapearMesa(ResultSet rs) throws SQLException {
        Mesa mesa = new Mesa();
        mesa.setIdMesa(rs.getInt("id_mesa"));
        mesa.setNumero(rs.getInt("numero"));
        mesa.setEstado(EstadoMesa.valueOf(rs.getString("estado")));
        return mesa;
    }
}