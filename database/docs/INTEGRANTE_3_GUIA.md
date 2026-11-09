# Integrante 3 — AutoTech Manager

## Alcance incorporado

Se agregaron los siguientes componentes al proyecto original:

- DAO de consulta para OrdenTrabajo, Diagnostico, Presupuesto, ActividadTrabajo, ControlCalidad, Motor, CambioMotor, Pago, Factura y Garantia. Cada DAO ofrece listado de identificadores y conteo de registros, usando `BaseDAO` y SQL parametrizable.
- Servicios de reglas de negocio: `OrdenTrabajoService`, `PresupuestoService`, `CambioMotorService`, `PagoService` y `FacturacionService`.
- `ProcesadorOrdenWorker`, basado en `SwingWorker`, para ejecutar procesos fuera del hilo de interfaz y actualizar barra de progreso.
- Formularios Swing: `FrmOrdenTrabajo`, `FrmDiagnosticoPresupuesto`, `FrmAsignacionActividades`, `FrmControlCalidad`, `FrmCambioMotor` y `FrmPagosFacturacion`.

## Reglas incluidas

1. Una orden solo puede cambiar a estados permitidos por `EstadoOrden`.
2. Para finalizar se revisan actividades pendientes, consumos no registrados y control de calidad cuando el servicio lo exige.
3. Un presupuesto requiere detalles; el descuento no puede superar el subtotal y, si existe, debe tener autorización.
4. Un pago debe ser positivo y no puede superar el saldo pendiente.
5. El cambio de motor exige datos completos, autorización del cliente, motor disponible, fechas válidas y pruebas aprobadas.
6. La lista de once pasos del cambio Hilux está documentada en `CambioMotorService.PASOS` como flujo operativo propuesto. El SQL original solo hace referencia explícita a determinados pasos del caso, por lo que se debe contrastar con el enunciado completo del docente si existe una guía adicional.

## Abrir en NetBeans 28

1. Extraer el ZIP.
2. En NetBeans seleccionar **File > Open Project** y elegir la carpeta `AutoTech_Manager`.
3. Copiar `config.properties.example` como `config.properties` y configurar servidor, base de datos, usuario y contraseña de SQL Server.
4. Ejecutar primero `database/AutoTechDB.sql` en SQL Server Management Studio.
5. Esperar que Maven descargue `mssql-jdbc` y ejecutar el proyecto.

## Nota de integración importante

Los formularios entregados son pantallas base de captura y validación visual; sus botones no persisten todavía los datos en SQL Server. Los DAO creados para el integrante 3 incluyen consultas de conteo e identificadores, no el CRUD completo de todas las entidades. Los servicios aplican reglas de negocio sobre los objetos del modelo en memoria. Antes de presentar como sistema terminado, se debe conectar cada formulario y servicio con operaciones transaccionales CRUD y probarlos contra la base de datos del equipo.

## Verificación

El código fuente Java se compiló con `javac` usando JDK 21. No se pudo ejecutar el empaquetado Maven en este entorno porque Maven no está instalado; NetBeans puede resolver las dependencias desde `pom.xml`.
