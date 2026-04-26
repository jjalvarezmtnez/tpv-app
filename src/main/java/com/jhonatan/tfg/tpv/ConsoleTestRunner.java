package com.jhonatan.tfg.tpv;

import com.jhonatan.tfg.tpv.dao.CategoriaDAO;
import com.jhonatan.tfg.tpv.dao.MesaDAO;
import com.jhonatan.tfg.tpv.dao.ProductoDAO;
import com.jhonatan.tfg.tpv.dao.UsuarioDAO;
import com.jhonatan.tfg.tpv.model.Categoria;
import com.jhonatan.tfg.tpv.model.Mesa;
import com.jhonatan.tfg.tpv.model.Producto;
import com.jhonatan.tfg.tpv.model.Usuario;
import com.jhonatan.tfg.tpv.service.ComandaService;
import com.jhonatan.tfg.tpv.util.MoneyUtils;

/**
 * Clase auxiliar para ejecutar pruebas manuales por consola
 * durante la fase de desarrollo.
 *
 * Permite validar operaciones básicas del backend antes de utilizar
 * la interfaz gráfica JavaFX.
 */
public class ConsoleTestRunner {

    /**
     * Constructor privado para evitar instancias.
     */
    private ConsoleTestRunner() {
    }

    /**
     * Ejecuta todas las pruebas manuales disponibles.
     */
    public static void runAllTests() {
        printSection("PRUEBAS DE USUARIOS");
        probarUsuarios();

        printSection("PRUEBAS DE CATEGORÍAS");
        probarCategorias();

        printSection("PRUEBAS DE PRODUCTOS");
        probarProductos();

        printSection("PRUEBAS DE MESAS");
        probarMesas();

        printSection("PRUEBAS DE COMANDA SERVICE");
        probarComandaService();

        System.out.println("Pruebas finalizadas.");
    }

    /**
     * Comprueba autenticación correcta e incorrecta.
     */
    private static void probarUsuarios() {
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        Usuario usuarioAutenticado = usuarioDAO.autenticarUsuario("admin", "1234");

        if (usuarioAutenticado != null) {
            System.out.println("Login correcto. Bienvenido: " + usuarioAutenticado.getNombre());
        } else {
            System.out.println("Error en login.");
        }

        Usuario loginIncorrecto = usuarioDAO.autenticarUsuario("admin", "wrong");

        if (loginIncorrecto == null) {
            System.out.println("Login incorrecto detectado correctamente.");
        } else {
            System.out.println("Error: debería haber fallado el login.");
        }
    }

    /**
     * Muestra categorías registradas.
     */
    private static void probarCategorias() {
        CategoriaDAO categoriaDAO = new CategoriaDAO();

        System.out.println("Listado de categorías:");

        for (Categoria categoria : categoriaDAO.obtenerTodasLasCategorias()) {
            System.out.println("- " + categoria.getIdCategoria() + " | " + categoria.getNombre());
        }
    }

    /**
     * Muestra productos y comprueba datos concretos de uno de ellos.
     */
    private static void probarProductos() {
        ProductoDAO productoDAO = new ProductoDAO();

        System.out.println("Listado de productos:");

        for (String producto : productoDAO.obtenerProductosConCategoria()) {
            System.out.println(producto);
        }

        Producto cocaCola = productoDAO.buscarPorNombre("Coca-Cola");

        if (cocaCola != null) {
            System.out.println("Stock actual de Coca-Cola: " + cocaCola.getStock());
            System.out.println("Precio actual de Coca-Cola: " + MoneyUtils.formatearEuros(cocaCola.getPrecio()));
        }
    }

    /**
     * Muestra las mesas registradas y su estado.
     */
    private static void probarMesas() {
        MesaDAO mesaDAO = new MesaDAO();

        System.out.println("Listado de mesas:");

        for (Mesa mesa : mesaDAO.obtenerTodasLasMesas()) {
            System.out.println("- Mesa " + mesa.getNumero() + " | Estado: " + mesa.getEstado());
        }
    }

    /**
     * Prueba el flujo básico de una comanda:
     * apertura, inserción de producto, cálculo de total, cierre,
     * liberación de mesa y actualización de stock.
     */
    private static void probarComandaService() {
        ComandaService comandaService = new ComandaService();
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        ProductoDAO productoDAO = new ProductoDAO();
        MesaDAO mesaDAO = new MesaDAO();

        Usuario admin = usuarioDAO.buscarPorUsername("admin");
        Mesa mesa1 = mesaDAO.buscarPorNumero(1);
        Producto cocaCola = productoDAO.buscarPorNombre("Coca-Cola");

        if (admin == null || mesa1 == null || cocaCola == null) {
            System.out.println("No se han encontrado los datos necesarios para probar ComandaService.");
            return;
        }

        boolean abierta = comandaService.abrirComanda(mesa1.getIdMesa(), admin.getIdUsuario());
        System.out.println("Comanda abierta mediante service para mesa 1: " + abierta);

        boolean lineaAnadida = comandaService.anadirProductoAComanda(
                mesa1.getIdMesa(),
                cocaCola.getIdProducto(),
                3
        );
        System.out.println("Producto añadido mediante service a mesa 1: " + lineaAnadida);

        double total = comandaService.obtenerTotalComandaAbierta(mesa1.getIdMesa());
        System.out.println("Total de la comanda abierta de mesa 1: " + MoneyUtils.formatearEuros(total));

        double totalRecalculado = comandaService.recalcularTotalComandaAbierta(mesa1.getIdMesa());
        System.out.println("Total recalculado en service para mesa 1: " + MoneyUtils.formatearEuros(totalRecalculado));

        boolean cerrada = comandaService.cerrarComanda(mesa1.getIdMesa());
        System.out.println("Comanda cerrada mediante service para mesa 1: " + cerrada);

        mostrarEstadoFinalMesa(mesaDAO);
        mostrarStockFinalProducto(productoDAO);
    }

    /**
     * Muestra el estado final de la mesa 1 tras la prueba de comanda.
     *
     * @param mesaDAO DAO de mesas
     */
    private static void mostrarEstadoFinalMesa(MesaDAO mesaDAO) {
        Mesa mesaActualizada = mesaDAO.buscarPorNumero(1);

        if (mesaActualizada != null) {
            System.out.println("Estado final de mesa 1: " + mesaActualizada.getEstado());
        }
    }

    /**
     * Muestra el stock final de Coca-Cola tras la prueba de comanda.
     *
     * @param productoDAO DAO de productos
     */
    private static void mostrarStockFinalProducto(ProductoDAO productoDAO) {
        Producto productoActualizado = productoDAO.buscarPorNombre("Coca-Cola");

        if (productoActualizado != null) {
            System.out.println("Stock final de Coca-Cola tras usar service: " + productoActualizado.getStock());
        }
    }

    /**
     * Imprime una cabecera visual para separar bloques de prueba.
     *
     * @param titulo título de la sección
     */
    private static void printSection(String titulo) {
        System.out.println();
        System.out.println("========================================");
        System.out.println(titulo);
        System.out.println("========================================");
    }
}