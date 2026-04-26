package com.jhonatan.tfg.tpv.dao;

import com.jhonatan.tfg.tpv.database.DatabaseConnection;
import com.jhonatan.tfg.tpv.model.Producto;
import com.jhonatan.tfg.tpv.util.MoneyUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de gestionar el acceso a datos de la entidad Producto.
 *
 * Responsabilidades:
 * - CRUD de productos
 * - Consultas por ID y nombre
 * - Gestión de stock
 * - Comprobación de duplicados
 */
public class ProductoDAO {

    /**
     * Inserta un nuevo producto en la base de datos.
     *
     * @param producto objeto Producto a registrar
     * @return true si la inserción se realiza correctamente, false en caso contrario
     */
    public boolean crearProducto(Producto producto) {
        final String sql = """
                INSERT INTO productos (nombre, precio, stock, id_categoria)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setInt(3, producto.getStock());
            ps.setInt(4, producto.getIdCategoria());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un producto por su identificador.
     *
     * @param idProducto identificador del producto
     * @return Producto si existe, null en caso contrario
     */
    public Producto buscarPorId(int idProducto) {
        final String sql = """
                SELECT id_producto, nombre, precio, stock, id_categoria
                FROM productos
                WHERE id_producto = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar producto por ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Busca un producto por su nombre.
     *
     * @param nombre nombre del producto
     * @return Producto si existe, null en caso contrario
     */
    public Producto buscarPorNombre(String nombre) {
        final String sql = """
                SELECT id_producto, nombre, precio, stock, id_categoria
                FROM productos
                WHERE nombre = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar producto por nombre: " + e.getMessage());
        }

        return null;
    }

    /**
     * Obtiene todos los productos almacenados.
     *
     * @return lista de productos ordenados por nombre
     */
    public List<Producto> obtenerTodosLosProductos() {
        final String sql = """
                SELECT id_producto, nombre, precio, stock, id_categoria
                FROM productos
                ORDER BY nombre
                """;

        List<Producto> productos = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener todos los productos: " + e.getMessage());
        }

        return productos;
    }

    /**
     * Comprueba si existe un producto con el nombre indicado.
     *
     * @param nombre nombre del producto
     * @return true si existe, false en caso contrario
     */
    public boolean existeProducto(String nombre) {
        return buscarPorNombre(nombre) != null;
    }

    /**
     * Comprueba si existe otro producto con el mismo nombre,
     * excluyendo un producto concreto.
     *
     * @param nombre nombre del producto
     * @param idProductoExcluir identificador del producto a excluir
     * @return true si existe otro producto con ese nombre, false en caso contrario
     */
    public boolean existeProductoConNombreExcluyendoId(String nombre, int idProductoExcluir) {
        final String sql = """
                SELECT 1
                FROM productos
                WHERE nombre = ? AND id_producto <> ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idProductoExcluir);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error al comprobar producto duplicado excluyendo ID: " + e.getMessage());
        }

        return false;
    }

    /**
     * Actualiza los datos principales de un producto.
     *
     * @param producto producto con los datos actualizados
     * @return true si la actualización se realiza correctamente, false en caso contrario
     */
    public boolean actualizarProducto(Producto producto) {
        final String sql = """
                UPDATE productos
                SET nombre = ?, precio = ?, stock = ?, id_categoria = ?
                WHERE id_producto = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setInt(3, producto.getStock());
            ps.setInt(4, producto.getIdCategoria());
            ps.setInt(5, producto.getIdProducto());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza únicamente el stock de un producto.
     *
     * @param idProducto identificador del producto
     * @param nuevoStock nuevo valor de stock
     * @return true si la actualización se realiza correctamente, false en caso contrario
     */
    public boolean actualizarStock(int idProducto, int nuevoStock) {
        final String sql = """
                UPDATE productos
                SET stock = ?
                WHERE id_producto = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, nuevoStock);
            ps.setInt(2, idProducto);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar el stock del producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un producto por su identificador.
     *
     * Puede fallar si el producto está asociado a líneas de comanda
     * por restricciones de integridad referencial.
     *
     * @param idProducto identificador del producto
     * @return true si se elimina correctamente, false en caso contrario
     */
    public boolean eliminarProducto(int idProducto) {
        final String sql = """
                DELETE FROM productos
                WHERE id_producto = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene una representación textual de productos con su categoría.
     *
     * Este método es útil para vistas simples o depuración, aunque para TableView
     * es preferible trabajar con objetos en lugar de cadenas formateadas.
     *
     * @return lista de textos con información de producto y categoría
     */
    public List<String> obtenerProductosConCategoria() {
        final String sql = """
                SELECT p.id_producto, p.nombre, p.precio, p.stock, c.nombre AS categoria
                FROM productos p
                JOIN categorias c ON p.id_categoria = c.id_categoria
                ORDER BY p.nombre
                """;

        List<String> productos = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String producto = rs.getInt("id_producto") + " | "
                        + rs.getString("nombre") + " | "
                        + MoneyUtils.formatearEuros(rs.getDouble("precio")) + " | "
                        + "Stock: " + rs.getInt("stock") + " | "
                        + rs.getString("categoria");

                productos.add(producto);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos con categoría: " + e.getMessage());
        }

        return productos;
    }

    /**
     * Convierte una fila del ResultSet en un objeto Producto.
     *
     * @param rs resultado SQL posicionado en una fila válida
     * @return objeto Producto mapeado
     * @throws SQLException si ocurre un error al leer los datos
     */
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setNombre(rs.getString("nombre"));
        producto.setPrecio(rs.getDouble("precio"));
        producto.setStock(rs.getInt("stock"));
        producto.setIdCategoria(rs.getInt("id_categoria"));
        return producto;
    }
}