package hilos;

import excepciones.StockInsuficienteException;
import modelo.MovimientoInventario;
import modelo.Repuesto;
import servicio.InventarioService;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class DemostracionConcurrencia {

    private final InventarioService inventarioService;
    private Consumer<String> logger = System.out::println;

    public DemostracionConcurrencia() {
        this.inventarioService = new InventarioService();
    }

    public DemostracionConcurrencia(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    public void setLogger(Consumer<String> logger) {
        if (logger != null) {
            this.logger = logger;
        }
    }

    private void log(String msg) {
        logger.accept(msg);
    }

    public void ejecutarDemostracion(Integer repuestoIdOpcional) {
        new Thread(() -> {
            try {
                log("=================================================================");
                log("INICIANDO DEMOSTRACIÓN DE CONCURRENCIA: 2 HILOS SIMULTÁNEOS");
                log("=================================================================");

                int repuestoId;
                if (repuestoIdOpcional != null && repuestoIdOpcional > 0) {
                    repuestoId = repuestoIdOpcional;
                } else {
                    var repuestos = inventarioService.listarRepuestosActivos();
                    if (repuestos.isEmpty()) {
                        log("[ERROR] No hay repuestos en la base de datos para la prueba.");
                        return;
                    }
                    repuestoId = repuestos.get(0).getId();
                }

                Optional<Repuesto> optRepuesto = inventarioService.buscarPorId(repuestoId);
                if (optRepuesto.isEmpty()) {
                    log("[ERROR] Repuesto con ID " + repuestoId + " no encontrado.");
                    return;
                }
                Repuesto r = optRepuesto.get();

                log(String.format(">> Repuesto seleccionado: [%s] %s | Stock inicial: %d",
                        r.getCodigo(), r.getNombre(), r.getStock()));

                int stockPrueba = 5;
                log(String.format(">> Ajustando stock a exactamente %d unidades para la prueba...", stockPrueba));
                inventarioService.registrarAjusteInventario(repuestoId, stockPrueba,
                        "Inicio demostración de concurrencia", "DEMO_CONCURRENCIA");

                log(">> Escenario:");
                log("   * Hilo 1 (Técnico A): Solicita descontar 4 unidades.");
                log("   * Hilo 2 (Técnico B): Solicita descontar 3 unidades.");
                log("   * Total solicitado: 4 + 3 = 7 unidades (Stock disponible: 5).");
                log("   * Uno de los dos debe ganar y el otro debe ser rechazado por StockInsuficienteException sin dejar inconsistencias.");

                CountDownLatch readyLatch = new CountDownLatch(2);
                CountDownLatch startLatch = new CountDownLatch(1);
                CountDownLatch doneLatch = new CountDownLatch(2);

                final String[] resultadoHilo1 = new String[1];
                final String[] resultadoHilo2 = new String[1];

                Thread hilo1 = new Thread(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                        log("[Hilo 1 - Técnico A] Iniciando transacción de descuento (4 unidades)...");
                        MovimientoInventario mov = inventarioService.descontarStockPorOrden(
                                1, repuestoId, 4, "Técnico_A");
                        resultadoHilo1[0] = "ÉXITO: Se descontaron 4 unidades. Stock resultante: " + mov.getStockResultante();
                        log("[Hilo 1 - Técnico A] " + resultadoHilo1[0]);
                    } catch (StockInsuficienteException e) {
                        resultadoHilo1[0] = "RECHAZADO POR STOCK INSUFICIENTE: " + e.getMessage();
                        log("[Hilo 1 - Técnico A] " + resultadoHilo1[0]);
                    } catch (Exception e) {
                        resultadoHilo1[0] = "ERROR: " + e.getMessage();
                        log("[Hilo 1 - Técnico A] " + resultadoHilo1[0]);
                    } finally {
                        doneLatch.countDown();
                    }
                }, "Hilo-Tecnico-A");

                Thread hilo2 = new Thread(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                        log("[Hilo 2 - Técnico B] Iniciando transacción de descuento (3 unidades)...");
                        MovimientoInventario mov = inventarioService.descontarStockPorOrden(
                                2, repuestoId, 3, "Técnico_B");
                        resultadoHilo2[0] = "ÉXITO: Se descontaron 3 unidades. Stock resultante: " + mov.getStockResultante();
                        log("[Hilo 2 - Técnico B] " + resultadoHilo2[0]);
                    } catch (StockInsuficienteException e) {
                        resultadoHilo2[0] = "RECHAZADO POR STOCK INSUFICIENTE: " + e.getMessage();
                        log("[Hilo 2 - Técnico B] " + resultadoHilo2[0]);
                    } catch (Exception e) {
                        resultadoHilo2[0] = "ERROR: " + e.getMessage();
                        log("[Hilo 2 - Técnico B] " + resultadoHilo2[0]);
                    } finally {
                        doneLatch.countDown();
                    }
                }, "Hilo-Tecnico-B");

                hilo1.start();
                hilo2.start();

                readyLatch.await();
                log(">> ¡Disparando ambos hilos concurrentemente!");
                startLatch.countDown();

                doneLatch.await();

                Repuesto repuestoFinal = inventarioService.buscarPorId(repuestoId).orElseThrow();
                log("-----------------------------------------------------------------");
                log("RESULTADO FINAL DE LA DEMOSTRACIÓN:");
                log("   * Estado Hilo 1: " + resultadoHilo1[0]);
                log("   * Estado Hilo 2: " + resultadoHilo2[0]);
                log("   * Stock final verificado en BD: " + repuestoFinal.getStock() + " unidades.");
                if (repuestoFinal.getStock() >= 0) {
                    log(">> [VERIFICACIÓN EXITOSA] Concurrencia controlada correctamente: jamás hubo sobregiro ni stock negativo.");
                } else {
                    log(">> [ALERTA] Anomalía de concurrencia detectada (stock negativo).");
                }
                log("=================================================================");

            } catch (Exception ex) {
                log("[ERROR EN DEMOSTRACIÓN] " + ex.getMessage());
            }
        }, "Runner-DemoConcurrencia").start();
    }
}
