package com.jhonatan.tfg.tpv.dao;

import com.jhonatan.tfg.tpv.database.DatabaseConnection;
import com.jhonatan.tfg.tpv.model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de gestionar las operaciones de acceso a datos
 * relacionadas con la entidad Categoria.
 *
 * Centraliza las consultas SQL sobre la tabla categorias,
 * separando la lógica de persistencia del resto de capas.
 */
public class CategoriaDAO {

    /**
     * Inserta una nueva categoría en la base de datos.
     *
     * @param categoria objeto Categoria a registrar
     * @return true si la inserción se realiza correctamente, false en caso contrario
     */
    public boolean crearCategoria(Categoria categoria) {
        final String sql = """
                INSERT INTO categorias (nombre)
                VALUES (?)
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, categoria.getNombre());

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear categoría: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recupera todas las categorías almacenadas.
     *
     * @return lista de categorías ordenadas por nombre
     */
    public List<Categoria> obtenerTodasLasCategorias() {
        final String sql = """
                SELECT id_categoria, nombre
                FROM categorias
                ORDER BY nombre
                """;

        List<Categoria> categorias = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                categorias.add(mapearCategoria(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
        }

        return categorias;
    }

    /**
     * Busca una categoría por su identificador.
     *
     * @param idCategoria identificador de la categoría
     * @return objeto Categoria si existe, null en caso contrario
     */
    public Categoria buscarPorId(int idCategoria) {
        final String sql = """
                SELECT id_categoria, nombre
                FROM categorias
                WHERE id_categoria = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, idCategoria);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearCategoria(resultSet);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar categoría por ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Busca una categoría por su nombre.
     *
     * @param nombre nombre de la categoría
     * @return objeto Categoria si existe, null en caso contrario
     */
    public Categoria buscarPorNombre(String nombre) {
        final String sql = """
                SELECT id_categoria, nombre
                FROM categorias
                WHERE nombre = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, nombre);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearCategoria(resultSet);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar categoría por nombre: " + e.getMessage());
        }

        return null;
    }

    /**
     * Actualiza el nombre de una categoría existente.
     *
     * @param categoria categoría con los datos actualizados
     * @return true si la actualización se realiza correctamente, false en caso contrario
     */
    public boolean actualizarCategoria(Categoria categoria) {
        final String sql = """
                UPDATE categorias
                SET nombre = ?
                WHERE id_categoria = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, categoria.getNombre());
            preparedStatement.setInt(2, categoria.getIdCategoria());

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina una categoría por su identificador.
     *
     * Puede fallar si existen productos asociados por integridad referencial.
     *
     * @param idCategoria identificador de la categoría
     * @return true si la eliminación se realiza correctamente, false en caso contrario
     */
    public boolean eliminarCategoria(int idCategoria) {
        final String sql = """
                DELETE FROM categorias
                WHERE id_categoria = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, idCategoria);

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            return false;
        }
    }

    /**
     * Comprueba si ya existe una categoría con el nombre indicado.
     *
     * @param nombre nombre a comprobar
     * @return true si existe, false en caso contrario
     */
    public boolean existeCategoria(String nombre) {
        return buscarPorNombre(nombre) != null;
    }

    /**
     * Comprueba si existe otra categoría con el mismo nombre,
     * excluyendo una categoría concreta.
     *
     * Se utiliza al actualizar para evitar falsos duplicados
     * contra la propia categoría editada.
     *
     * @param nombre nombre a comprobar
     * @param idCategoriaExcluir identificador de la categoría a excluir
     * @return true si existe otra categoría con ese nombre, false en caso contrario
     */
    public boolean existeCategoriaExcluyendoId(String nombre, int idCategoriaExcluir) {
        final String sql = """
                SELECT 1
                FROM categorias
                WHERE nombre = ? AND id_categoria <> ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, nombre);
            preparedStatement.setInt(2, idCategoriaExcluir);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.err.println("Error al comprobar categoría duplicada excluyendo ID: " + e.getMessage());
        }

        return false;
    }

    /**
     * Convierte la fila actual de un ResultSet en un objeto Categoria.
     *
     * @param resultSet resultado SQL posicionado en una fila válida
     * @return objeto Categoria con los datos de la fila actual
     * @throws SQLException si ocurre un error al leer los datos
     */
    private Categoria mapearCategoria(ResultSet resultSet) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(resultSet.getInt("id_categoria"));
        categoria.setNombre(resultSet.getString("nombre"));
        return categoria;
    }
}