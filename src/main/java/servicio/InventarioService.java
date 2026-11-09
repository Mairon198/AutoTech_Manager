package servicio;

import conexion.ConexionDB;
import dao.MovimientoInventarioDAO;
import dao.ProveedorDAO;
import dao.RepuestoDAO;
import dao.impl.MovimientoInventarioDAOImpl;
import dao.impl.ProveedorDAOImpl;
import dao.impl.RepuestoDAOImpl;
import excepciones.DuplicadoException;
import excepciones.PersistenciaException;
import excepciones.StockInsuficienteException;
import excepciones.ValidacionException;
import modelo.MovimientoInventario;
import modelo.Proveedor;
import modelo.Repuesto;
import modelo.enums.CategoriaRepuesto;
import modelo.enums.TipoMovimiento;
import util.Validador;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class InventarioService {

    private final RepuestoDAO repuestoDAO;
    private final ProveedorDAO proveedorDAO;
    private final MovimientoInventarioDAO movimientoDAO;

    public InventarioService() {
        this(new RepuestoDAOImpl(), new ProveedorDAOImpl(), new MovimientoInventarioDAOImpl());
    }

    public InventarioService(RepuestoDAO repuestoDAO, ProveedorDAO proveedorDAO, MovimientoInventarioDAO movimientoDAO) {
        this.repuestoDAO = repuestoDAO;
        this.proveedorDAO = proveedorDAO;
        this.movimientoDAO = movimientoDAO;
    }

    public Proveedor registrarProveedor(Proveedor p) throws ValidacionException, DuplicadoException, PersistenciaException {
        validarProveedor(p);
        if (proveedorDAO.existeRtn(p.getRtn(), null)) {
            throw new DuplicadoException("Ya existe un proveedor con el RTN: " + p.getRtn());
        }
        int id = proveedorDAO.insertar(p);
        p.setId(id);
        return p;
    }

    public void actualizarProveedor(Proveedor p) throws ValidacionException, DuplicadoException, PersistenciaException {
        if (p.getId() <= 0) {
            throw new ValidacionException("El proveedor debe tener un ID válido para actualizar.");
        }
        validarProveedor(p);
        if (proveedorDAO.existeRtn(p.getRtn(), p.getId())) {
            throw new DuplicadoException("El RTN '" + p.getRtn() + "' ya pertenece a otro proveedor.");
        }
        proveedorDAO.actualizar(p);
    }

    public void desactivarProveedor(int proveedorId) throws PersistenciaException {
        proveedorDAO.desactivar(proveedorId);
    }

    public List<Proveedor> listarProveedoresActivos() throws PersistenciaException {
        return proveedorDAO.listarActivos();
    }

    public List<Proveedor> listarTodosProveedores() throws PersistenciaException {
        return proveedorDAO.listar();
    }

    public Repuesto registrarRepuesto(Repuesto r) throws ValidacionException, DuplicadoException, PersistenciaException {
        validarRepuesto(r);
        if (repuestoDAO.existeCodigo(r.getCodigo(), null)) {
            throw new DuplicadoException("Ya existe un repuesto con el código: " + r.getCodigo().toUpperCase().trim());
        }
        int id = repuestoDAO.insertar(r);
        r.setId(id);
        return r;
    }

    public void actualizarRepuesto(Repuesto r) throws ValidacionException, DuplicadoException, PersistenciaException {
        if (r.getId() <= 0) {
            throw new ValidacionException("El repuesto debe tener un ID válido para actualizar.");
        }
        validarRepuesto(r);
        if (repuestoDAO.existeCodigo(r.getCodigo(), r.getId())) {
            throw new DuplicadoException("El código '" + r.getCodigo().toUpperCase().trim() + "' ya pertenece a otro repuesto.");
        }
        repuestoDAO.actualizar(r);
    }

    public void desactivarRepuesto(int repuestoId) throws PersistenciaException {
        repuestoDAO.desactivar(repuestoId);
    }

    public Optional<Repuesto> buscarPorId(int id) throws PersistenciaException {
        return repuestoDAO.buscarPorId(id);
    }

    public Optional<Repuesto> buscarPorCodigo(String codigo) throws PersistenciaException {
        return repuestoDAO.buscarPorCodigo(codigo);
    }

    public List<Repuesto> listarTodosRepuestos() throws PersistenciaException {
        return repuestoDAO.listar();
    }

    public List<Repuesto> listarRepuestosActivos() throws PersistenciaException {
        return repuestoDAO.listarActivos();
    }

    public List<Repuesto> listarBajoStockMinimo() throws PersistenciaException {
        return repuestoDAO.listarBajoStockMinimo();
    }

    public List<Repuesto> listarPorCategoria(CategoriaRepuesto categoria) throws PersistenciaException {
        return repuestoDAO.listarPorCategoria(categoria);
    }

    public List<Repuesto> buscarRepuestos(String filtro) throws PersistenciaException {
        return repuestoDAO.buscarPorCriterio(filtro);
    }

    public synchronized MovimientoInventario descontarStockPorOrden(int ordenId, int repuestoId, int cantidad, String usuario)
            throws ValidacionException, StockInsuficienteException, PersistenciaException {

        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad a descontar debe ser mayor que cero.");
        }

        Connection cn = null;
        boolean originalAutoCommit = true;

        try {
            cn = ConexionDB.getConexion();
            originalAutoCommit = cn.getAutoCommit();
            cn.setAutoCommit(false);

            String sqlSelect = "SELECT repuesto_id, codigo, nombre, stock_actual, stock_minimo, costo_compra, precio_venta, activo "
                             + "FROM dbo.Repuestos WITH (UPDLOCK, HOLDLOCK) "
                             + "WHERE repuesto_id = ?";

            int stockActual;
            BigDecimal costoCompra;
            BigDecimal precioVenta;
            String nombreRepuesto;
            boolean activo;

            try (PreparedStatement ps = cn.prepareStatement(sqlSelect)) {
                ps.setInt(1, repuestoId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ValidacionException("El repuesto con ID " + repuestoId + " no existe.");
                    }
                    stockActual = rs.getInt("stock_actual");
                    costoCompra = rs.getBigDecimal("costo_compra");
                    precioVenta = rs.getBigDecimal("precio_venta");
                    nombreRepuesto = rs.getString("nombre");
                    activo = rs.getBoolean("activo");
                }
            }

            if (!activo) {
                throw new ValidacionException("El repuesto '" + nombreRepuesto + "' está inactivo en el sistema.");
            }

            if (stockActual < cantidad) {
                throw new StockInsuficienteException("Stock insuficiente para '" + nombreRepuesto + "'. Disponible: "
                        + stockActual + ", solicitado: " + cantidad);
            }

            int stockResultante = stockActual - cantidad;

            String sqlUpdate = "UPDATE dbo.Repuestos SET stock_actual = ? WHERE repuesto_id = ?";
            try (PreparedStatement ps = cn.prepareStatement(sqlUpdate)) {
                ps.setInt(1, stockResultante);
                ps.setInt(2, repuestoId);
                ps.executeUpdate();
            }

            String sqlMov = "INSERT INTO dbo.MovimientosInventario (repuesto_id, orden_id, tipo_movimiento, cantidad, "
                          + "stock_anterior, stock_resultante, costo_unitario, motivo, usuario_registro, fecha_movimiento) "
                          + "VALUES (?, ?, 'SALIDA_ORDEN', ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = cn.prepareStatement(sqlMov)) {
                ps.setInt(1, repuestoId);
                if (ordenId > 0) {
                    ps.setInt(2, ordenId);
                } else {
                    ps.setNull(2, java.sql.Types.INTEGER);
                }
                ps.setInt(3, cantidad);
                ps.setInt(4, stockActual);
                ps.setInt(5, stockResultante);
                ps.setBigDecimal(6, costoCompra);
                ps.setString(7, ordenId > 0 ? "Consumo en orden #" + ordenId : "Salida manual");
                ps.setString(8, usuario != null ? usuario : "SISTEMA");
                ps.setTimestamp(9, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }

            if (ordenId > 0) {
                String sqlRepOrden = "INSERT INTO dbo.RepuestosOrden (orden_id, repuesto_id, nombre_historico, "
                                   + "cantidad, costo_unitario_historico, precio_unitario_historico, consumido) "
                                   + "VALUES (?, ?, ?, ?, ?, ?, 1)";
                try (PreparedStatement ps = cn.prepareStatement(sqlRepOrden)) {
                    ps.setInt(1, ordenId);
                    ps.setInt(2, repuestoId);
                    ps.setString(3, nombreRepuesto);
                    ps.setInt(4, cantidad);
                    ps.setBigDecimal(5, costoCompra);
                    ps.setBigDecimal(6, precioVenta);
                    ps.executeUpdate();
                } catch (SQLException ignored) {

                }
            }

            cn.commit();

            Repuesto r = new Repuesto();
            r.setId(repuestoId);
            r.setNombre(nombreRepuesto);
            r.setStock(stockResultante);

            return new MovimientoInventario(r, TipoMovimiento.SALIDA, cantidad, stockActual, stockResultante,
                    costoCompra, "Consumo en orden #" + ordenId, ordenId, usuario);

        } catch (SQLException e) {
            ConexionDB.rollback(cn);
            throw new PersistenciaException("Error transaccional al descontar repuesto: " + e.getMessage(), e);
        } catch (ValidacionException | StockInsuficienteException e) {
            ConexionDB.rollback(cn);
            throw e;
        } finally {
            if (cn != null) {
                try {
                    cn.setAutoCommit(originalAutoCommit);
                    cn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public synchronized MovimientoInventario registrarEntradaStock(int repuestoId, int cantidad, BigDecimal costoUnitario,
                                                                  String motivo, String usuario)
            throws ValidacionException, PersistenciaException {

        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad ingresada debe ser mayor que cero.");
        }

        Connection cn = null;
        boolean originalAutoCommit = true;

        try {
            cn = ConexionDB.getConexion();
            originalAutoCommit = cn.getAutoCommit();
            cn.setAutoCommit(false);

            String sqlSelect = "SELECT stock_actual, nombre, costo_compra FROM dbo.Repuestos WITH (UPDLOCK, HOLDLOCK) WHERE repuesto_id = ?";
            int stockActual;
            String nombre;
            BigDecimal costoActual;

            try (PreparedStatement ps = cn.prepareStatement(sqlSelect)) {
                ps.setInt(1, repuestoId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ValidacionException("El repuesto con ID " + repuestoId + " no existe.");
                    }
                    stockActual = rs.getInt("stock_actual");
                    nombre = rs.getString("nombre");
                    costoActual = rs.getBigDecimal("costo_compra");
                }
            }

            BigDecimal costoFinal = (costoUnitario != null && costoUnitario.signum() > 0) ? costoUnitario : costoActual;
            int stockResultante = stockActual + cantidad;

            String sqlUpdate = "UPDATE dbo.Repuestos SET stock_actual = ?, costo_compra = ? WHERE repuesto_id = ?";
            try (PreparedStatement ps = cn.prepareStatement(sqlUpdate)) {
                ps.setInt(1, stockResultante);
                ps.setBigDecimal(2, costoFinal);
                ps.setInt(3, repuestoId);
                ps.executeUpdate();
            }

            String sqlMov = "INSERT INTO dbo.MovimientosInventario (repuesto_id, tipo_movimiento, cantidad, "
                          + "stock_anterior, stock_resultante, costo_unitario, motivo, usuario_registro, fecha_movimiento) "
                          + "VALUES (?, 'ENTRADA', ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = cn.prepareStatement(sqlMov)) {
                ps.setInt(1, repuestoId);
                ps.setInt(2, cantidad);
                ps.setInt(3, stockActual);
                ps.setInt(4, stockResultante);
                ps.setBigDecimal(5, costoFinal);
                ps.setString(6, motivo != null ? motivo : "Reabastecimiento de compras");
                ps.setString(7, usuario != null ? usuario : "SISTEMA");
                ps.setTimestamp(8, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }

            cn.commit();

            Repuesto r = new Repuesto();
            r.setId(repuestoId);
            r.setNombre(nombre);
            r.setStock(stockResultante);

            return new MovimientoInventario(r, TipoMovimiento.ENTRADA, cantidad, stockActual, stockResultante,
                    costoFinal, motivo, null, usuario);

        } catch (SQLException e) {
            ConexionDB.rollback(cn);
            throw new PersistenciaException("Error al registrar entrada en inventario: " + e.getMessage(), e);
        } finally {
            if (cn != null) {
                try {
                    cn.setAutoCommit(originalAutoCommit);
                    cn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public synchronized MovimientoInventario registrarAjusteInventario(int repuestoId, int nuevoStock, String motivo, String usuario)
            throws ValidacionException, PersistenciaException {

        if (nuevoStock < 0) {
            throw new ValidacionException("El nuevo stock no puede ser negativo.");
        }

        Connection cn = null;
        boolean originalAutoCommit = true;

        try {
            cn = ConexionDB.getConexion();
            originalAutoCommit = cn.getAutoCommit();
            cn.setAutoCommit(false);

            String sqlSelect = "SELECT stock_actual, nombre, costo_compra FROM dbo.Repuestos WITH (UPDLOCK, HOLDLOCK) WHERE repuesto_id = ?";
            int stockActual;
            String nombre;
            BigDecimal costo;

            try (PreparedStatement ps = cn.prepareStatement(sqlSelect)) {
                ps.setInt(1, repuestoId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ValidacionException("El repuesto con ID " + repuestoId + " no existe.");
                    }
                    stockActual = rs.getInt("stock_actual");
                    nombre = rs.getString("nombre");
                    costo = rs.getBigDecimal("costo_compra");
                }
            }

            int cantidadAjuste = Math.abs(nuevoStock - stockActual);

            String sqlUpdate = "UPDATE dbo.Repuestos SET stock_actual = ? WHERE repuesto_id = ?";
            try (PreparedStatement ps = cn.prepareStatement(sqlUpdate)) {
                ps.setInt(1, nuevoStock);
                ps.setInt(2, repuestoId);
                ps.executeUpdate();
            }

            String sqlMov = "INSERT INTO dbo.MovimientosInventario (repuesto_id, tipo_movimiento, cantidad, "
                          + "stock_anterior, stock_resultante, costo_unitario, motivo, usuario_registro, fecha_movimiento) "
                          + "VALUES (?, 'AJUSTE', ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = cn.prepareStatement(sqlMov)) {
                ps.setInt(1, repuestoId);
                ps.setInt(2, cantidadAjuste);
                ps.setInt(3, stockActual);
                ps.setInt(4, nuevoStock);
                ps.setBigDecimal(5, costo);
                ps.setString(6, motivo != null ? motivo : "Ajuste manual de inventario");
                ps.setString(7, usuario != null ? usuario : "SISTEMA");
                ps.setTimestamp(8, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }

            cn.commit();

            Repuesto r = new Repuesto();
            r.setId(repuestoId);
            r.setNombre(nombre);
            r.setStock(nuevoStock);

            return new MovimientoInventario(r, TipoMovimiento.AJUSTE, cantidadAjuste, stockActual, nuevoStock,
                    costo, motivo, null, usuario);

        } catch (SQLException e) {
            ConexionDB.rollback(cn);
            throw new PersistenciaException("Error al ajustar inventario: " + e.getMessage(), e);
        } finally {
            if (cn != null) {
                try {
                    cn.setAutoCommit(originalAutoCommit);
                    cn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public List<MovimientoInventario> listarMovimientosPorRepuesto(int repuestoId) throws PersistenciaException {
        return movimientoDAO.listarPorRepuesto(repuestoId);
    }

    public List<MovimientoInventario> listarMovimientosRecientes(int limite) throws PersistenciaException {
        return movimientoDAO.listarRecientes(limite);
    }

    private void validarProveedor(Proveedor p) throws ValidacionException {
        if (p == null) {
            throw new ValidacionException("El proveedor no puede ser nulo.");
        }
        Validador.requerido(p.getRtn(), "RTN");
        Validador.requerido(p.getNombre(), "Nombre del proveedor");
        if (p.getCorreo() != null && !p.getCorreo().isBlank()) {
            Validador.correo(p.getCorreo());
        }
    }

    private void validarRepuesto(Repuesto r) throws ValidacionException {
        if (r == null) {
            throw new ValidacionException("El repuesto no puede ser nulo.");
        }
        Validador.requerido(r.getCodigo(), "Código");
        Validador.requerido(r.getNombre(), "Nombre");
        Validador.noNegativo(r.getStock(), "Stock actual");
        Validador.noNegativo(r.getStockMinimo(), "Stock mínimo");
        Validador.noNegativo(r.getCostoUnitario(), "Costo de compra");
        Validador.noNegativo(r.getPrecioVenta(), "Precio de venta");
    }
}
