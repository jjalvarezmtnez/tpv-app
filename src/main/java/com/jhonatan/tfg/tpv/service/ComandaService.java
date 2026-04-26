package com.jhonatan.tfg.tpv.service;

import com.jhonatan.tfg.tpv.dao.ComandaDAO;
import com.jhonatan.tfg.tpv.dao.LineaComandaDAO;
import com.jhonatan.tfg.tpv.dao.MesaDAO;
import com.jhonatan.tfg.tpv.dao.ProductoDAO;
import com.jhonatan.tfg.tpv.model.Comanda;
import com.jhonatan.tfg.tpv.model.LineaComanda;
import com.jhonatan.tfg.tpv.model.Producto;
import com.jhonatan.tfg.tpv.model.enums.EstadoComanda;
import com.jhonatan.tfg.tpv.model.enums.EstadoMesa;
import com.jhonatan.tfg.tpv.util.MoneyUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio encargado de coordinar la lógica de negocio relacionada con las comandas.
 *
 * Actúa como intermediario entre los controladores y los DAOs, agrupando operaciones
 * que afectan a varias entidades: comandas, mesas, productos y líneas de comanda.
 */
public class ComandaService {

    private final ComandaDAO comandaDAO;
    private final MesaDAO mesaDAO;
    private final ProductoDAO productoDAO;
    private final LineaComandaDAO lineaComandaDAO;

    /**
     * Constructor del servicio.
     *
     * Inicializa los DAOs necesarios para realizar operaciones de negocio
     * relacionadas con comandas.
     */
    public ComandaService() {
        this.comandaDAO = new ComandaDAO();
        this.mesaDAO = new MesaDAO();
        this.productoDAO = new ProductoDAO();
        this.lineaComandaDAO = new LineaComandaDAO();
    }

    /**
     * Abre una nueva comanda para una mesa concreta.
     *
     * Antes de crearla valida que la mesa esté libre y que no exista ya
     * una comanda abierta asociada a esa mesa.
     *
     * @param idMesa identificador de la mesa
     * @param idUsuario identificador del usuario que abre la comanda
     * @return true si la comanda se crea correctamente, false en caso contrario
     */
    public boolean abrirComanda(int idMesa, int idUsuario) {
        if (!mesaDAO.mesaEstaLibre(idMesa)) {
            System.err.println("No se puede abrir la comanda: la mesa no está libre.");
            return false;
        }

        if (comandaDAO.existeComandaAbiertaParaMesa(idMesa)) {
            System.err.println("No se puede abrir la comanda: ya existe una comanda abierta para esta mesa.");
            return false;
        }

        Comanda comanda = new Comanda();
        comanda.setIdMesa(idMesa);
        comanda.setIdUsuario(idUsuario);
        comanda.setFechaApertura(LocalDateTime.now().toString());
        comanda.setEstado(EstadoComanda.ABIERTA);

        return comandaDAO.crearComanda(comanda);
    }

    /**
     * Añade un producto a la comanda abierta de una mesa.
     *
     * Si el producto ya existe en la comanda, se incrementa su cantidad.
     * Si no existe, se crea una nueva línea.
     *
     * @param idMesa identificador de la mesa
     * @param idProducto identificador del producto
     * @param cantidad cantidad a añadir
     * @return true si la operación se realiza correctamente, false en caso contrario
     */
    public boolean anadirProductoAComanda(int idMesa, int idProducto, int cantidad) {
        if (cantidad <= 0) {
            System.err.println("La cantidad debe ser mayor que cero.");
            return false;
        }

        Comanda comanda = comandaDAO.buscarComandaAbiertaPorMesa(idMesa);

        if (comanda == null) {
            System.err.println("No existe una comanda abierta para la mesa indicada.");
            return false;
        }

        Producto producto = productoDAO.buscarPorId(idProducto);

        if (producto == null) {
            System.err.println("No existe el producto indicado.");
            return false;
        }

        if (producto.getStock() < cantidad) {
            System.err.println("No hay stock suficiente para añadir el producto a la comanda.");
            return false;
        }

        LineaComanda lineaExistente = lineaComandaDAO.buscarLineaPorComandaYProducto(
                comanda.getIdComanda(),
                producto.getIdProducto()
        );

        if (lineaExistente != null) {
            int nuevaCantidad = lineaExistente.getCantidad() + cantidad;

            if (producto.getStock() < nuevaCantidad) {
                System.err.println("No hay stock suficiente para aumentar la cantidad del producto en la comanda.");
                return false;
            }

            return lineaComandaDAO.actualizarCantidadLinea(lineaExistente.getIdLinea(), nuevaCantidad);
        }

        LineaComanda linea = new LineaComanda();
        linea.setIdComanda(comanda.getIdComanda());
        linea.setIdProducto(producto.getIdProducto());
        linea.setCantidad(cantidad);
        linea.setPrecioUnitario(producto.getPrecio());

        return lineaComandaDAO.crearLineaComanda(linea);
    }

    /**
     * Reduce en una unidad la cantidad de una línea de comanda.
     *
     * Si la línea tiene cantidad 1, se elimina completamente.
     *
     * @param idLinea identificador de la línea de comanda
     * @return true si la operación se realiza correctamente, false en caso contrario
     */
    public boolean reducirCantidadLinea(int idLinea) {
        if (idLinea <= 0) {
            return false;
        }

        LineaComanda linea = lineaComandaDAO.buscarLineaPorId(idLinea);

        if (linea == null) {
            return false;
        }

        if (linea.getCantidad() > 1) {
            int nuevaCantidad = linea.getCantidad() - 1;
            return lineaComandaDAO.actualizarCantidadLinea(idLinea, nuevaCantidad);
        }

        return lineaComandaDAO.eliminarLinea(idLinea);
    }

    /**
     * Elimina una línea concreta de una comanda.
     *
     * @param idLinea identificador de la línea
     * @return true si la línea se elimina correctamente, false en caso contrario
     */
    public boolean eliminarLineaDeComanda(int idLinea) {
        if (idLinea <= 0) {
            return false;
        }

        return lineaComandaDAO.eliminarLinea(idLinea);
    }

    /**
     * Cierra la comanda abierta asociada a una mesa.
     *
     * @param idMesa identificador de la mesa
     * @return true si la comanda se cierra correctamente, false en caso contrario
     */
    public boolean cerrarComanda(int idMesa) {
        Comanda comanda = comandaDAO.buscarComandaAbiertaPorMesa(idMesa);

        if (comanda == null) {
            System.err.println("No existe una comanda abierta para la mesa indicada.");
            return false;
        }

        return comandaDAO.cerrarComanda(comanda.getIdComanda());
    }

    /**
     * Anula la comanda abierta de una mesa siempre que no tenga líneas.
     *
     * Esta operación está pensada para revertir aperturas de comandas realizadas
     * por error antes de registrar productos.
     *
     * @param idMesa identificador de la mesa
     * @return true si la comanda se anula correctamente, false en caso contrario
     */
    public boolean anularComanda(int idMesa) {
        Comanda comanda = comandaDAO.buscarComandaAbiertaPorMesa(idMesa);

        if (comanda == null) {
            return false;
        }

        List<LineaComanda> lineas = lineaComandaDAO.obtenerLineasPorComanda(comanda.getIdComanda());

        if (lineas != null && !lineas.isEmpty()) {
            return false;
        }

        boolean comandaEliminada = comandaDAO.eliminarComanda(comanda.getIdComanda());

        if (!comandaEliminada) {
            return false;
        }

        return mesaDAO.actualizarEstadoMesa(idMesa, EstadoMesa.LIBRE);
    }

    /**
     * Obtiene las líneas de la comanda abierta de una mesa.
     *
     * @param idMesa identificador de la mesa
     * @return lista de líneas de comanda; lista vacía si no existe comanda abierta
     */
    public List<LineaComanda> obtenerLineasDeComandaAbierta(int idMesa) {
        Comanda comanda = comandaDAO.buscarComandaAbiertaPorMesa(idMesa);

        if (comanda == null) {
            return List.of();
        }

        return lineaComandaDAO.obtenerLineasPorComanda(comanda.getIdComanda());
    }

    /**
     * Obtiene el total actual de la comanda abierta de una mesa.
     *
     * @param idMesa identificador de la mesa
     * @return total de la comanda; 0.0 si no existe comanda abierta
     */
    public double obtenerTotalComandaAbierta(int idMesa) {
        Comanda comanda = comandaDAO.buscarComandaAbiertaPorMesa(idMesa);

        if (comanda == null) {
            return 0.0;
        }

        double total = lineaComandaDAO.calcularTotalComanda(comanda.getIdComanda());

        return MoneyUtils.redondear(total);
    }

    /**
     * Recalcula el total de la comanda abierta recorriendo sus líneas en memoria.
     *
     * Se utiliza MoneyUtils para evitar errores de precisión derivados del uso
     * de double en importes monetarios.
     *
     * @param idMesa identificador de la mesa
     * @return total recalculado; 0.0 si no existe comanda abierta
     */
    public double recalcularTotalComandaAbierta(int idMesa) {
        Comanda comanda = comandaDAO.buscarComandaAbiertaPorMesa(idMesa);

        if (comanda == null) {
            return 0.0;
        }

        List<LineaComanda> lineas = lineaComandaDAO.obtenerLineasPorComanda(comanda.getIdComanda());

        double total = 0.0;

        for (LineaComanda linea : lineas) {
            double subtotal = linea.getCantidad() * linea.getPrecioUnitario();
            total = MoneyUtils.sumar(total, subtotal);
        }

        return MoneyUtils.redondear(total);
    }
}