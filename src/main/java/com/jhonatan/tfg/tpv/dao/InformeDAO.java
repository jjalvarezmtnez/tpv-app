package com.jhonatan.tfg.tpv.dao;

import com.jhonatan.tfg.tpv.database.DatabaseConnection;
import com.jhonatan.tfg.tpv.model.informe.HistorialVentaInforme;
import com.jhonatan.tfg.tpv.model.informe.ResumenProductoInforme;
import com.jhonatan.tfg.tpv.util.MoneyUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO encargado de obtener datos agregados para la vista de informes.
 *
 * A diferencia de otros DAO, esta clase no se centra en operaciones CRUD,
 * sino en consultas de lectura orientadas a métricas, estadísticas,
 * gráficos e historial de ventas.
 */
public class InformeDAO {

    /**
     * Obtiene el total de ingresos de todas las comandas cerradas.
     *
     * @return total de ingresos redondeado a 2 decimales
     */
    public double obtenerTotalIngresos() {
        final String sql = """
                SELECT SUM(lc.cantidad * lc.precio_unitario) AS total
                FROM lineas_comanda lc
                JOIN comandas c ON lc.id_comanda = c.id_comanda
                WHERE c.estado = 'CERRADA'
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return MoneyUtils.redondear(resultSet.getDouble("total"));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener el total de ingresos: " + e.getMessage());
        }

        return 0.0;
    }

    /**
     * Obtiene el total de ingresos de una fecha concreta.
     *
     * @param fecha fecha en formato YYYY-MM-DD
     * @return total de ingresos de la fecha indicada
     */
    public double obtenerTotalIngresosPorFecha(String fecha) {
        final String sql = """
                SELECT SUM(lc.cantidad * lc.precio_unitario) AS total_ingresos
                FROM comandas c
                JOIN lineas_comanda lc ON c.id_comanda = lc.id_comanda
                WHERE c.estado = 'CERRADA'
                  AND substr(c.fecha_apertura, 1, 10) = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, fecha);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return MoneyUtils.redondear(resultSet.getDouble("total_ingresos"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener ingresos por fecha: " + e.getMessage());
        }

        return 0.0;
    }

    /**
     * Cuenta cuántas comandas cerradas existen en el sistema.
     *
     * @return número de comandas cerradas
     */
    public int contarComandasCerradas() {
        final String sql = """
                SELECT COUNT(*) AS total
                FROM comandas
                WHERE estado = 'CERRADA'
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error al contar comandas cerradas: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Cuenta cuántas comandas cerradas existen en una fecha concreta.
     *
     * @param fecha fecha en formato YYYY-MM-DD
     * @return número de comandas cerradas en esa fecha
     */
    public int contarComandasCerradasPorFecha(String fecha) {
        final String sql = """
                SELECT COUNT(*) AS total
                FROM comandas
                WHERE estado = 'CERRADA'
                  AND substr(fecha_apertura, 1, 10) = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, fecha);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al contar comandas cerradas por fecha: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Calcula el total de unidades vendidas en comandas cerradas.
     *
     * @return total de unidades vendidas
     */
    public int obtenerTotalProductosVendidos() {
        final String sql = """
                SELECT SUM(lc.cantidad) AS total
                FROM lineas_comanda lc
                JOIN comandas c ON lc.id_comanda = c.id_comanda
                WHERE c.estado = 'CERRADA'
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener total de productos vendidos: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Calcula el total de unidades vendidas en una fecha concreta.
     *
     * @param fecha fecha en formato YYYY-MM-DD
     * @return total de unidades vendidas en esa fecha
     */
    public int obtenerTotalProductosVendidosPorFecha(String fecha) {
        final String sql = """
                SELECT SUM(lc.cantidad) AS total_productos
                FROM comandas c
                JOIN lineas_comanda lc ON c.id_comanda = lc.id_comanda
                WHERE c.estado = 'CERRADA'
                  AND substr(c.fecha_apertura, 1, 10) = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, fecha);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total_productos");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos vendidos por fecha: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Obtiene el producto más vendido considerando todas las comandas cerradas.
     *
     * @return nombre del producto más vendido, o "Sin datos" si no hay ventas
     */
    public String obtenerProductoMasVendido() {
        final String sql = """
                SELECT p.nombre
                FROM lineas_comanda lc
                JOIN comandas c ON lc.id_comanda = c.id_comanda
                JOIN productos p ON lc.id_producto = p.id_producto
                WHERE c.estado = 'CERRADA'
                GROUP BY p.id_producto, p.nombre
                ORDER BY SUM(lc.cantidad) DESC
                LIMIT 1
                """;

        return obtenerNombreProductoMasVendido(sql, null);
    }

    /**
     * Obtiene el producto más vendido en una fecha concreta.
     *
     * @param fecha fecha en formato YYYY-MM-DD
     * @return nombre del producto más vendido, o "Sin datos" si no hay ventas
     */
    public String obtenerProductoMasVendidoPorFecha(String fecha) {
        final String sql = """
                SELECT p.nombre
                FROM comandas c
                JOIN lineas_comanda lc ON c.id_comanda = lc.id_comanda
                JOIN productos p ON lc.id_producto = p.id_producto
                WHERE c.estado = 'CERRADA'
                  AND substr(c.fecha_apertura, 1, 10) = ?
                GROUP BY p.id_producto, p.nombre
                ORDER BY SUM(lc.cantidad) DESC, p.nombre ASC
                LIMIT 1
                """;

        return obtenerNombreProductoMasVendido(sql, fecha);
    }

    /**
     * Obtiene datos de productos más vendidos para gráficos.
     *
     * @return mapa ordenado (producto → unidades vendidas)
     */
    public Map<String, Integer> obtenerProductosMasVendidosParaGrafico() {
        final String sql = """
                SELECT p.nombre, SUM(lc.cantidad) AS total_vendido
                FROM lineas_comanda lc
                JOIN comandas c ON lc.id_comanda = c.id_comanda
                JOIN productos p ON lc.id_producto = p.id_producto
                WHERE c.estado = 'CERRADA'
                GROUP BY p.id_producto, p.nombre
                ORDER BY total_vendido DESC, p.nombre ASC
                LIMIT 8
                """;

        return ejecutarConsultaGraficoProductos(sql, null);
    }

    /**
     * Obtiene datos de productos más vendidos para gráficos filtrados por fecha.
     *
     * @param fecha fecha en formato YYYY-MM-DD
     * @return mapa ordenado (producto → unidades vendidas)
     */
    public Map<String, Integer> obtenerProductosMasVendidosParaGraficoPorFecha(String fecha) {
        final String sql = """
                SELECT p.nombre, SUM(lc.cantidad) AS total_vendido
                FROM lineas_comanda lc
                JOIN comandas c ON lc.id_comanda = c.id_comanda
                JOIN productos p ON lc.id_producto = p.id_producto
                WHERE c.estado = 'CERRADA'
                  AND substr(c.fecha_apertura, 1, 10) = ?
                GROUP BY p.id_producto, p.nombre
                ORDER BY total_vendido DESC, p.nombre ASC
                LIMIT 8
                """;

        return ejecutarConsultaGraficoProductos(sql, fecha);
    }

    /**
     * Obtiene ingresos agrupados por día para gráficos.
     *
     * @return mapa ordenado (fecha → ingresos)
     */
    public Map<String, Double> obtenerIngresosPorDiaParaGrafico() {
        final String sql = """
                SELECT substr(c.fecha_apertura, 1, 10) AS fecha,
                       SUM(lc.cantidad * lc.precio_unitario) AS total_ingresos
                FROM comandas c
                JOIN lineas_comanda lc ON c.id_comanda = lc.id_comanda
                WHERE c.estado = 'CERRADA'
                GROUP BY fecha
                ORDER BY fecha ASC
                """;

        Map<String, Double> datos = new LinkedHashMap<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                datos.put(
                        rs.getString("fecha"),
                        MoneyUtils.redondear(rs.getDouble("total_ingresos"))
                );
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener ingresos por día: " + e.getMessage());
        }

        return datos;
    }

    /**
     * Obtiene resumen de productos vendidos para tabla.
     *
     * @return lista de filas con producto y unidades vendidas
     */
    public List<ResumenProductoInforme> obtenerResumenProductosVendidosTabla() {
        final String sql = """
                SELECT p.nombre, SUM(lc.cantidad) AS total_vendido
                FROM lineas_comanda lc
                JOIN comandas c ON lc.id_comanda = c.id_comanda
                JOIN productos p ON lc.id_producto = p.id_producto
                WHERE c.estado = 'CERRADA'
                GROUP BY p.id_producto, p.nombre
                ORDER BY total_vendido DESC, p.nombre ASC
                """;

        return ejecutarConsultaResumen(sql, null);
    }

    /**
     * Obtiene resumen de productos vendidos por fecha para tabla.
     *
     * @param fecha fecha en formato YYYY-MM-DD
     * @return lista de filas
     */
    public List<ResumenProductoInforme> obtenerResumenProductosVendidosTablaPorFecha(String fecha) {
        final String sql = """
                SELECT p.nombre, SUM(lc.cantidad) AS total_vendido
                FROM lineas_comanda lc
                JOIN comandas c ON lc.id_comanda = c.id_comanda
                JOIN productos p ON lc.id_producto = p.id_producto
                WHERE c.estado = 'CERRADA'
                  AND substr(c.fecha_apertura, 1, 10) = ?
                GROUP BY p.id_producto, p.nombre
                ORDER BY total_vendido DESC, p.nombre ASC
                """;

        return ejecutarConsultaResumen(sql, fecha);
    }

    /**
     * Obtiene historial de ventas para tabla.
     *
     * @return lista de ventas
     */
    public List<HistorialVentaInforme> obtenerHistorialVentasTabla() {
        final String sql = """
                SELECT m.numero AS numero_mesa,
                       c.fecha_apertura,
                       SUM(lc.cantidad * lc.precio_unitario) AS total_venta
                FROM comandas c
                JOIN mesas m ON c.id_mesa = m.id_mesa
                JOIN lineas_comanda lc ON c.id_comanda = lc.id_comanda
                WHERE c.estado = 'CERRADA'
                GROUP BY c.id_comanda, m.numero, c.fecha_apertura
                ORDER BY c.fecha_apertura DESC
                """;

        return ejecutarConsultaHistorial(sql, null);
    }

    /**
     * Obtiene historial de ventas por fecha para tabla.
     *
     * @param fecha fecha en formato YYYY-MM-DD
     * @return lista de ventas
     */
    public List<HistorialVentaInforme> obtenerHistorialVentasTablaPorFecha(String fecha) {
        final String sql = """
                SELECT m.numero AS numero_mesa,
                       c.fecha_apertura,
                       SUM(lc.cantidad * lc.precio_unitario) AS total_venta
                FROM comandas c
                JOIN mesas m ON c.id_mesa = m.id_mesa
                JOIN lineas_comanda lc ON c.id_comanda = lc.id_comanda
                WHERE c.estado = 'CERRADA'
                  AND substr(c.fecha_apertura, 1, 10) = ?
                GROUP BY c.id_comanda, m.numero, c.fecha_apertura
                ORDER BY c.fecha_apertura DESC
                """;

        return ejecutarConsultaHistorial(sql, fecha);
    }

    // =========================
    // MÉTODOS PRIVADOS AUXILIARES
    // =========================

    /**
     * Ejecuta consulta para obtener producto más vendido.
     */
    private String obtenerNombreProductoMasVendido(String sql, String fecha) {
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            if (fecha != null) {
                ps.setString(1, fecha);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nombre");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener producto más vendido: " + e.getMessage());
        }

        return "Sin datos";
    }

    /**
     * Ejecuta consulta de gráfico de productos.
     */
    private Map<String, Integer> ejecutarConsultaGraficoProductos(String sql, String fecha) {
        Map<String, Integer> datos = new LinkedHashMap<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            if (fecha != null) {
                ps.setString(1, fecha);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.put(
                            rs.getString("nombre"),
                            rs.getInt("total_vendido")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en gráfico productos: " + e.getMessage());
        }

        return datos;
    }

    /**
     * Ejecuta consulta de resumen de productos.
     */
    private List<ResumenProductoInforme> ejecutarConsultaResumen(String sql, String fecha) {
        List<ResumenProductoInforme> lista = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            if (fecha != null) {
                ps.setString(1, fecha);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ResumenProductoInforme(
                            rs.getString("nombre"),
                            rs.getInt("total_vendido")
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en resumen productos: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Ejecuta consulta de historial.
     */
    private List<HistorialVentaInforme> ejecutarConsultaHistorial(String sql, String fecha) {
        List<HistorialVentaInforme> lista = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            if (fecha != null) {
                ps.setString(1, fecha);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new HistorialVentaInforme(
                            rs.getInt("numero_mesa"),
                            formatearFechaHistorial(rs.getString("fecha_apertura")),
                            MoneyUtils.formatearEuros(rs.getDouble("total_venta"))
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en historial: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Formatea fecha ISO a formato legible.
     *
     * @param fechaTexto fecha en texto
     * @return fecha formateada
     */
    private String formatearFechaHistorial(String fechaTexto) {
        try {
            String base = fechaTexto.length() >= 19
                    ? fechaTexto.substring(0, 19)
                    : fechaTexto;

            base = base.replace(" ", "T");

            LocalDateTime fecha = LocalDateTime.parse(base);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            return fecha.format(formatter);

        } catch (Exception e) {
            return fechaTexto;
        }
    }
}