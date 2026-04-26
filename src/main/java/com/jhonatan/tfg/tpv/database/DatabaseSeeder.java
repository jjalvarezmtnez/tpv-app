package com.jhonatan.tfg.tpv.database;

import com.jhonatan.tfg.tpv.dao.CategoriaDAO;
import com.jhonatan.tfg.tpv.dao.MesaDAO;
import com.jhonatan.tfg.tpv.dao.ProductoDAO;
import com.jhonatan.tfg.tpv.dao.UsuarioDAO;
import com.jhonatan.tfg.tpv.model.Categoria;
import com.jhonatan.tfg.tpv.model.Mesa;
import com.jhonatan.tfg.tpv.model.Producto;
import com.jhonatan.tfg.tpv.model.Usuario;
import com.jhonatan.tfg.tpv.model.enums.EstadoMesa;
import com.jhonatan.tfg.tpv.model.enums.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase encargada de insertar datos semilla en la base de datos.
 *
 * Su objetivo es dejar un estado inicial coherente y controlado
 * para facilitar las pruebas del sistema durante el desarrollo.
 *
 * Los datos se insertan solo si no existen, evitando duplicados
 * al reiniciar la aplicación.
 */
public final class DatabaseSeeder {

    /**
     * Constructor privado para evitar instanciación.
     */
    private DatabaseSeeder() {
    }

    /**
     * Inserta todos los datos semilla necesarios para probar la aplicación.
     */
    public static void seedDatabase() {
        seedUsuarios();
        seedCategorias();
        seedProductos();
        seedMesas();
        seedComandasCerradas();

        System.out.println("Datos semilla insertados correctamente.");
    }

    /**
     * Inserta los usuarios base del sistema.
     *
     * Se crean dos perfiles:
     * - admin: usuario administrador
     * - camarero: usuario operativo
     */
    private static void seedUsuarios() {
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        if (!usuarioDAO.existeUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setNombre("Administrador");
            admin.setPassword("1234");
            admin.setRol(Rol.ADMIN);
            admin.setActivo(true);

            usuarioDAO.registrarUsuario(admin);
        }

        if (!usuarioDAO.existeUsername("camarero")) {
            Usuario camarero = new Usuario();
            camarero.setUsername("camarero");
            camarero.setNombre("Camarero");
            camarero.setPassword("1234");
            camarero.setRol(Rol.CAMARERO);
            camarero.setActivo(true);

            usuarioDAO.registrarUsuario(camarero);
        }
    }

    /**
     * Inserta las categorías base del catálogo.
     */
    private static void seedCategorias() {
        CategoriaDAO categoriaDAO = new CategoriaDAO();

        crearCategoriaSiNoExiste(categoriaDAO, "Bebidas");
        crearCategoriaSiNoExiste(categoriaDAO, "Cafés");
        crearCategoriaSiNoExiste(categoriaDAO, "Tapas");
        crearCategoriaSiNoExiste(categoriaDAO, "Bocadillos");
        crearCategoriaSiNoExiste(categoriaDAO, "Postres");
    }

    /**
     * Inserta productos de prueba con diferentes niveles de stock.
     *
     * Algunos productos tienen stock alto, otros stock bajo
     * y otros stock cero para probar los estilos visuales de la interfaz.
     */
    private static void seedProductos() {
        CategoriaDAO categoriaDAO = new CategoriaDAO();
        ProductoDAO productoDAO = new ProductoDAO();

        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Coca-Cola", 2.80, 60, "Bebidas");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Agua mineral", 1.50, 30, "Bebidas");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Nestea", 2.70, 8, "Bebidas");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Zumo de naranja", 2.40, 0, "Bebidas");

        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Café solo", 1.30, 80, "Cafés");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Café con leche", 1.60, 50, "Cafés");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Cappuccino", 2.10, 6, "Cafés");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Chocolate caliente", 2.30, 0, "Cafés");

        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Tortilla", 3.20, 20, "Tapas");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Croquetas", 4.50, 9, "Tapas");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Patatas bravas", 4.00, 15, "Tapas");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Calamares", 5.80, 0, "Tapas");

        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Bocadillo de lomo", 5.50, 18, "Bocadillos");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Bocadillo de bacon", 5.20, 7, "Bocadillos");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Bocadillo vegetal", 4.80, 3, "Bocadillos");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Bocadillo de tortilla", 4.50, 0, "Bocadillos");

        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Tarta de queso", 4.20, 12, "Postres");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Flan casero", 3.00, 5, "Postres");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Brownie", 3.80, 2, "Postres");
        crearProductoSiNoExiste(productoDAO, categoriaDAO, "Helado", 2.90, 0, "Postres");
    }

    /**
     * Inserta mesas base del establecimiento.
     */
    private static void seedMesas() {
        MesaDAO mesaDAO = new MesaDAO();

        for (int numero = 1; numero <= 5; numero++) {
            if (!mesaDAO.existeMesa(numero)) {
                Mesa mesa = new Mesa();
                mesa.setNumero(numero);
                mesa.setEstado(EstadoMesa.LIBRE);

                mesaDAO.crearMesa(mesa);
            }
        }
    }

    /**
     * Inserta comandas cerradas de prueba.
     *
     * Estos datos permiten probar:
     * - historial de ventas
     * - métricas agregadas
     * - gráficos de informes
     * - filtros por fecha
     */
    private static void seedComandasCerradas() {
        if (existenComandasCerradas()) {
            return;
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO();

        Usuario admin = usuarioDAO.buscarPorUsername("admin");
        Usuario camarero = usuarioDAO.buscarPorUsername("camarero");

        if (admin == null || camarero == null) {
            return;
        }

        insertarComandaCerrada(1, camarero.getIdUsuario(), "2026-04-20 10:15:00",
                new LineaSeed("Café solo", 4),
                new LineaSeed("Tarta de queso", 2),
                new LineaSeed("Agua mineral", 3)
        );

        insertarComandaCerrada(2, camarero.getIdUsuario(), "2026-04-20 14:20:00",
                new LineaSeed("Tortilla", 3),
                new LineaSeed("Coca-Cola", 4),
                new LineaSeed("Patatas bravas", 2)
        );

        insertarComandaCerrada(3, admin.getIdUsuario(), "2026-04-21 09:35:00",
                new LineaSeed("Café con leche", 6),
                new LineaSeed("Flan casero", 2)
        );

        insertarComandaCerrada(1, camarero.getIdUsuario(), "2026-04-21 15:10:00",
                new LineaSeed("Bocadillo de lomo", 3),
                new LineaSeed("Nestea", 2),
                new LineaSeed("Croquetas", 2)
        );

        insertarComandaCerrada(4, camarero.getIdUsuario(), "2026-04-22 11:45:00",
                new LineaSeed("Cappuccino", 3),
                new LineaSeed("Brownie", 2),
                new LineaSeed("Agua mineral", 2)
        );

        insertarComandaCerrada(5, admin.getIdUsuario(), "2026-04-22 21:05:00",
                new LineaSeed("Bocadillo de bacon", 4),
                new LineaSeed("Coca-Cola", 5),
                new LineaSeed("Tarta de queso", 2)
        );

        insertarComandaCerrada(2, camarero.getIdUsuario(), "2026-04-23 13:30:00",
                new LineaSeed("Patatas bravas", 5),
                new LineaSeed("Croquetas", 4),
                new LineaSeed("Café solo", 2)
        );

        insertarComandaCerrada(3, camarero.getIdUsuario(), "2026-04-23 20:40:00",
                new LineaSeed("Bocadillo vegetal", 2),
                new LineaSeed("Zumo de naranja", 1),
                new LineaSeed("Flan casero", 3)
        );

        insertarComandaCerrada(1, admin.getIdUsuario(), "2026-04-24 12:10:00",
                new LineaSeed("Tortilla", 4),
                new LineaSeed("Coca-Cola", 3),
                new LineaSeed("Café con leche", 2)
        );

        insertarComandaCerrada(5, camarero.getIdUsuario(), "2026-04-24 18:25:00",
                new LineaSeed("Bocadillo de lomo", 2),
                new LineaSeed("Bocadillo de bacon", 2),
                new LineaSeed("Nestea", 3)
        );

        insertarComandaCerrada(4, camarero.getIdUsuario(), "2026-04-25 10:05:00",
                new LineaSeed("Café solo", 8),
                new LineaSeed("Cappuccino", 2),
                new LineaSeed("Brownie", 1)
        );

        insertarComandaCerrada(2, admin.getIdUsuario(), "2026-04-25 15:50:00",
                new LineaSeed("Patatas bravas", 3),
                new LineaSeed("Tarta de queso", 3),
                new LineaSeed("Agua mineral", 4)
        );
    }

    /**
     * Crea una categoría si no existe previamente.
     *
     * @param categoriaDAO DAO de categorías
     * @param nombre nombre de la categoría
     */
    private static void crearCategoriaSiNoExiste(CategoriaDAO categoriaDAO, String nombre) {
        if (!categoriaDAO.existeCategoria(nombre)) {
            Categoria categoria = new Categoria();
            categoria.setNombre(nombre);

            categoriaDAO.crearCategoria(categoria);
        }
    }

    /**
     * Crea un producto si no existe previamente.
     *
     * @param productoDAO DAO de productos
     * @param categoriaDAO DAO de categorías
     * @param nombre nombre del producto
     * @param precio precio unitario
     * @param stock stock inicial
     * @param nombreCategoria categoría asociada
     */
    private static void crearProductoSiNoExiste(ProductoDAO productoDAO,
                                                CategoriaDAO categoriaDAO,
                                                String nombre,
                                                double precio,
                                                int stock,
                                                String nombreCategoria) {
        if (productoDAO.existeProducto(nombre)) {
            return;
        }

        Categoria categoria = categoriaDAO.buscarPorNombre(nombreCategoria);

        if (categoria == null) {
            return;
        }

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setIdCategoria(categoria.getIdCategoria());

        productoDAO.crearProducto(producto);
    }

    /**
     * Comprueba si ya existen comandas cerradas.
     *
     * Esto evita duplicar datos históricos cada vez que se inicia la aplicación.
     *
     * @return true si ya existen comandas cerradas, false en caso contrario
     */
    private static boolean existenComandasCerradas() {
        final String sql = """
                SELECT COUNT(*) AS total
                FROM comandas
                WHERE estado = 'CERRADA'
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total") > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al comprobar comandas cerradas: " + e.getMessage());
        }

        return false;
    }

    /**
     * Inserta una comanda cerrada de prueba y sus líneas asociadas.
     *
     * @param numeroMesa número visible de la mesa
     * @param idUsuario identificador del usuario asociado
     * @param fechaApertura fecha de apertura de la comanda
     * @param lineas líneas de productos incluidas en la comanda
     */
    private static void insertarComandaCerrada(int numeroMesa,
                                               int idUsuario,
                                               String fechaApertura,
                                               LineaSeed... lineas) {
        Integer idMesa = obtenerIdMesaPorNumero(numeroMesa);

        if (idMesa == null) {
            return;
        }

        final String sqlComanda = """
                INSERT INTO comandas (id_mesa, id_usuario, fecha_apertura, estado)
                VALUES (?, ?, ?, 'CERRADA')
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     sqlComanda,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            preparedStatement.setInt(1, idMesa);
            preparedStatement.setInt(2, idUsuario);
            preparedStatement.setString(3, fechaApertura);

            int filasInsertadas = preparedStatement.executeUpdate();

            if (filasInsertadas == 0) {
                return;
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idComanda = generatedKeys.getInt(1);

                    for (LineaSeed linea : lineas) {
                        insertarLineaComanda(idComanda, linea);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al insertar comanda cerrada: " + e.getMessage());
        }
    }

    /**
     * Inserta una línea asociada a una comanda cerrada.
     *
     * @param idComanda identificador de la comanda
     * @param linea datos de la línea a insertar
     */
    private static void insertarLineaComanda(int idComanda, LineaSeed linea) {
        ProductoInfo productoInfo = obtenerProductoInfo(linea.nombreProducto());

        if (productoInfo == null) {
            return;
        }

        final String sql = """
                INSERT INTO lineas_comanda (id_comanda, id_producto, cantidad, precio_unitario)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, idComanda);
            preparedStatement.setInt(2, productoInfo.idProducto());
            preparedStatement.setInt(3, linea.cantidad());
            preparedStatement.setDouble(4, productoInfo.precio());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al insertar línea de comanda: " + e.getMessage());
        }
    }

    /**
     * Obtiene el identificador interno de una mesa a partir de su número visible.
     *
     * @param numeroMesa número visible de la mesa
     * @return identificador de la mesa o null si no existe
     */
    private static Integer obtenerIdMesaPorNumero(int numeroMesa) {
        final String sql = """
                SELECT id_mesa
                FROM mesas
                WHERE numero = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, numeroMesa);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id_mesa");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener ID de mesa: " + e.getMessage());
        }

        return null;
    }

    /**
     * Obtiene la información necesaria de un producto para crear líneas de comanda.
     *
     * @param nombreProducto nombre del producto
     * @return información básica del producto o null si no existe
     */
    private static ProductoInfo obtenerProductoInfo(String nombreProducto) {
        final String sql = """
                SELECT id_producto, precio
                FROM productos
                WHERE nombre = ?
                """;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, nombreProducto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new ProductoInfo(
                            resultSet.getInt("id_producto"),
                            resultSet.getDouble("precio")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener información del producto: " + e.getMessage());
        }

        return null;
    }

    /**
     * Record auxiliar para representar una línea semilla antes de insertarla.
     *
     * @param nombreProducto nombre del producto
     * @param cantidad cantidad vendida
     */
    private record LineaSeed(String nombreProducto, int cantidad) {
    }

    /**
     * Record auxiliar con los datos mínimos de un producto.
     *
     * @param idProducto identificador del producto
     * @param precio precio unitario
     */
    private record ProductoInfo(int idProducto, double precio) {
    }
}