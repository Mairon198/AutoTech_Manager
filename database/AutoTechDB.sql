/* =====================================================================
   AutoTech Manager - Base de datos AutoTechDB (SQL Server / Azure SQL)
   Programación Avanzada II - Segundo Parcial
   ---------------------------------------------------------------------
   Convención de nombres (la misma que usan los DAO del proyecto):
     - tablas en PascalCase:   dbo.Clientes, dbo.OrdenesTrabajo ...
     - columnas en snake_case:  cliente_id, stock_actual, fecha_registro ...
   ---------------------------------------------------------------------
   Cómo ejecutarlo:
   1. Conectarse a AutoTechDB con SSMS o VS Code (extensión mssql).
   2. Ejecutar este archivo completo (F5).
   ATENCIÓN: la sección 0 BORRA todas las tablas y las vuelve a crear,
   así que el script se puede ejecutar varias veces durante el desarrollo.
   ---------------------------------------------------------------------
   Contenido:
     0. Limpieza
     1. Catálogos (estados, impuestos, especialidades)
     2. Clientes y vehículos
     3. Mecánicos
     4. Proveedores, repuestos e inventario
     5. Catálogo de servicios
     6. Órdenes de trabajo y detalle
     7. Diagnóstico y presupuesto
     8. Actividades, repuestos por orden, movimientos y control de calidad
     9. Motores y cambio de motor
    10. Garantías, pagos y facturas
    11. Auditoría
    12. Funciones
    13. Triggers
    14. Procedimientos almacenados
    15. Vistas administrativas
    16. Datos de prueba
    17. Consultas de ejemplo (JOIN, GROUP BY, subconsultas)
    18. Usuarios del sistema (inicio de sesión)
   ===================================================================== */


/* =====================================================================
   0. LIMPIEZA (orden inverso a las dependencias)
   ===================================================================== */
DROP VIEW IF EXISTS dbo.vw_HistorialVehiculo;
DROP VIEW IF EXISTS dbo.vw_OrdenesResumen;
DROP VIEW IF EXISTS dbo.vw_StockBajo;
DROP VIEW IF EXISTS dbo.vw_SaldosFacturas;
DROP VIEW IF EXISTS dbo.vw_ProductividadMecanicos;
DROP VIEW IF EXISTS dbo.vw_ServiciosMasSolicitados;
DROP VIEW IF EXISTS dbo.vw_InventarioBajoMinimo;      -- nombre anterior
DROP PROCEDURE IF EXISTS dbo.sp_ConsumirRepuesto;
DROP PROCEDURE IF EXISTS dbo.sp_RegistrarEntradaInventario;
DROP PROCEDURE IF EXISTS dbo.sp_AsignarMecanico;
DROP PROCEDURE IF EXISTS dbo.sp_CambiarEstadoOrden;
DROP PROCEDURE IF EXISTS dbo.sp_RegistrarCambioMotor;
DROP PROCEDURE IF EXISTS dbo.sp_RegistrarPago;
DROP PROCEDURE IF EXISTS dbo.sp_GenerarFactura;
DROP PROCEDURE IF EXISTS dbo.sp_HistorialVehiculo;
DROP FUNCTION IF EXISTS dbo.fn_TotalesOrden;
DROP FUNCTION IF EXISTS dbo.fn_MotorCompatible;

DROP TABLE IF EXISTS dbo.Auditoria;
DROP TABLE IF EXISTS dbo.Facturas;
DROP TABLE IF EXISTS dbo.Pagos;
DROP TABLE IF EXISTS dbo.Garantias;
DROP TABLE IF EXISTS dbo.CambiosMotor;
DROP TABLE IF EXISTS dbo.CompatibilidadMotor;
DROP TABLE IF EXISTS dbo.Motores;
DROP TABLE IF EXISTS dbo.ControlesCalidad;
DROP TABLE IF EXISTS dbo.MovimientosInventario;
DROP TABLE IF EXISTS dbo.RepuestosOrden;
DROP TABLE IF EXISTS dbo.ConsumoRepuestos;              -- nombre anterior
DROP TABLE IF EXISTS dbo.ActividadesTrabajo;
DROP TABLE IF EXISTS dbo.DetallePresupuesto;
DROP TABLE IF EXISTS dbo.Presupuestos;
DROP TABLE IF EXISTS dbo.EvidenciasTecnicas;
DROP TABLE IF EXISTS dbo.Diagnosticos;
DROP TABLE IF EXISTS dbo.AsignacionMecanicos;
DROP TABLE IF EXISTS dbo.DetalleOrden;
DROP TABLE IF EXISTS dbo.OrdenesTrabajo;
DROP TABLE IF EXISTS dbo.ServicioRepuestos;
DROP TABLE IF EXISTS dbo.Servicios;
DROP TABLE IF EXISTS dbo.Repuestos;
DROP TABLE IF EXISTS dbo.Proveedores;
DROP TABLE IF EXISTS dbo.MecanicoEspecialidades;
DROP TABLE IF EXISTS dbo.Mecanicos;
DROP TABLE IF EXISTS dbo.Especialidades;
DROP TABLE IF EXISTS dbo.Vehiculos;
DROP TABLE IF EXISTS dbo.Clientes;
DROP TABLE IF EXISTS dbo.TransicionesEstadoOrden;
DROP TABLE IF EXISTS dbo.EstadosOrden;
DROP TABLE IF EXISTS dbo.ConfiguracionImpuestos;
GO


/* =====================================================================
   1. CATÁLOGOS
   ===================================================================== */

-- Estados posibles de una orden (mismo flujo que el enum EstadoOrden de Java)
CREATE TABLE dbo.EstadosOrden (
    codigo       VARCHAR(25)  NOT NULL CONSTRAINT PK_EstadosOrden PRIMARY KEY,
    descripcion  VARCHAR(50)  NOT NULL,
    secuencia    TINYINT      NOT NULL,
    es_final     BIT          NOT NULL CONSTRAINT DF_EstadosOrden_EsFinal DEFAULT 0
);
GO

-- Cambios de estado permitidos (tabla puente estado -> estado)
CREATE TABLE dbo.TransicionesEstadoOrden (
    estado_actual     VARCHAR(25) NOT NULL CONSTRAINT FK_Trans_Actual    REFERENCES dbo.EstadosOrden(codigo),
    estado_siguiente  VARCHAR(25) NOT NULL CONSTRAINT FK_Trans_Siguiente REFERENCES dbo.EstadosOrden(codigo),
    CONSTRAINT PK_TransicionesEstadoOrden PRIMARY KEY (estado_actual, estado_siguiente),
    CONSTRAINT CK_Trans_Distinto CHECK (estado_actual <> estado_siguiente)
);
GO

-- Impuestos configurables (ej.: ISV 15 %). Solo uno puede ser el predeterminado.
CREATE TABLE dbo.ConfiguracionImpuestos (
    impuesto_id        INT IDENTITY(1,1) CONSTRAINT PK_ConfiguracionImpuestos PRIMARY KEY,
    nombre             VARCHAR(50)   NOT NULL CONSTRAINT UQ_Impuestos_Nombre UNIQUE,
    porcentaje         DECIMAL(5,2)  NOT NULL CONSTRAINT CK_Impuestos_Porcentaje CHECK (porcentaje BETWEEN 0 AND 100),
    es_predeterminado  BIT           NOT NULL CONSTRAINT DF_Impuestos_Predet DEFAULT 0,
    activo             BIT           NOT NULL CONSTRAINT DF_Impuestos_Activo DEFAULT 1
);
GO
CREATE UNIQUE INDEX UX_Impuestos_Predeterminado ON dbo.ConfiguracionImpuestos(es_predeterminado) WHERE es_predeterminado = 1;
GO

CREATE TABLE dbo.Especialidades (
    especialidad_id  INT IDENTITY(1,1) CONSTRAINT PK_Especialidades PRIMARY KEY,
    nombre           VARCHAR(60)   NOT NULL CONSTRAINT UQ_Especialidades_Nombre UNIQUE,
    descripcion      VARCHAR(200)  NULL,
    activo           BIT           NOT NULL CONSTRAINT DF_Especialidades_Activo DEFAULT 1
);
GO


/* =====================================================================
   2. CLIENTES Y VEHÍCULOS (Módulo 1)
   ===================================================================== */
CREATE TABLE dbo.Clientes (
    cliente_id      INT IDENTITY(1,1) CONSTRAINT PK_Clientes PRIMARY KEY,
    identificacion  VARCHAR(20)   NOT NULL CONSTRAINT UQ_Clientes_Identificacion UNIQUE,
    nombre          VARCHAR(150)  NOT NULL,            -- nombre completo
    telefono        VARCHAR(20)   NULL,
    correo          VARCHAR(120)  NULL,
    direccion       VARCHAR(200)  NULL,
    tipo_cliente    VARCHAR(15)   NOT NULL CONSTRAINT DF_Clientes_Tipo   DEFAULT 'PARTICULAR',
    activo          BIT           NOT NULL CONSTRAINT DF_Clientes_Activo DEFAULT 1,
    fecha_registro  DATETIME2(0)  NOT NULL CONSTRAINT DF_Clientes_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Clientes_Nombre CHECK (LEN(LTRIM(nombre)) > 0),
    CONSTRAINT CK_Clientes_Correo CHECK (correo IS NULL OR correo = '' OR correo LIKE '%_@_%._%'),
    CONSTRAINT CK_Clientes_Tipo   CHECK (tipo_cliente IN ('PARTICULAR','CORPORATIVO'))
);
GO

CREATE TABLE dbo.Vehiculos (
    vehiculo_id         INT IDENTITY(1,1) CONSTRAINT PK_Vehiculos PRIMARY KEY,
    cliente_id          INT           NOT NULL CONSTRAINT FK_Vehiculos_Clientes REFERENCES dbo.Clientes(cliente_id),
    placa               VARCHAR(10)   NOT NULL CONSTRAINT UQ_Vehiculos_Placa UNIQUE,
    vin                 CHAR(17)      NOT NULL CONSTRAINT UQ_Vehiculos_VIN   UNIQUE,
    marca               VARCHAR(40)   NOT NULL,
    modelo              VARCHAR(40)   NOT NULL,
    anio                SMALLINT      NOT NULL,
    color               VARCHAR(30)   NULL,
    kilometraje         INT           NOT NULL CONSTRAINT DF_Vehiculos_Km DEFAULT 0,
    combustible         VARCHAR(15)   NOT NULL,
    serie_motor_actual  VARCHAR(30)   NULL,
    activo              BIT           NOT NULL CONSTRAINT DF_Vehiculos_Activo DEFAULT 1,
    fecha_registro      DATETIME2(0)  NOT NULL CONSTRAINT DF_Vehiculos_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Vehiculos_Placa       CHECK (placa LIKE '[A-Z][A-Z][A-Z] [0-9][0-9][0-9][0-9]'),
    CONSTRAINT CK_Vehiculos_VIN         CHECK (LEN(vin) = 17 AND vin NOT LIKE '%[IOQ]%'),
    CONSTRAINT CK_Vehiculos_Anio        CHECK (anio BETWEEN 1950 AND 2100),
    CONSTRAINT CK_Vehiculos_Km          CHECK (kilometraje >= 0),
    CONSTRAINT CK_Vehiculos_Combustible CHECK (combustible IN ('GASOLINA','DIESEL','HIBRIDO','ELECTRICO','GLP')),
    -- Permite que OrdenesTrabajo verifique que el vehículo pertenece al cliente
    CONSTRAINT UQ_Vehiculos_Cliente UNIQUE (vehiculo_id, cliente_id)
);
GO
CREATE INDEX IX_Vehiculos_Cliente ON dbo.Vehiculos(cliente_id);
GO


/* =====================================================================
   3. MECÁNICOS (Módulo 4)
   ===================================================================== */
CREATE TABLE dbo.Mecanicos (
    mecanico_id          INT IDENTITY(1,1) CONSTRAINT PK_Mecanicos PRIMARY KEY,
    codigo_empleado      VARCHAR(10)   NOT NULL CONSTRAINT UQ_Mecanicos_Codigo         UNIQUE,
    identificacion       VARCHAR(20)   NOT NULL CONSTRAINT UQ_Mecanicos_Identificacion UNIQUE,
    nombre               VARCHAR(150)  NOT NULL,       -- nombre completo
    telefono             VARCHAR(20)   NULL,
    correo               VARCHAR(120)  NULL,
    nivel_certificacion  VARCHAR(10)   NOT NULL CONSTRAINT DF_Mecanicos_Nivel  DEFAULT 'SENIOR',
    tarifa_hora          DECIMAL(10,2) NOT NULL CONSTRAINT DF_Mecanicos_Tarifa DEFAULT 0,
    disponible           BIT           NOT NULL CONSTRAINT DF_Mecanicos_Disp   DEFAULT 1,
    activo               BIT           NOT NULL CONSTRAINT DF_Mecanicos_Activo DEFAULT 1,
    fecha_ingreso        DATETIME2(0)  NOT NULL CONSTRAINT DF_Mecanicos_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Mecanicos_Tarifa CHECK (tarifa_hora >= 0),
    CONSTRAINT CK_Mecanicos_Nivel  CHECK (nivel_certificacion IN ('JUNIOR','SENIOR','MASTER'))
);
GO

-- Tabla puente: un mecánico tiene varias especialidades
CREATE TABLE dbo.MecanicoEspecialidades (
    mecanico_id      INT NOT NULL CONSTRAINT FK_MecEsp_Mecanico     REFERENCES dbo.Mecanicos(mecanico_id),
    especialidad_id  INT NOT NULL CONSTRAINT FK_MecEsp_Especialidad REFERENCES dbo.Especialidades(especialidad_id),
    CONSTRAINT PK_MecanicoEspecialidades PRIMARY KEY (mecanico_id, especialidad_id)
);
GO


/* =====================================================================
   4. PROVEEDORES Y REPUESTOS (Módulo 5)
   ===================================================================== */
CREATE TABLE dbo.Proveedores (
    proveedor_id    INT IDENTITY(1,1) CONSTRAINT PK_Proveedores PRIMARY KEY,
    rtn             VARCHAR(20)   NOT NULL CONSTRAINT UQ_Proveedores_RTN UNIQUE,
    nombre          VARCHAR(100)  NOT NULL,
    contacto        VARCHAR(80)   NULL,
    telefono        VARCHAR(20)   NULL,
    correo          VARCHAR(120)  NULL,
    direccion       VARCHAR(200)  NULL,
    activo          BIT           NOT NULL CONSTRAINT DF_Proveedores_Activo DEFAULT 1,
    fecha_registro  DATETIME2(0)  NOT NULL CONSTRAINT DF_Proveedores_Fecha  DEFAULT SYSDATETIME()
);
GO

CREATE TABLE dbo.Repuestos (
    repuesto_id     INT IDENTITY(1,1) CONSTRAINT PK_Repuestos PRIMARY KEY,
    codigo          VARCHAR(20)   NOT NULL CONSTRAINT UQ_Repuestos_Codigo UNIQUE,
    nombre          VARCHAR(100)  NOT NULL,
    categoria       VARCHAR(15)   NOT NULL,
    marca           VARCHAR(40)   NULL,
    proveedor_id    INT           NULL CONSTRAINT FK_Repuestos_Proveedores REFERENCES dbo.Proveedores(proveedor_id),
    stock_actual    INT           NOT NULL CONSTRAINT DF_Repuestos_Stock    DEFAULT 0,
    stock_minimo    INT           NOT NULL CONSTRAINT DF_Repuestos_StockMin DEFAULT 0,
    costo_compra    DECIMAL(12,2) NOT NULL CONSTRAINT DF_Repuestos_Costo    DEFAULT 0,
    precio_venta    DECIMAL(12,2) NOT NULL CONSTRAINT DF_Repuestos_Precio   DEFAULT 0,
    ubicacion       VARCHAR(40)   NULL,
    activo          BIT           NOT NULL CONSTRAINT DF_Repuestos_Activo DEFAULT 1,
    fecha_registro  DATETIME2(0)  NOT NULL CONSTRAINT DF_Repuestos_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Repuestos_Stock     CHECK (stock_actual >= 0),          -- nunca stock negativo
    CONSTRAINT CK_Repuestos_StockMin  CHECK (stock_minimo >= 0),
    CONSTRAINT CK_Repuestos_Costo     CHECK (costo_compra >= 0),
    CONSTRAINT CK_Repuestos_Precio    CHECK (precio_venta >= 0),
    CONSTRAINT CK_Repuestos_Categoria CHECK (categoria IN ('ACEITE','FILTRO','BUJIA','BATERIA','FRENOS','RADIADOR',
                                                          'TRANSMISION','MOTOR','ELECTRICO','SUSPENSION','OTRO'))
);
GO


/* =====================================================================
   5. CATÁLOGO DE SERVICIOS (Módulo 2)
   ===================================================================== */
CREATE TABLE dbo.Servicios (
    servicio_id            INT IDENTITY(1,1) CONSTRAINT PK_Servicios PRIMARY KEY,
    codigo                 VARCHAR(10)   NOT NULL CONSTRAINT UQ_Servicios_Codigo UNIQUE,
    nombre                 VARCHAR(100)  NOT NULL,
    descripcion            VARCHAR(300)  NULL,
    nivel_complejidad      TINYINT       NOT NULL,   -- 1 básico ... 5 reparación mayor
    categoria_complejidad  VARCHAR(40)   NULL,       -- texto del nivel (ej.: "Mantenimiento básico")
    costo_mano_obra        DECIMAL(12,2) NOT NULL,   -- costo vigente (el histórico queda en DetalleOrden)
    tiempo_estimado_horas  DECIMAL(6,2)  NOT NULL,
    activo                 BIT           NOT NULL CONSTRAINT DF_Servicios_Activo DEFAULT 1,
    fecha_registro         DATETIME2(0)  NOT NULL CONSTRAINT DF_Servicios_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Servicios_Nivel  CHECK (nivel_complejidad BETWEEN 1 AND 5),
    CONSTRAINT CK_Servicios_Costo  CHECK (costo_mano_obra >= 0),
    CONSTRAINT CK_Servicios_Tiempo CHECK (tiempo_estimado_horas > 0)
);
GO

-- Tabla puente: repuestos asociados a cada servicio
CREATE TABLE dbo.ServicioRepuestos (
    servicio_id        INT NOT NULL CONSTRAINT FK_ServRep_Servicio REFERENCES dbo.Servicios(servicio_id),
    repuesto_id        INT NOT NULL CONSTRAINT FK_ServRep_Repuesto REFERENCES dbo.Repuestos(repuesto_id),
    cantidad_sugerida  INT NOT NULL CONSTRAINT DF_ServRep_Cantidad DEFAULT 1,
    CONSTRAINT PK_ServicioRepuestos PRIMARY KEY (servicio_id, repuesto_id),
    CONSTRAINT CK_ServRep_Cantidad CHECK (cantidad_sugerida > 0)
);
GO


/* =====================================================================
   6. ÓRDENES DE TRABAJO (Módulo 3)
   ===================================================================== */
CREATE TABLE dbo.OrdenesTrabajo (
    orden_id                INT IDENTITY(1,1) CONSTRAINT PK_OrdenesTrabajo PRIMARY KEY,
    -- Número único y legible, calculado a partir del id: OT-000001
    numero_orden            AS ('OT-' + RIGHT('000000' + CAST(orden_id AS VARCHAR(10)), 6)) PERSISTED,
    cliente_id              INT           NOT NULL,
    vehiculo_id             INT           NOT NULL,
    fecha_ingreso           DATETIME2(0)  NOT NULL CONSTRAINT DF_Ordenes_FechaIngreso DEFAULT SYSDATETIME(),
    kilometraje_ingreso     INT           NOT NULL,
    sintomas                VARCHAR(500)  NOT NULL,
    diagnostico_preliminar  VARCHAR(500)  NULL,
    estado                  VARCHAR(25)   NOT NULL CONSTRAINT DF_Ordenes_Estado DEFAULT 'RECIBIDA'
                                          CONSTRAINT FK_Ordenes_Estado REFERENCES dbo.EstadosOrden(codigo),
    fecha_entrega           DATETIME2(0)  NULL,
    activo                  BIT           NOT NULL CONSTRAINT DF_Ordenes_Activo DEFAULT 1,
    fecha_registro          DATETIME2(0)  NOT NULL CONSTRAINT DF_Ordenes_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT UQ_Ordenes_Numero UNIQUE (numero_orden),
    CONSTRAINT FK_Ordenes_Clientes FOREIGN KEY (cliente_id) REFERENCES dbo.Clientes(cliente_id),
    -- El vehículo debe pertenecer al cliente de la orden
    CONSTRAINT FK_Ordenes_VehiculoCliente FOREIGN KEY (vehiculo_id, cliente_id) REFERENCES dbo.Vehiculos(vehiculo_id, cliente_id),
    CONSTRAINT CK_Ordenes_Km        CHECK (kilometraje_ingreso >= 0),
    CONSTRAINT CK_Ordenes_Sintomas  CHECK (LEN(LTRIM(sintomas)) > 0),
    CONSTRAINT CK_Ordenes_Entrega   CHECK (fecha_entrega IS NULL OR fecha_entrega >= fecha_ingreso)
);
GO
CREATE INDEX IX_Ordenes_Vehiculo ON dbo.OrdenesTrabajo(vehiculo_id);
CREATE INDEX IX_Ordenes_Estado   ON dbo.OrdenesTrabajo(estado);
GO

-- Servicios por orden. precio_unitario guarda el precio del momento (histórico)
CREATE TABLE dbo.DetalleOrden (
    detalle_id       INT IDENTITY(1,1) CONSTRAINT PK_DetalleOrden PRIMARY KEY,
    orden_id         INT           NOT NULL CONSTRAINT FK_DetalleOrden_Orden    REFERENCES dbo.OrdenesTrabajo(orden_id),
    servicio_id      INT           NOT NULL CONSTRAINT FK_DetalleOrden_Servicio REFERENCES dbo.Servicios(servicio_id),
    cantidad         INT           NOT NULL CONSTRAINT DF_DetalleOrden_Cantidad DEFAULT 1,
    precio_unitario  DECIMAL(12,2) NOT NULL,
    observacion      VARCHAR(200)  NULL,
    CONSTRAINT UQ_DetalleOrden UNIQUE (orden_id, servicio_id),
    CONSTRAINT CK_DetalleOrden_Cantidad CHECK (cantidad > 0),
    CONSTRAINT CK_DetalleOrden_Precio   CHECK (precio_unitario >= 0)
);
GO

-- Tabla puente orden - mecánico. Solo un responsable principal por orden.
CREATE TABLE dbo.AsignacionMecanicos (
    orden_id          INT          NOT NULL CONSTRAINT FK_Asignacion_Orden    REFERENCES dbo.OrdenesTrabajo(orden_id),
    mecanico_id       INT          NOT NULL CONSTRAINT FK_Asignacion_Mecanico REFERENCES dbo.Mecanicos(mecanico_id),
    es_responsable    BIT          NOT NULL CONSTRAINT DF_Asignacion_Resp  DEFAULT 0,
    fecha_asignacion  DATETIME2(0) NOT NULL CONSTRAINT DF_Asignacion_Fecha DEFAULT SYSDATETIME(),
    CONSTRAINT PK_AsignacionMecanicos PRIMARY KEY (orden_id, mecanico_id)
);
GO
CREATE UNIQUE INDEX UX_Asignacion_UnResponsable ON dbo.AsignacionMecanicos(orden_id) WHERE es_responsable = 1;
GO


/* =====================================================================
   7. DIAGNÓSTICO Y PRESUPUESTO (Módulo 6)
   ===================================================================== */
CREATE TABLE dbo.Diagnosticos (
    diagnostico_id         INT IDENTITY(1,1) CONSTRAINT PK_Diagnosticos PRIMARY KEY,
    orden_id               INT           NOT NULL CONSTRAINT FK_Diagnosticos_Orden    REFERENCES dbo.OrdenesTrabajo(orden_id),
    mecanico_id            INT           NOT NULL CONSTRAINT FK_Diagnosticos_Mecanico REFERENCES dbo.Mecanicos(mecanico_id),
    fecha                  DATETIME2(0)  NOT NULL CONSTRAINT DF_Diagnosticos_Fecha DEFAULT SYSDATETIME(),
    descripcion            VARCHAR(1000) NOT NULL,
    causa_raiz             VARCHAR(500)  NULL,
    recomendacion          VARCHAR(500)  NULL,
    requiere_cambio_motor  BIT           NOT NULL CONSTRAINT DF_Diagnosticos_Motor DEFAULT 0
);
GO

CREATE TABLE dbo.EvidenciasTecnicas (
    evidencia_id    INT IDENTITY(1,1) CONSTRAINT PK_EvidenciasTecnicas PRIMARY KEY,
    diagnostico_id  INT           NOT NULL CONSTRAINT FK_Evidencias_Diagnostico REFERENCES dbo.Diagnosticos(diagnostico_id),
    tipo            VARCHAR(40)   NOT NULL,   -- Compresión, Temperatura, Escáner, Fotografía...
    descripcion     VARCHAR(500)  NOT NULL,
    valor_medido    VARCHAR(100)  NULL,
    ruta_archivo    VARCHAR(260)  NULL
);
GO

CREATE TABLE dbo.Presupuestos (
    presupuesto_id            INT IDENTITY(1,1) CONSTRAINT PK_Presupuestos PRIMARY KEY,
    orden_id                  INT           NOT NULL CONSTRAINT FK_Presupuestos_Orden    REFERENCES dbo.OrdenesTrabajo(orden_id),
    fecha                     DATETIME2(0)  NOT NULL CONSTRAINT DF_Presupuestos_Fecha DEFAULT SYSDATETIME(),
    subtotal                  DECIMAL(12,2) NOT NULL CONSTRAINT DF_Presupuestos_Subtotal DEFAULT 0,
    descuento                 DECIMAL(12,2) NOT NULL CONSTRAINT DF_Presupuestos_Desc     DEFAULT 0,
    autorizado_descuento_por  VARCHAR(80)   NULL,
    impuesto_id               INT           NULL CONSTRAINT FK_Presupuestos_Impuesto REFERENCES dbo.ConfiguracionImpuestos(impuesto_id),
    monto_impuesto            DECIMAL(12,2) NOT NULL CONSTRAINT DF_Presupuestos_Imp      DEFAULT 0,
    total                     DECIMAL(12,2) NOT NULL CONSTRAINT DF_Presupuestos_Total    DEFAULT 0,
    estado                    VARCHAR(10)   NOT NULL CONSTRAINT DF_Presupuestos_Estado   DEFAULT 'PENDIENTE',
    fecha_respuesta           DATETIME2(0)  NULL,
    observacion_cliente       VARCHAR(300)  NULL,
    CONSTRAINT CK_Presupuestos_Montos   CHECK (subtotal >= 0 AND descuento >= 0 AND monto_impuesto >= 0 AND total >= 0),
    CONSTRAINT CK_Presupuestos_Desc     CHECK (descuento <= subtotal),
    -- Un descuento solo es válido si alguien lo autorizó
    CONSTRAINT CK_Presupuestos_DescAut  CHECK (descuento = 0 OR autorizado_descuento_por IS NOT NULL),
    CONSTRAINT CK_Presupuestos_Estado   CHECK (estado IN ('PENDIENTE','APROBADO','RECHAZADO')),
    CONSTRAINT CK_Presupuestos_Resp     CHECK (estado = 'PENDIENTE' OR fecha_respuesta IS NOT NULL)
);
GO

-- Desglose del presupuesto: mano de obra (servicio + horas) o repuestos
CREATE TABLE dbo.DetallePresupuesto (
    detalle_presupuesto_id  INT IDENTITY(1,1) CONSTRAINT PK_DetallePresupuesto PRIMARY KEY,
    presupuesto_id          INT           NOT NULL CONSTRAINT FK_DetPres_Presupuesto REFERENCES dbo.Presupuestos(presupuesto_id),
    tipo_linea              VARCHAR(10)   NOT NULL,
    servicio_id             INT           NULL CONSTRAINT FK_DetPres_Servicio REFERENCES dbo.Servicios(servicio_id),
    repuesto_id             INT           NULL CONSTRAINT FK_DetPres_Repuesto REFERENCES dbo.Repuestos(repuesto_id),
    descripcion             VARCHAR(200)  NOT NULL,
    horas                   DECIMAL(6,2)  NOT NULL CONSTRAINT DF_DetPres_Horas    DEFAULT 0,
    cantidad                INT           NOT NULL CONSTRAINT DF_DetPres_Cantidad DEFAULT 1,
    precio_unitario         DECIMAL(12,2) NOT NULL,
    CONSTRAINT CK_DetPres_Tipo     CHECK (tipo_linea IN ('MANO_OBRA','REPUESTO')),
    CONSTRAINT CK_DetPres_Ref      CHECK ((tipo_linea = 'MANO_OBRA' AND servicio_id IS NOT NULL AND repuesto_id IS NULL)
                                       OR (tipo_linea = 'REPUESTO'  AND repuesto_id IS NOT NULL AND servicio_id IS NULL)),
    CONSTRAINT CK_DetPres_Valores  CHECK (horas >= 0 AND cantidad > 0 AND precio_unitario >= 0)
);
GO


/* =====================================================================
   8. ACTIVIDADES, REPUESTOS POR ORDEN, MOVIMIENTOS Y CONTROL DE CALIDAD
   ===================================================================== */
CREATE TABLE dbo.ActividadesTrabajo (
    actividad_id   INT IDENTITY(1,1) CONSTRAINT PK_ActividadesTrabajo PRIMARY KEY,
    orden_id       INT           NOT NULL CONSTRAINT FK_Actividades_Orden    REFERENCES dbo.OrdenesTrabajo(orden_id),
    mecanico_id    INT           NOT NULL CONSTRAINT FK_Actividades_Mecanico REFERENCES dbo.Mecanicos(mecanico_id),
    descripcion    VARCHAR(300)  NOT NULL,
    horas          DECIMAL(6,2)  NOT NULL CONSTRAINT DF_Actividades_Horas  DEFAULT 0,
    estado         VARCHAR(12)   NOT NULL CONSTRAINT DF_Actividades_Estado DEFAULT 'PENDIENTE',
    inicio         DATETIME2(0)  NULL,
    fin            DATETIME2(0)  NULL,
    observaciones  VARCHAR(300)  NULL,
    CONSTRAINT CK_Actividades_Horas  CHECK (horas >= 0),
    CONSTRAINT CK_Actividades_Estado CHECK (estado IN ('PENDIENTE','EN_PROCESO','COMPLETADA')),
    CONSTRAINT CK_Actividades_Fechas CHECK (fin IS NULL OR (inicio IS NOT NULL AND fin >= inicio))
);
GO

-- Tabla puente orden - repuesto (repuestos usados en la orden).
-- Guarda nombre, costo y precio del momento (históricos). consumido = 1 cuando ya se descontó del inventario.
CREATE TABLE dbo.RepuestosOrden (
    repuesto_orden_id          INT IDENTITY(1,1) CONSTRAINT PK_RepuestosOrden PRIMARY KEY,
    orden_id                   INT           NOT NULL CONSTRAINT FK_RepOrden_Orden    REFERENCES dbo.OrdenesTrabajo(orden_id),
    repuesto_id                INT           NOT NULL CONSTRAINT FK_RepOrden_Repuesto REFERENCES dbo.Repuestos(repuesto_id),
    nombre_historico           VARCHAR(100)  NOT NULL,
    cantidad                   INT           NOT NULL,
    costo_unitario_historico   DECIMAL(12,2) NOT NULL CONSTRAINT DF_RepOrden_Costo DEFAULT 0,
    precio_unitario_historico  DECIMAL(12,2) NOT NULL,
    consumido                  BIT           NOT NULL CONSTRAINT DF_RepOrden_Consumido DEFAULT 0,
    fecha                      DATETIME2(0)  NOT NULL CONSTRAINT DF_RepOrden_Fecha DEFAULT SYSDATETIME(),
    CONSTRAINT CK_RepOrden_Cantidad CHECK (cantidad > 0),
    CONSTRAINT CK_RepOrden_Montos   CHECK (costo_unitario_historico >= 0 AND precio_unitario_historico >= 0)
);
GO
CREATE INDEX IX_RepOrden_Orden ON dbo.RepuestosOrden(orden_id);
GO

-- Kardex: cada entrada, salida o ajuste con el stock antes y después
CREATE TABLE dbo.MovimientosInventario (
    movimiento_id     INT IDENTITY(1,1) CONSTRAINT PK_MovimientosInventario PRIMARY KEY,
    repuesto_id       INT              NOT NULL CONSTRAINT FK_Movimientos_Repuesto REFERENCES dbo.Repuestos(repuesto_id),
    orden_id          INT              NULL CONSTRAINT FK_Movimientos_Orden REFERENCES dbo.OrdenesTrabajo(orden_id),
    tipo_movimiento   VARCHAR(15)      NOT NULL,
    cantidad          INT              NOT NULL,
    stock_anterior    INT              NOT NULL,
    stock_resultante  INT              NOT NULL,
    costo_unitario    DECIMAL(12,2)    NOT NULL CONSTRAINT DF_Movimientos_Costo DEFAULT 0,
    motivo            VARCHAR(200)     NULL,
    usuario_registro  VARCHAR(60)      NOT NULL CONSTRAINT DF_Movimientos_Usuario DEFAULT SUSER_SNAME(),
    fecha_movimiento  DATETIME2(0)     NOT NULL CONSTRAINT DF_Movimientos_Fecha DEFAULT SYSDATETIME(),
    -- Clave única opcional por operación: si llega dos veces, no se descuenta dos veces
    clave_operacion   UNIQUEIDENTIFIER NULL,
    CONSTRAINT CK_Movimientos_Tipo     CHECK (tipo_movimiento IN ('ENTRADA','SALIDA_ORDEN','AJUSTE')),
    CONSTRAINT CK_Movimientos_Cantidad CHECK ((tipo_movimiento IN ('ENTRADA','SALIDA_ORDEN') AND cantidad > 0)
                                           OR (tipo_movimiento = 'AJUSTE' AND cantidad >= 0)),
    CONSTRAINT CK_Movimientos_Stock    CHECK (stock_anterior >= 0 AND stock_resultante >= 0),
    -- El kardex debe cuadrar: entrada suma, salida resta
    CONSTRAINT CK_Movimientos_Cuadre   CHECK ((tipo_movimiento = 'ENTRADA'      AND stock_resultante = stock_anterior + cantidad)
                                           OR (tipo_movimiento = 'SALIDA_ORDEN' AND stock_resultante = stock_anterior - cantidad)
                                           OR  tipo_movimiento = 'AJUSTE'),
    CONSTRAINT CK_Movimientos_Costo    CHECK (costo_unitario >= 0)
);
GO
CREATE UNIQUE INDEX UX_Movimientos_Clave ON dbo.MovimientosInventario(clave_operacion) WHERE clave_operacion IS NOT NULL;
CREATE INDEX IX_Movimientos_Repuesto ON dbo.MovimientosInventario(repuesto_id, fecha_movimiento);
GO

CREATE TABLE dbo.ControlesCalidad (
    control_id     INT IDENTITY(1,1) CONSTRAINT PK_ControlesCalidad PRIMARY KEY,
    orden_id       INT           NOT NULL CONSTRAINT FK_Controles_Orden     REFERENCES dbo.OrdenesTrabajo(orden_id),
    inspector_id   INT           NOT NULL CONSTRAINT FK_Controles_Inspector REFERENCES dbo.Mecanicos(mecanico_id),
    fecha          DATETIME2(0)  NOT NULL CONSTRAINT DF_Controles_Fecha DEFAULT SYSDATETIME(),
    aprobado       BIT           NOT NULL,
    checklist      VARCHAR(1000) NULL,
    observaciones  VARCHAR(500)  NULL,
    -- Si se rechaza, se debe explicar por qué
    CONSTRAINT CK_Controles_Obs CHECK (aprobado = 1 OR observaciones IS NOT NULL)
);
GO


/* =====================================================================
   9. MOTORES Y CAMBIO DE MOTOR (Módulo 7)
   ===================================================================== */
CREATE TABLE dbo.Motores (
    motor_id        INT IDENTITY(1,1) CONSTRAINT PK_Motores PRIMARY KEY,
    numero_serie    VARCHAR(30)   NOT NULL CONSTRAINT UQ_Motores_Serie UNIQUE,
    marca           VARCHAR(40)   NOT NULL,
    modelo_motor    VARCHAR(40)   NOT NULL,
    cilindrada      VARCHAR(10)   NOT NULL,
    combustible     VARCHAR(15)   NOT NULL,
    estado          VARCHAR(12)   NOT NULL CONSTRAINT DF_Motores_Estado DEFAULT 'DISPONIBLE',
    es_nuevo        BIT           NOT NULL CONSTRAINT DF_Motores_Nuevo  DEFAULT 1,
    costo           DECIMAL(12,2) NOT NULL CONSTRAINT DF_Motores_Costo  DEFAULT 0,
    repuesto_id     INT           NULL CONSTRAINT FK_Motores_Repuesto  REFERENCES dbo.Repuestos(repuesto_id),
    proveedor_id    INT           NULL CONSTRAINT FK_Motores_Proveedor REFERENCES dbo.Proveedores(proveedor_id),
    activo          BIT           NOT NULL CONSTRAINT DF_Motores_Activo DEFAULT 1,
    fecha_registro  DATETIME2(0)  NOT NULL CONSTRAINT DF_Motores_Fecha  DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Motores_Estado      CHECK (estado IN ('DISPONIBLE','RESERVADO','INSTALADO','RETIRADO')),
    CONSTRAINT CK_Motores_Combustible CHECK (combustible IN ('GASOLINA','DIESEL','HIBRIDO','ELECTRICO','GLP')),
    CONSTRAINT CK_Motores_Costo       CHECK (costo >= 0)
);
GO

-- Reglas de compatibilidad: el motor sirve para marca + modelo + rango de años
CREATE TABLE dbo.CompatibilidadMotor (
    compatibilidad_id  INT IDENTITY(1,1) CONSTRAINT PK_CompatibilidadMotor PRIMARY KEY,
    motor_id           INT          NOT NULL CONSTRAINT FK_Compat_Motor REFERENCES dbo.Motores(motor_id),
    marca              VARCHAR(40)  NOT NULL,
    modelo             VARCHAR(40)  NOT NULL,
    anio_desde         SMALLINT     NOT NULL,
    anio_hasta         SMALLINT     NOT NULL,
    CONSTRAINT UQ_Compat UNIQUE (motor_id, marca, modelo, anio_desde),
    CONSTRAINT CK_Compat_Anios CHECK (anio_desde BETWEEN 1950 AND 2100 AND anio_hasta >= anio_desde)
);
GO

CREATE TABLE dbo.CambiosMotor (
    cambio_motor_id         INT IDENTITY(1,1) CONSTRAINT PK_CambiosMotor PRIMARY KEY,
    orden_id                INT           NOT NULL CONSTRAINT FK_CambiosMotor_Orden    REFERENCES dbo.OrdenesTrabajo(orden_id),
    vehiculo_id             INT           NOT NULL CONSTRAINT FK_CambiosMotor_Vehiculo REFERENCES dbo.Vehiculos(vehiculo_id),
    serie_motor_anterior    VARCHAR(30)   NOT NULL,
    motor_nuevo_id          INT           NOT NULL CONSTRAINT FK_CambiosMotor_Motor    REFERENCES dbo.Motores(motor_id),
    destino_motor_retirado  VARCHAR(20)   NOT NULL,
    fecha_desmontaje        DATETIME2(0)  NOT NULL,
    fecha_instalacion       DATETIME2(0)  NOT NULL,
    resultado_pruebas       VARCHAR(1000) NOT NULL,
    pruebas_aprobadas       BIT           NOT NULL CONSTRAINT DF_CambiosMotor_Pruebas DEFAULT 0,
    autorizado_cliente      BIT           NOT NULL,
    observaciones           VARCHAR(500)  NULL,
    CONSTRAINT UQ_CambiosMotor_Orden UNIQUE (orden_id),
    CONSTRAINT UQ_CambiosMotor_Motor UNIQUE (motor_nuevo_id),     -- un motor se instala una sola vez
    CONSTRAINT CK_CambiosMotor_Destino CHECK (destino_motor_retirado IN ('DEVUELTO_CLIENTE','RECONSTRUCCION','CHATARRA','ALMACEN')),
    CONSTRAINT CK_CambiosMotor_Fechas  CHECK (fecha_instalacion >= fecha_desmontaje),
    CONSTRAINT CK_CambiosMotor_Serie   CHECK (LEN(LTRIM(serie_motor_anterior)) > 0),
    CONSTRAINT CK_CambiosMotor_Aut     CHECK (autorizado_cliente = 1)  -- sin autorización no se registra
);
GO


/* =====================================================================
   10. GARANTÍAS, PAGOS Y FACTURAS (Módulo 8)
   ===================================================================== */
CREATE TABLE dbo.Garantias (
    garantia_id         INT IDENTITY(1,1) CONSTRAINT PK_Garantias PRIMARY KEY,
    orden_id            INT           NOT NULL CONSTRAINT FK_Garantias_Orden REFERENCES dbo.OrdenesTrabajo(orden_id),
    descripcion         VARCHAR(300)  NOT NULL,
    fecha_inicio        DATE          NOT NULL,
    fecha_fin           DATE          NOT NULL,
    kilometraje_limite  INT           NULL,
    condiciones         VARCHAR(1000) NULL,
    CONSTRAINT UQ_Garantias_Orden UNIQUE (orden_id),
    CONSTRAINT CK_Garantias_Fechas CHECK (fecha_fin > fecha_inicio),
    CONSTRAINT CK_Garantias_Km     CHECK (kilometraje_limite IS NULL OR kilometraje_limite > 0)
);
GO

CREATE TABLE dbo.Pagos (
    pago_id     INT IDENTITY(1,1) CONSTRAINT PK_Pagos PRIMARY KEY,
    orden_id    INT           NOT NULL CONSTRAINT FK_Pagos_Orden REFERENCES dbo.OrdenesTrabajo(orden_id),
    monto       DECIMAL(12,2) NOT NULL,
    metodo      VARCHAR(15)   NOT NULL,
    fecha       DATETIME2(0)  NOT NULL CONSTRAINT DF_Pagos_Fecha DEFAULT SYSDATETIME(),
    referencia  VARCHAR(60)   NULL,
    CONSTRAINT CK_Pagos_Monto  CHECK (monto > 0),
    CONSTRAINT CK_Pagos_Metodo CHECK (metodo IN ('EFECTIVO','TARJETA','TRANSFERENCIA'))
);
GO
CREATE INDEX IX_Pagos_Orden ON dbo.Pagos(orden_id);
GO

CREATE TABLE dbo.Facturas (
    factura_id      INT IDENTITY(1,1) CONSTRAINT PK_Facturas PRIMARY KEY,
    numero_factura  AS ('FAC-' + RIGHT('000000' + CAST(factura_id AS VARCHAR(10)), 6)) PERSISTED,
    orden_id        INT           NOT NULL CONSTRAINT FK_Facturas_Orden REFERENCES dbo.OrdenesTrabajo(orden_id),
    fecha_emision   DATETIME2(0)  NOT NULL CONSTRAINT DF_Facturas_Fecha DEFAULT SYSDATETIME(),
    subtotal        DECIMAL(12,2) NOT NULL,
    descuento       DECIMAL(12,2) NOT NULL,
    impuesto        DECIMAL(12,2) NOT NULL,
    total           DECIMAL(12,2) NOT NULL,
    CONSTRAINT UQ_Facturas_Numero UNIQUE (numero_factura),
    CONSTRAINT UQ_Facturas_Orden  UNIQUE (orden_id),           -- una factura por orden
    CONSTRAINT CK_Facturas_Montos CHECK (subtotal >= 0 AND descuento >= 0 AND impuesto >= 0 AND total >= 0)
);
GO


/* =====================================================================
   11. AUDITORÍA
   ===================================================================== */
CREATE TABLE dbo.Auditoria (
    auditoria_id    BIGINT IDENTITY(1,1) CONSTRAINT PK_Auditoria PRIMARY KEY,
    tabla           VARCHAR(50)   NOT NULL,
    operacion       VARCHAR(10)   NOT NULL,
    registro_id     INT           NOT NULL,
    campo           VARCHAR(50)   NOT NULL,
    valor_anterior  VARCHAR(200)  NULL,
    valor_nuevo     VARCHAR(200)  NULL,
    usuario         VARCHAR(60)   NOT NULL CONSTRAINT DF_Auditoria_Usuario DEFAULT SUSER_SNAME(),
    fecha           DATETIME2(0)  NOT NULL CONSTRAINT DF_Auditoria_Fecha   DEFAULT SYSDATETIME()
);
GO


/* =====================================================================
   12. FUNCIONES
   ===================================================================== */

-- Totales de una orden: servicios + repuestos - descuento + impuesto, pagado y saldo.
-- El total no se guarda en la tabla (es un dato derivado); se calcula aquí.
CREATE OR ALTER FUNCTION dbo.fn_TotalesOrden (@orden_id INT)
RETURNS TABLE
AS
RETURN
WITH Base AS (
    SELECT
        ISNULL((SELECT SUM(d.cantidad * d.precio_unitario) FROM dbo.DetalleOrden d WHERE d.orden_id = @orden_id), 0)  AS total_servicios,
        ISNULL((SELECT SUM(r.cantidad * r.precio_unitario_historico) FROM dbo.RepuestosOrden r
                WHERE r.orden_id = @orden_id AND r.consumido = 1), 0)                                                 AS total_repuestos,
        ISNULL((SELECT TOP 1 p.descuento FROM dbo.Presupuestos p
                WHERE p.orden_id = @orden_id AND p.estado = 'APROBADO' ORDER BY p.fecha_respuesta DESC), 0)           AS descuento,
        COALESCE((SELECT TOP 1 i.porcentaje FROM dbo.Presupuestos p
                  INNER JOIN dbo.ConfiguracionImpuestos i ON i.impuesto_id = p.impuesto_id
                  WHERE p.orden_id = @orden_id AND p.estado = 'APROBADO' ORDER BY p.fecha_respuesta DESC),
                 (SELECT TOP 1 i.porcentaje FROM dbo.ConfiguracionImpuestos i WHERE i.activo = 1 AND i.es_predeterminado = 1),
                 0)                                                                                                   AS porcentaje_impuesto,
        ISNULL((SELECT SUM(pg.monto) FROM dbo.Pagos pg WHERE pg.orden_id = @orden_id), 0)                             AS total_pagado
),
Calculo AS (
    SELECT *,
           CAST(CASE WHEN total_servicios + total_repuestos - descuento < 0 THEN 0
                     ELSE total_servicios + total_repuestos - descuento END AS DECIMAL(12,2)) AS base_imponible
    FROM Base
)
SELECT
    CAST(total_servicios AS DECIMAL(12,2))                                                         AS total_servicios,
    CAST(total_repuestos AS DECIMAL(12,2))                                                         AS total_repuestos,
    CAST(descuento AS DECIMAL(12,2))                                                               AS descuento,
    base_imponible,
    CAST(porcentaje_impuesto AS DECIMAL(5,2))                                                      AS porcentaje_impuesto,
    CAST(ROUND(base_imponible * porcentaje_impuesto / 100, 2) AS DECIMAL(12,2))                    AS impuesto,
    CAST(base_imponible + ROUND(base_imponible * porcentaje_impuesto / 100, 2) AS DECIMAL(12,2))   AS total,
    CAST(total_pagado AS DECIMAL(12,2))                                                            AS total_pagado,
    CAST(base_imponible + ROUND(base_imponible * porcentaje_impuesto / 100, 2) - total_pagado AS DECIMAL(12,2)) AS saldo
FROM Calculo;
GO

-- Devuelve 1 si el motor es compatible con el vehículo (marca, modelo, año y combustible)
CREATE OR ALTER FUNCTION dbo.fn_MotorCompatible (@motor_id INT, @vehiculo_id INT)
RETURNS BIT
AS
BEGIN
    DECLARE @resultado BIT = 0;
    IF EXISTS (
        SELECT 1
        FROM dbo.Motores m
        INNER JOIN dbo.CompatibilidadMotor c ON c.motor_id = m.motor_id
        INNER JOIN dbo.Vehiculos v           ON v.vehiculo_id = @vehiculo_id
        WHERE m.motor_id = @motor_id
          AND c.marca = v.marca
          AND c.modelo = v.modelo
          AND v.anio BETWEEN c.anio_desde AND c.anio_hasta
          AND m.combustible = v.combustible
    )
        SET @resultado = 1;
    RETURN @resultado;
END;
GO


/* =====================================================================
   13. TRIGGERS
   ===================================================================== */

-- Auditoría: cada cambio de stock, costo o precio de un repuesto
CREATE OR ALTER TRIGGER dbo.trg_Repuestos_Auditoria
ON dbo.Repuestos
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.Auditoria (tabla, operacion, registro_id, campo, valor_anterior, valor_nuevo)
    SELECT 'Repuestos', 'UPDATE', i.repuesto_id, 'stock_actual', CAST(d.stock_actual AS VARCHAR(20)), CAST(i.stock_actual AS VARCHAR(20))
    FROM inserted i INNER JOIN deleted d ON d.repuesto_id = i.repuesto_id
    WHERE i.stock_actual <> d.stock_actual;

    INSERT INTO dbo.Auditoria (tabla, operacion, registro_id, campo, valor_anterior, valor_nuevo)
    SELECT 'Repuestos', 'UPDATE', i.repuesto_id, 'precio_venta', CAST(d.precio_venta AS VARCHAR(20)), CAST(i.precio_venta AS VARCHAR(20))
    FROM inserted i INNER JOIN deleted d ON d.repuesto_id = i.repuesto_id
    WHERE i.precio_venta <> d.precio_venta;

    INSERT INTO dbo.Auditoria (tabla, operacion, registro_id, campo, valor_anterior, valor_nuevo)
    SELECT 'Repuestos', 'UPDATE', i.repuesto_id, 'costo_compra', CAST(d.costo_compra AS VARCHAR(20)), CAST(i.costo_compra AS VARCHAR(20))
    FROM inserted i INNER JOIN deleted d ON d.repuesto_id = i.repuesto_id
    WHERE i.costo_compra <> d.costo_compra;
END;
GO

-- Auditoría: cada cambio de estado de una orden de trabajo
CREATE OR ALTER TRIGGER dbo.trg_OrdenesTrabajo_AuditoriaEstado
ON dbo.OrdenesTrabajo
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT UPDATE(estado) RETURN;

    INSERT INTO dbo.Auditoria (tabla, operacion, registro_id, campo, valor_anterior, valor_nuevo)
    SELECT 'OrdenesTrabajo', 'UPDATE', i.orden_id, 'estado', d.estado, i.estado
    FROM inserted i INNER JOIN deleted d ON d.orden_id = i.orden_id
    WHERE i.estado <> d.estado;
END;
GO

-- Auditoría: cambios de precio del catálogo (los importes de órdenes viejas no cambian)
CREATE OR ALTER TRIGGER dbo.trg_Servicios_AuditoriaPrecio
ON dbo.Servicios
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT UPDATE(costo_mano_obra) RETURN;

    INSERT INTO dbo.Auditoria (tabla, operacion, registro_id, campo, valor_anterior, valor_nuevo)
    SELECT 'Servicios', 'UPDATE', i.servicio_id, 'costo_mano_obra', CAST(d.costo_mano_obra AS VARCHAR(20)), CAST(i.costo_mano_obra AS VARCHAR(20))
    FROM inserted i INNER JOIN deleted d ON d.servicio_id = i.servicio_id
    WHERE i.costo_mano_obra <> d.costo_mano_obra;
END;
GO

-- Las órdenes cerradas no se pueden modificar en sus servicios ni repuestos (importes históricos)
CREATE OR ALTER TRIGGER dbo.trg_DetalleOrden_Bloqueo
ON dbo.DetalleOrden
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1 FROM dbo.OrdenesTrabajo o
        WHERE o.orden_id IN (SELECT orden_id FROM inserted UNION SELECT orden_id FROM deleted)
          AND o.estado IN ('FINALIZADA','ENTREGADA','RECHAZADA','CANCELADA')
    )
    BEGIN
        THROW 50020, 'No se pueden modificar los servicios de una orden cerrada.', 1;
    END
END;
GO

CREATE OR ALTER TRIGGER dbo.trg_RepuestosOrden_Bloqueo
ON dbo.RepuestosOrden
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1 FROM dbo.OrdenesTrabajo o
        WHERE o.orden_id IN (SELECT orden_id FROM inserted UNION SELECT orden_id FROM deleted)
          AND o.estado IN ('FINALIZADA','ENTREGADA','RECHAZADA','CANCELADA')
    )
    BEGIN
        THROW 50021, 'No se pueden agregar ni modificar repuestos de una orden cerrada.', 1;
    END
END;
GO


/* =====================================================================
   14. PROCEDIMIENTOS ALMACENADOS
   Todos usan transacciones: si algo falla, se revierte todo (ROLLBACK).
   Los errores de negocio se lanzan con THROW 5xxxx y Java los recibe como
   SQLException (getErrorCode() = número) con el mensaje en español.
   ===================================================================== */

-- 14.1 Consumir un repuesto en una orden (descuento atómico de inventario)
--      Control de concurrencia:
--        * el UPDATE descuenta solo si hay stock suficiente (WHERE stock_actual >= @cantidad),
--          así dos hilos nunca dejan el stock negativo;
--        * @clave_operacion evita descontar dos veces la misma operación
--          (doble clic o reintento): la segunda vez no hace nada.
CREATE OR ALTER PROCEDURE dbo.sp_ConsumirRepuesto
    @orden_id          INT,
    @repuesto_id       INT,
    @cantidad          INT,
    @usuario           VARCHAR(60) = NULL,
    @clave_operacion   UNIQUEIDENTIFIER = NULL,
    @repuesto_orden_id INT = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @cantidad IS NULL OR @cantidad <= 0
        THROW 50001, 'La cantidad a consumir debe ser mayor que cero.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Operación ya procesada: no se repite el descuento
        IF @clave_operacion IS NOT NULL AND EXISTS (
            SELECT 1 FROM dbo.MovimientosInventario WITH (UPDLOCK, HOLDLOCK) WHERE clave_operacion = @clave_operacion)
        BEGIN
            SET @repuesto_orden_id = NULL;
            COMMIT TRANSACTION;
            RETURN 1;   -- 1 = ya existía
        END

        DECLARE @estado VARCHAR(25);
        SELECT @estado = estado FROM dbo.OrdenesTrabajo WITH (UPDLOCK) WHERE orden_id = @orden_id AND activo = 1;
        IF @estado IS NULL
            THROW 50002, 'La orden no existe o está inactiva.', 1;
        IF @estado NOT IN ('EN_DIAGNOSTICO','EN_REPARACION')
            THROW 50003, 'Solo se pueden consumir repuestos en órdenes en diagnóstico o en reparación.', 1;

        DECLARE @stock_anterior INT, @precio DECIMAL(12,2), @costo DECIMAL(12,2), @nombre VARCHAR(100);

        UPDATE dbo.Repuestos
        SET stock_actual    = stock_actual - @cantidad,
            @stock_anterior = stock_actual,        -- valor antes del descuento
            @precio         = precio_venta,
            @costo          = costo_compra,
            @nombre         = nombre
        WHERE repuesto_id = @repuesto_id
          AND activo = 1
          AND stock_actual >= @cantidad;

        IF @@ROWCOUNT = 0
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM dbo.Repuestos WHERE repuesto_id = @repuesto_id AND activo = 1)
                THROW 50004, 'El repuesto no existe o está inactivo.', 1;
            ELSE
                THROW 50005, 'Stock insuficiente para el repuesto solicitado.', 1;
        END

        INSERT INTO dbo.MovimientosInventario (repuesto_id, orden_id, tipo_movimiento, cantidad, stock_anterior,
                                               stock_resultante, costo_unitario, motivo, usuario_registro, clave_operacion)
        VALUES (@repuesto_id, @orden_id, 'SALIDA_ORDEN', @cantidad, @stock_anterior, @stock_anterior - @cantidad, @costo,
                CONCAT('Consumo en orden OT-', RIGHT('000000' + CAST(@orden_id AS VARCHAR(10)), 6)),
                ISNULL(@usuario, SUSER_SNAME()), @clave_operacion);

        INSERT INTO dbo.RepuestosOrden (orden_id, repuesto_id, nombre_historico, cantidad,
                                        costo_unitario_historico, precio_unitario_historico, consumido)
        VALUES (@orden_id, @repuesto_id, @nombre, @cantidad, @costo, @precio, 1);

        SET @repuesto_orden_id = SCOPE_IDENTITY();

        COMMIT TRANSACTION;
        RETURN 0;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 14.2 Entrada de inventario (compra a proveedor)
CREATE OR ALTER PROCEDURE dbo.sp_RegistrarEntradaInventario
    @repuesto_id    INT,
    @cantidad       INT,
    @costo_compra   DECIMAL(12,2),
    @motivo         VARCHAR(200) = NULL,
    @usuario        VARCHAR(60) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @cantidad IS NULL OR @cantidad <= 0
        THROW 50010, 'La cantidad de la entrada debe ser mayor que cero.', 1;
    IF @costo_compra IS NULL OR @costo_compra < 0
        THROW 50011, 'El costo de compra no puede ser negativo.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @stock_anterior INT;

        UPDATE dbo.Repuestos
        SET stock_actual    = stock_actual + @cantidad,
            @stock_anterior = stock_actual,
            costo_compra    = @costo_compra
        WHERE repuesto_id = @repuesto_id AND activo = 1;

        IF @@ROWCOUNT = 0
            THROW 50004, 'El repuesto no existe o está inactivo.', 1;

        INSERT INTO dbo.MovimientosInventario (repuesto_id, tipo_movimiento, cantidad, stock_anterior, stock_resultante,
                                               costo_unitario, motivo, usuario_registro)
        VALUES (@repuesto_id, 'ENTRADA', @cantidad, @stock_anterior, @stock_anterior + @cantidad, @costo_compra,
                ISNULL(@motivo, 'Compra a proveedor'), ISNULL(@usuario, SUSER_SNAME()));

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 14.3 Asignar un mecánico a una orden (impide personal inactivo o no disponible)
CREATE OR ALTER PROCEDURE dbo.sp_AsignarMecanico
    @orden_id        INT,
    @mecanico_id     INT,
    @es_responsable  BIT = 0
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @estado VARCHAR(25), @activo BIT, @disponible BIT;

        SELECT @estado = estado FROM dbo.OrdenesTrabajo WITH (UPDLOCK) WHERE orden_id = @orden_id AND activo = 1;
        IF @estado IS NULL
            THROW 50002, 'La orden no existe o está inactiva.', 1;
        IF @estado IN ('FINALIZADA','ENTREGADA','RECHAZADA','CANCELADA')
            THROW 50030, 'No se pueden asignar mecánicos a una orden cerrada.', 1;

        SELECT @activo = activo, @disponible = disponible FROM dbo.Mecanicos WHERE mecanico_id = @mecanico_id;
        IF @activo IS NULL
            THROW 50031, 'El mecánico no existe.', 1;
        IF @activo = 0
            THROW 50032, 'No se puede asignar un mecánico inactivo.', 1;
        IF @disponible = 0
            THROW 50033, 'El mecánico no está disponible.', 1;

        -- Si es el nuevo responsable, los demás dejan de serlo
        IF @es_responsable = 1
            UPDATE dbo.AsignacionMecanicos SET es_responsable = 0 WHERE orden_id = @orden_id AND es_responsable = 1;

        IF EXISTS (SELECT 1 FROM dbo.AsignacionMecanicos WHERE orden_id = @orden_id AND mecanico_id = @mecanico_id)
            UPDATE dbo.AsignacionMecanicos SET es_responsable = @es_responsable
            WHERE orden_id = @orden_id AND mecanico_id = @mecanico_id;
        ELSE
            INSERT INTO dbo.AsignacionMecanicos (orden_id, mecanico_id, es_responsable)
            VALUES (@orden_id, @mecanico_id, @es_responsable);

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 14.4 Cambiar el estado de una orden validando el flujo y las reglas de negocio
CREATE OR ALTER PROCEDURE dbo.sp_CambiarEstadoOrden
    @orden_id      INT,
    @nuevo_estado  VARCHAR(25)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @estado_actual VARCHAR(25), @nivel_maximo TINYINT;

        SELECT @estado_actual = estado
        FROM dbo.OrdenesTrabajo WITH (UPDLOCK, HOLDLOCK)
        WHERE orden_id = @orden_id AND activo = 1;

        IF @estado_actual IS NULL
            THROW 50002, 'La orden no existe o está inactiva.', 1;

        IF NOT EXISTS (SELECT 1 FROM dbo.TransicionesEstadoOrden
                       WHERE estado_actual = @estado_actual AND estado_siguiente = @nuevo_estado)
        BEGIN
            DECLARE @msg NVARCHAR(200) = CONCAT('No se permite pasar de ', @estado_actual, ' a ', @nuevo_estado, '.');
            THROW 50040, @msg, 1;
        END

        SELECT @nivel_maximo = ISNULL(MAX(s.nivel_complejidad), 0)
        FROM dbo.DetalleOrden d INNER JOIN dbo.Servicios s ON s.servicio_id = d.servicio_id
        WHERE d.orden_id = @orden_id;

        -- Para presupuestar: debe haber servicios; niveles 3 a 5 exigen diagnóstico registrado
        IF @nuevo_estado = 'PENDIENTE_APROBACION'
        BEGIN
            IF @nivel_maximo = 0
                THROW 50041, 'La orden no tiene servicios registrados.', 1;
            IF @nivel_maximo >= 3 AND NOT EXISTS (SELECT 1 FROM dbo.Diagnosticos WHERE orden_id = @orden_id)
                THROW 50042, 'Los servicios de nivel 3 a 5 requieren un diagnóstico registrado.', 1;
        END

        -- Para reparar: presupuesto aprobado por el cliente y mecánico responsable asignado
        IF @nuevo_estado = 'EN_REPARACION' AND @estado_actual = 'PENDIENTE_APROBACION'
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM dbo.Presupuestos WHERE orden_id = @orden_id AND estado = 'APROBADO')
                THROW 50043, 'El cliente no ha aprobado el presupuesto.', 1;
            IF NOT EXISTS (SELECT 1 FROM dbo.AsignacionMecanicos WHERE orden_id = @orden_id AND es_responsable = 1)
                THROW 50044, 'La orden no tiene un mecánico responsable asignado.', 1;
        END

        IF @nuevo_estado = 'RECHAZADA'
           AND NOT EXISTS (SELECT 1 FROM dbo.Presupuestos WHERE orden_id = @orden_id AND estado = 'RECHAZADO')
            THROW 50045, 'Para rechazar la orden, el presupuesto debe estar rechazado por el cliente.', 1;

        -- Para finalizar: todo debe estar completo
        IF @nuevo_estado = 'FINALIZADA'
        BEGIN
            IF EXISTS (SELECT 1 FROM dbo.ActividadesTrabajo WHERE orden_id = @orden_id AND estado <> 'COMPLETADA')
                THROW 50046, 'No se puede finalizar: hay actividades pendientes.', 1;

            IF EXISTS (SELECT 1 FROM dbo.RepuestosOrden WHERE orden_id = @orden_id AND consumido = 0)
                THROW 50047, 'No se puede finalizar: hay repuestos sin descontar del inventario.', 1;

            -- Control de calidad obligatorio para niveles 3 a 5: el último control debe estar aprobado
            IF @nivel_maximo >= 3 AND ISNULL((SELECT TOP 1 aprobado FROM dbo.ControlesCalidad
                                              WHERE orden_id = @orden_id ORDER BY fecha DESC, control_id DESC), 0) = 0
                THROW 50048, 'No se puede finalizar: el control de calidad obligatorio no está aprobado.', 1;

            -- Cambio de motor: pruebas aprobadas
            IF EXISTS (SELECT 1 FROM dbo.CambiosMotor WHERE orden_id = @orden_id AND pruebas_aprobadas = 0)
                THROW 50049, 'No se puede finalizar: las pruebas del motor nuevo no están aprobadas.', 1;

            -- Reparación mayor (nivel 5): debe existir la garantía
            IF @nivel_maximo = 5 AND NOT EXISTS (SELECT 1 FROM dbo.Garantias WHERE orden_id = @orden_id)
                THROW 50050, 'No se puede finalizar: la reparación mayor requiere generar la garantía.', 1;
        END

        UPDATE dbo.OrdenesTrabajo
        SET estado = @nuevo_estado,
            fecha_entrega = CASE WHEN @nuevo_estado = 'ENTREGADA' THEN SYSDATETIME() ELSE fecha_entrega END
        WHERE orden_id = @orden_id;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 14.5 Registrar la sustitución de motor (pasos 2, 3, 4, 9 y 10 del caso Hilux)
--      Bloquea motores incompatibles, no disponibles o sin datos obligatorios, informando la causa.
--      En una sola transacción: registra el cambio, marca el motor como INSTALADO,
--      actualiza la serie del motor del vehículo y descuenta el motor del inventario.
CREATE OR ALTER PROCEDURE dbo.sp_RegistrarCambioMotor
    @orden_id                INT,
    @serie_motor_anterior    VARCHAR(30),
    @motor_nuevo_id          INT,
    @destino_motor_retirado  VARCHAR(20),
    @fecha_desmontaje        DATETIME2(0),
    @fecha_instalacion       DATETIME2(0),
    @resultado_pruebas       VARCHAR(1000),
    @pruebas_aprobadas       BIT,
    @observaciones           VARCHAR(500) = NULL,
    @usuario                 VARCHAR(60) = NULL,
    @cambio_motor_id         INT = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    -- Datos obligatorios
    IF NULLIF(LTRIM(@serie_motor_anterior), '') IS NULL
        THROW 50060, 'Falta el número de serie del motor anterior.', 1;
    IF @motor_nuevo_id IS NULL
        THROW 50061, 'Debe seleccionar el motor nuevo.', 1;
    IF @destino_motor_retirado IS NULL
        THROW 50062, 'Debe indicar el destino del motor retirado.', 1;
    IF @fecha_desmontaje IS NULL OR @fecha_instalacion IS NULL
        THROW 50063, 'Debe registrar las fechas de desmontaje e instalación.', 1;
    IF NULLIF(LTRIM(@resultado_pruebas), '') IS NULL
        THROW 50064, 'Debe registrar el resultado de las pruebas.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @vehiculo_id INT, @estado VARCHAR(25), @estado_motor VARCHAR(12),
                @repuesto_motor_id INT, @serie_nueva VARCHAR(30);

        SELECT @vehiculo_id = vehiculo_id, @estado = estado
        FROM dbo.OrdenesTrabajo WITH (UPDLOCK) WHERE orden_id = @orden_id AND activo = 1;

        IF @vehiculo_id IS NULL
            THROW 50002, 'La orden no existe o está inactiva.', 1;
        IF @estado <> 'EN_REPARACION'
            THROW 50065, 'El cambio de motor solo se registra con la orden en reparación.', 1;
        IF NOT EXISTS (SELECT 1 FROM dbo.Presupuestos WHERE orden_id = @orden_id AND estado = 'APROBADO')
            THROW 50043, 'El cliente no ha autorizado el presupuesto.', 1;

        SELECT @estado_motor = estado, @repuesto_motor_id = repuesto_id, @serie_nueva = numero_serie
        FROM dbo.Motores WITH (UPDLOCK) WHERE motor_id = @motor_nuevo_id AND activo = 1;

        IF @estado_motor IS NULL
            THROW 50066, 'El motor seleccionado no existe o está inactivo.', 1;
        IF @estado_motor <> 'DISPONIBLE'
        BEGIN
            DECLARE @msg_motor NVARCHAR(200) = CONCAT('El motor seleccionado no está disponible (estado: ', @estado_motor, ').');
            THROW 50067, @msg_motor, 1;
        END
        IF dbo.fn_MotorCompatible(@motor_nuevo_id, @vehiculo_id) = 0
            THROW 50068, 'El motor seleccionado no es compatible con la marca, modelo, año o combustible del vehículo.', 1;
        IF @serie_nueva = @serie_motor_anterior
            THROW 50069, 'La serie del motor anterior no puede ser la misma del motor nuevo.', 1;

        INSERT INTO dbo.CambiosMotor (orden_id, vehiculo_id, serie_motor_anterior, motor_nuevo_id, destino_motor_retirado,
                                      fecha_desmontaje, fecha_instalacion, resultado_pruebas, pruebas_aprobadas,
                                      autorizado_cliente, observaciones)
        VALUES (@orden_id, @vehiculo_id, @serie_motor_anterior, @motor_nuevo_id, @destino_motor_retirado,
                @fecha_desmontaje, @fecha_instalacion, @resultado_pruebas, @pruebas_aprobadas, 1, @observaciones);

        SET @cambio_motor_id = SCOPE_IDENTITY();

        UPDATE dbo.Motores   SET estado = 'INSTALADO'          WHERE motor_id = @motor_nuevo_id;
        UPDATE dbo.Vehiculos SET serie_motor_actual = @serie_nueva WHERE vehiculo_id = @vehiculo_id;

        -- El motor también es un artículo del inventario: se descuenta (paso 9)
        IF @repuesto_motor_id IS NOT NULL
            EXEC dbo.sp_ConsumirRepuesto @orden_id = @orden_id, @repuesto_id = @repuesto_motor_id,
                                         @cantidad = 1, @usuario = @usuario;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 14.6 Registrar un pago parcial o total (impide sobrepagos)
CREATE OR ALTER PROCEDURE dbo.sp_RegistrarPago
    @orden_id     INT,
    @monto        DECIMAL(12,2),
    @metodo       VARCHAR(15),
    @referencia   VARCHAR(60) = NULL,
    @saldo_nuevo  DECIMAL(12,2) = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @monto IS NULL OR @monto <= 0
        THROW 50070, 'El monto del pago debe ser mayor que cero.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @estado VARCHAR(25), @saldo DECIMAL(12,2);

        -- Bloquea la orden: dos pagos simultáneos no pueden superar el saldo
        SELECT @estado = estado FROM dbo.OrdenesTrabajo WITH (UPDLOCK, HOLDLOCK)
        WHERE orden_id = @orden_id AND activo = 1;

        IF @estado IS NULL
            THROW 50002, 'La orden no existe o está inactiva.', 1;
        IF @estado IN ('RECHAZADA','CANCELADA')
            THROW 50071, 'No se pueden registrar pagos en una orden rechazada o cancelada.', 1;

        SELECT @saldo = saldo FROM dbo.fn_TotalesOrden(@orden_id);

        IF @saldo <= 0
            THROW 50072, 'La orden no tiene saldo pendiente.', 1;
        IF @monto > @saldo
        BEGIN
            DECLARE @msg NVARCHAR(200) = CONCAT('Sobrepago: el monto (', @monto, ') supera el saldo pendiente (', @saldo, ').');
            THROW 50073, @msg, 1;
        END

        INSERT INTO dbo.Pagos (orden_id, monto, metodo, referencia)
        VALUES (@orden_id, @monto, @metodo, @referencia);

        SET @saldo_nuevo = @saldo - @monto;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- 14.7 Generar la factura de una orden finalizada
CREATE OR ALTER PROCEDURE dbo.sp_GenerarFactura
    @orden_id    INT,
    @factura_id  INT = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @estado VARCHAR(25);
        SELECT @estado = estado FROM dbo.OrdenesTrabajo WITH (UPDLOCK, HOLDLOCK) WHERE orden_id = @orden_id AND activo = 1;

        IF @estado IS NULL
            THROW 50002, 'La orden no existe o está inactiva.', 1;
        IF @estado NOT IN ('FINALIZADA','ENTREGADA')
            THROW 50080, 'Solo se facturan órdenes finalizadas.', 1;
        IF EXISTS (SELECT 1 FROM dbo.Facturas WHERE orden_id = @orden_id)
            THROW 50081, 'La orden ya tiene una factura.', 1;

        INSERT INTO dbo.Facturas (orden_id, subtotal, descuento, impuesto, total)
        SELECT @orden_id, t.total_servicios + t.total_repuestos, t.descuento, t.impuesto, t.total
        FROM dbo.fn_TotalesOrden(@orden_id) t;

        SET @factura_id = SCOPE_IDENTITY();

        COMMIT TRANSACTION;

        SELECT f.factura_id, f.numero_factura, f.fecha_emision, f.subtotal, f.descuento, f.impuesto, f.total
        FROM dbo.Facturas f WHERE f.factura_id = @factura_id;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO


/* =====================================================================
   15. VISTAS ADMINISTRATIVAS
   ===================================================================== */

-- Resumen de órdenes con cliente, vehículo, responsable y montos
CREATE OR ALTER VIEW dbo.vw_OrdenesResumen
AS
SELECT
    o.orden_id,
    o.numero_orden,
    o.fecha_ingreso,
    o.fecha_entrega,
    o.estado,
    e.descripcion                                          AS estado_descripcion,
    c.cliente_id,
    c.nombre                                               AS cliente,
    v.vehiculo_id,
    v.placa,
    CONCAT(v.marca, ' ', v.modelo, ' ', v.anio)            AS vehiculo,
    m.nombre                                               AS mecanico_responsable,
    CAST(CASE WHEN EXISTS (SELECT 1 FROM dbo.Diagnosticos dg WHERE dg.orden_id = o.orden_id AND dg.requiere_cambio_motor = 1)
                OR EXISTS (SELECT 1 FROM dbo.CambiosMotor cm WHERE cm.orden_id = o.orden_id)
              THEN 1 ELSE 0 END AS BIT)                    AS requiere_cambio_motor,
    t.total_servicios                                      AS total_mano_obra,
    t.total_repuestos,
    t.total_servicios + t.total_repuestos                  AS importe_total_base,
    t.descuento,
    t.impuesto,
    t.total,
    t.total_pagado,
    t.saldo,
    f.numero_factura
FROM dbo.OrdenesTrabajo o
INNER JOIN dbo.Clientes c      ON c.cliente_id  = o.cliente_id
INNER JOIN dbo.Vehiculos v     ON v.vehiculo_id = o.vehiculo_id
INNER JOIN dbo.EstadosOrden e  ON e.codigo      = o.estado
LEFT  JOIN dbo.AsignacionMecanicos a ON a.orden_id = o.orden_id AND a.es_responsable = 1
LEFT  JOIN dbo.Mecanicos m     ON m.mecanico_id = a.mecanico_id
LEFT  JOIN dbo.Facturas f      ON f.orden_id    = o.orden_id
CROSS APPLY dbo.fn_TotalesOrden(o.orden_id) t
WHERE o.activo = 1;
GO

-- Historial de cada vehículo: fechas, servicios, mecánicos, repuestos, costos y motores
CREATE OR ALTER VIEW dbo.vw_HistorialVehiculo
AS
SELECT
    v.vehiculo_id,
    v.placa,
    CONCAT(v.marca, ' ', v.modelo, ' ', v.anio)  AS vehiculo,
    c.nombre                                     AS propietario,
    o.orden_id,
    o.numero_orden,
    o.fecha_ingreso,
    o.fecha_entrega,
    o.kilometraje_ingreso,
    o.estado,
    resp.nombre                                  AS mecanico_responsable,
    mec.mecanicos,
    srv.servicios_realizados,
    rep.repuestos_utilizados,
    t.total                                      AS costo_total_orden,
    cm.serie_motor_anterior,
    mn.numero_serie                              AS serie_motor_nuevo
FROM dbo.Vehiculos v
INNER JOIN dbo.Clientes c       ON c.cliente_id  = v.cliente_id
INNER JOIN dbo.OrdenesTrabajo o ON o.vehiculo_id = v.vehiculo_id AND o.activo = 1
LEFT  JOIN dbo.AsignacionMecanicos ar ON ar.orden_id = o.orden_id AND ar.es_responsable = 1
LEFT  JOIN dbo.Mecanicos resp   ON resp.mecanico_id = ar.mecanico_id
LEFT  JOIN dbo.CambiosMotor cm  ON cm.orden_id = o.orden_id
LEFT  JOIN dbo.Motores mn       ON mn.motor_id = cm.motor_nuevo_id
OUTER APPLY (SELECT STRING_AGG(s.nombre, ', ') AS servicios_realizados
             FROM dbo.DetalleOrden d INNER JOIN dbo.Servicios s ON s.servicio_id = d.servicio_id
             WHERE d.orden_id = o.orden_id) srv
OUTER APPLY (SELECT STRING_AGG(m.nombre, ', ') AS mecanicos
             FROM dbo.AsignacionMecanicos a INNER JOIN dbo.Mecanicos m ON m.mecanico_id = a.mecanico_id
             WHERE a.orden_id = o.orden_id) mec
OUTER APPLY (SELECT STRING_AGG(CONCAT(r.nombre_historico, ' x', r.cantidad), ', ') AS repuestos_utilizados
             FROM dbo.RepuestosOrden r
             WHERE r.orden_id = o.orden_id AND r.consumido = 1) rep
CROSS APPLY dbo.fn_TotalesOrden(o.orden_id) t;
GO

-- Repuestos en o por debajo del stock mínimo (para reabastecer)
CREATE OR ALTER VIEW dbo.vw_StockBajo
AS
SELECT
    r.repuesto_id,
    r.codigo,
    r.nombre,
    r.categoria,
    r.marca,
    r.stock_actual,
    r.stock_minimo,
    r.stock_minimo - r.stock_actual  AS unidades_faltantes,
    r.costo_compra,
    p.nombre                         AS proveedor,
    p.telefono                       AS telefono_proveedor
FROM dbo.Repuestos r
LEFT JOIN dbo.Proveedores p ON p.proveedor_id = r.proveedor_id
WHERE r.activo = 1
  AND r.stock_actual <= r.stock_minimo;
GO

-- Facturas con lo pagado y el saldo de cada una
CREATE OR ALTER VIEW dbo.vw_SaldosFacturas
AS
SELECT
    f.factura_id,
    f.numero_factura,
    o.orden_id,
    o.numero_orden,
    c.nombre                                      AS nombre_cliente,
    f.subtotal,
    f.descuento,
    f.impuesto,
    f.total,
    ISNULL(pg.total_pagado, 0)                    AS total_pagado,
    f.total - ISNULL(pg.total_pagado, 0)          AS saldo_pendiente,
    CASE WHEN ISNULL(pg.total_pagado, 0) >= f.total THEN 'PAGADA'
         WHEN ISNULL(pg.total_pagado, 0) > 0        THEN 'PARCIAL'
         ELSE 'PENDIENTE' END                     AS estado_pago,
    f.fecha_emision
FROM dbo.Facturas f
INNER JOIN dbo.OrdenesTrabajo o ON o.orden_id = f.orden_id
INNER JOIN dbo.Clientes c       ON c.cliente_id = o.cliente_id
LEFT JOIN (SELECT orden_id, SUM(monto) AS total_pagado FROM dbo.Pagos GROUP BY orden_id) pg
       ON pg.orden_id = f.orden_id;
GO

-- Productividad de mecánicos: órdenes, actividades y horas (GROUP BY)
CREATE OR ALTER VIEW dbo.vw_ProductividadMecanicos
AS
SELECT
    m.mecanico_id,
    m.codigo_empleado,
    m.nombre                                                                AS mecanico,
    COUNT(DISTINCT a.orden_id)                                              AS ordenes_asignadas,
    COUNT(DISTINCT CASE WHEN a.es_responsable = 1 THEN a.orden_id END)      AS ordenes_como_responsable,
    (SELECT COUNT(*) FROM dbo.ActividadesTrabajo x
     WHERE x.mecanico_id = m.mecanico_id AND x.estado = 'COMPLETADA')       AS actividades_completadas,
    (SELECT ISNULL(SUM(x.horas), 0) FROM dbo.ActividadesTrabajo x
     WHERE x.mecanico_id = m.mecanico_id)                                   AS horas_trabajadas
FROM dbo.Mecanicos m
LEFT JOIN dbo.AsignacionMecanicos a ON a.mecanico_id = m.mecanico_id
WHERE m.activo = 1
GROUP BY m.mecanico_id, m.codigo_empleado, m.nombre;
GO

-- Servicios más solicitados e ingresos generados (GROUP BY + agregaciones)
CREATE OR ALTER VIEW dbo.vw_ServiciosMasSolicitados
AS
SELECT
    s.servicio_id,
    s.codigo,
    s.nombre,
    s.nivel_complejidad,
    COUNT(d.detalle_id)                                 AS veces_solicitado,
    ISNULL(SUM(d.cantidad * d.precio_unitario), 0)      AS ingresos,
    ISNULL(AVG(d.precio_unitario), 0)                   AS precio_promedio
FROM dbo.Servicios s
LEFT JOIN dbo.DetalleOrden d ON d.servicio_id = s.servicio_id
GROUP BY s.servicio_id, s.codigo, s.nombre, s.nivel_complejidad;
GO

-- Historial completo de un vehículo (por placa)
CREATE OR ALTER PROCEDURE dbo.sp_HistorialVehiculo
    @placa VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT * FROM dbo.vw_HistorialVehiculo
    WHERE placa = @placa
    ORDER BY fecha_ingreso DESC;
END;
GO


/* =====================================================================
   16. DATOS DE PRUEBA
   ===================================================================== */

INSERT INTO dbo.EstadosOrden (codigo, descripcion, secuencia, es_final) VALUES
('RECIBIDA',             'Recibida',                1, 0),
('EN_DIAGNOSTICO',       'En diagnóstico',          2, 0),
('PENDIENTE_APROBACION', 'Pendiente de aprobación', 3, 0),
('EN_REPARACION',        'En reparación',           4, 0),
('EN_CONTROL_CALIDAD',   'En control de calidad',   5, 0),
('FINALIZADA',           'Finalizada',              6, 0),
('ENTREGADA',            'Entregada',               7, 1),
('RECHAZADA',            'Rechazada',               8, 1),
('CANCELADA',            'Cancelada',               9, 1);

INSERT INTO dbo.TransicionesEstadoOrden (estado_actual, estado_siguiente) VALUES
('RECIBIDA',             'EN_DIAGNOSTICO'),
('RECIBIDA',             'CANCELADA'),
('EN_DIAGNOSTICO',       'PENDIENTE_APROBACION'),
('EN_DIAGNOSTICO',       'CANCELADA'),
('PENDIENTE_APROBACION', 'EN_REPARACION'),
('PENDIENTE_APROBACION', 'RECHAZADA'),
('PENDIENTE_APROBACION', 'CANCELADA'),
('EN_REPARACION',        'EN_CONTROL_CALIDAD'),
('EN_REPARACION',        'CANCELADA'),
('EN_CONTROL_CALIDAD',   'FINALIZADA'),
('EN_CONTROL_CALIDAD',   'EN_REPARACION'),   -- si el control falla, vuelve a reparación
('FINALIZADA',           'ENTREGADA');

INSERT INTO dbo.ConfiguracionImpuestos (nombre, porcentaje, es_predeterminado) VALUES
('ISV 15%', 15.00, 1),
('ISV 18%', 18.00, 0),
('Exento',   0.00, 0);

INSERT INTO dbo.Especialidades (nombre, descripcion) VALUES
('Mantenimiento general', 'Aceite, filtros, bujías, luces'),
('Frenos y suspensión',   'Sistema de frenos, amortiguadores, dirección'),
('Sistema eléctrico',     'Alternador, batería, cableado, sensores'),
('Transmisiones',         'Caja de cambios, embrague, diferencial'),
('Motores',               'Culata, reconstrucción y sustitución de motor');

-- Clientes (Carlos tiene dos vehículos: historiales separados)
INSERT INTO dbo.Clientes (identificacion, nombre, telefono, correo, direccion, tipo_cliente) VALUES
('0801-1985-12345', 'Carlos Mejía',         '9988-1122', 'carlos.mejia@correo.hn',  'Col. Kennedy, Tegucigalpa',             'PARTICULAR'),
('0501-1990-54321', 'Ana Rodríguez',        '9876-5432', 'ana.rodriguez@correo.hn', 'Barrio Río de Piedras, San Pedro Sula', 'PARTICULAR'),
('08019010111222',  'Transportes Lempira',  '2234-5566', 'flota@tlempira.hn',       'Blvd. del Norte, Tegucigalpa',          'CORPORATIVO');

INSERT INTO dbo.Vehiculos (cliente_id, placa, vin, marca, modelo, anio, color, kilometraje, combustible, serie_motor_actual) VALUES
(1, 'HAB 1234', 'MR0FZ29G5F1234567', 'Toyota', 'Hilux',    2015, 'Blanco', 185000, 'DIESEL',   '1KD-5511223'),
(1, 'HCD 5678', '2HGFC2F59JH512345', 'Honda',  'Civic',    2018, 'Gris',    62000, 'GASOLINA', 'K20C2-889900'),
(2, 'PBE 4321', '3N6CD33B6KK123456', 'Nissan', 'Frontier', 2019, 'Rojo',    98000, 'DIESEL',   'YD25-445566'),
(3, 'TGU 2468', 'KNADM4A39L6123456', 'Kia',    'Rio',      2020, 'Azul',    45000, 'GASOLINA', NULL);

INSERT INTO dbo.Mecanicos (codigo_empleado, identificacion, nombre, telefono, nivel_certificacion, tarifa_hora, disponible, activo) VALUES
('MEC-001', '0801-1980-11111', 'José Martínez',   '9911-2233', 'MASTER', 250.00, 1, 1),
('MEC-002', '0801-1988-22222', 'Pedro Álvarez',   '9922-3344', 'SENIOR', 220.00, 1, 1),
('MEC-003', '0801-1992-33333', 'María López',     '9933-4455', 'SENIOR', 230.00, 1, 1),
('MEC-004', '0801-1975-44444', 'Ricardo Banegas', '9944-5566', 'SENIOR', 200.00, 0, 1),   -- no disponible
('MEC-005', '0801-1970-55555', 'Jorge Zelaya',    '9955-6677', 'JUNIOR', 180.00, 0, 0);   -- inactivo

INSERT INTO dbo.MecanicoEspecialidades (mecanico_id, especialidad_id) VALUES
(1, 5), (1, 4),       -- José: motores y transmisiones
(2, 1), (2, 2),       -- Pedro: mantenimiento y frenos
(3, 3), (3, 1),       -- María: eléctrico y mantenimiento
(4, 2), (5, 1);

INSERT INTO dbo.Proveedores (rtn, nombre, contacto, telefono, correo, direccion) VALUES
('08019001234567', 'Repuestos Toyota Honduras', 'Mario Cruz',   '2232-1000', 'ventas@reptoyota.hn',    'Blvd. Suyapa, Tegucigalpa'),
('05019007654321', 'Lubricantes del Norte',     'Sofía Pineda', '2550-2000', 'pedidos@lubrinorte.hn',  'Zona Industrial, San Pedro Sula'),
('08019005555555', 'Motores y Partes S.A.',     'Iván Reyes',   '2239-3000', 'info@motoresypartes.hn', 'Anillo Periférico, Tegucigalpa');

INSERT INTO dbo.Repuestos (codigo, nombre, categoria, marca, proveedor_id, stock_actual, stock_minimo, costo_compra, precio_venta, ubicacion) VALUES
('ACE-1540',  'Aceite 15W-40 diésel (galón)',    'ACEITE',      'Mobil',    2, 30, 10,   520.00,   680.00, 'Estante A1'),
('ACE-5W30',  'Aceite sintético 5W-30 (cuarto)', 'ACEITE',      'Castrol',  2, 48, 12,   210.00,   290.00, 'Estante A2'),
('FIL-ACE01', 'Filtro de aceite',                'FILTRO',      'Toyota',   1, 25,  8,   180.00,   260.00, 'Estante B1'),
('FIL-AIR01', 'Filtro de aire',                  'FILTRO',      'Toyota',   1, 15,  5,   300.00,   420.00, 'Estante B2'),
('BUJ-IRI01', 'Bujía de iridio',                 'BUJIA',       'NGK',      1, 40, 16,   190.00,   280.00, 'Estante B3'),
('BAT-12V70', 'Batería 12V 70Ah',                'BATERIA',     'LTH',      1,  6,  3,  2600.00,  3400.00, 'Bodega 1'),
('FRE-PAS01', 'Juego de pastillas de freno',     'FRENOS',      'Brembo',   1, 12,  4,   950.00,  1350.00, 'Estante C1'),
('RAD-HIL01', 'Radiador Hilux',                  'RADIADOR',    'Denso',    1,  2,  1,  5200.00,  6900.00, 'Bodega 2'),
('EMB-KIT01', 'Kit de embrague',                 'TRANSMISION', 'Exedy',    1,  3,  2,  4800.00,  6300.00, 'Bodega 2'),
('REF-GAL01', 'Refrigerante (galón)',            'OTRO',        'Prestone', 2,  4, 10,   260.00,   350.00, 'Estante A3'),   -- bajo mínimo
('EMP-CUL01', 'Empaque de culata',               'MOTOR',       'Toyota',   1,  5,  2,  1100.00,  1500.00, 'Estante C2'),
('MOT-1KD01', 'Motor 1KD-FTV 3.0 diésel',        'MOTOR',       'Toyota',   3,  1,  0, 95000.00,125000.00, 'Bodega motores'),
('MOT-2KD01', 'Motor 2KD-FTV 2.5 diésel',        'MOTOR',       'Toyota',   3,  1,  0, 82000.00,108000.00, 'Bodega motores'),
('MOT-YD251', 'Motor YD25 2.5 diésel',           'MOTOR',       'Nissan',   3,  1,  0, 78000.00,102000.00, 'Bodega motores');

INSERT INTO dbo.Servicios (codigo, nombre, descripcion, nivel_complejidad, categoria_complejidad, costo_mano_obra, tiempo_estimado_horas) VALUES
('SRV-101', 'Cambio de aceite y filtro',  'Drenado, cambio de aceite y filtro',                 1, 'Mantenimiento básico',       350.00,  1.00),
('SRV-102', 'Cambio de bujías',           'Sustitución del juego de bujías',                    1, 'Mantenimiento básico',       400.00,  1.00),
('SRV-103', 'Revisión de luces',          'Revisión y cambio de bombillos',                     1, 'Mantenimiento básico',       200.00,  0.50),
('SRV-201', 'Afinamiento mayor',          'Bujías, filtros, limpieza de cuerpo de aceleración', 2, 'Mantenimiento preventivo',  1200.00,  3.00),
('SRV-202', 'Limpieza de inyectores',     'Limpieza por ultrasonido',                           2, 'Mantenimiento preventivo',  1500.00,  2.50),
('SRV-203', 'Servicio de frenos',         'Cambio de pastillas y rectificado de discos',        2, 'Mantenimiento preventivo',   900.00,  2.00),
('SRV-301', 'Cambio de radiador',         'Sustitución de radiador y refrigerante',             3, 'Reparación intermedia',     1800.00,  3.00),
('SRV-302', 'Cambio de embrague',         'Sustitución del kit de embrague',                    3, 'Reparación intermedia',     3500.00,  6.00),
('SRV-303', 'Reparación de alternador',   'Diagnóstico y reparación',                           3, 'Reparación intermedia',     1600.00,  3.00),
('SRV-401', 'Reparación de transmisión',  'Desarme y reparación de caja de cambios',            4, 'Reparación avanzada',       8500.00, 16.00),
('SRV-402', 'Reparación de culata',       'Rectificado y cambio de empaque',                    4, 'Reparación avanzada',       7000.00, 12.00),
('SRV-501', 'Sustitución de motor',       'Desmontaje, instalación y pruebas de motor',         5, 'Reparación mayor',         18000.00, 24.00),
('SRV-502', 'Reconstrucción de motor',    'Reconstrucción completa del motor',                  5, 'Reparación mayor',         22000.00, 32.00);

INSERT INTO dbo.ServicioRepuestos (servicio_id, repuesto_id, cantidad_sugerida) VALUES
(1, 2, 5), (1, 3, 1),          -- cambio de aceite: aceite 5W-30 + filtro
(2, 5, 4),                     -- bujías
(4, 5, 4), (4, 4, 1), (4, 3, 1),
(6, 7, 1),                     -- frenos
(7, 8, 1), (7, 10, 2),         -- radiador + refrigerante
(8, 9, 1),                     -- embrague
(11, 11, 1),                   -- culata
(12, 12, 1), (12, 1, 2), (12, 10, 2);  -- sustitución de motor

-- Motores para el caso Hilux 2015
INSERT INTO dbo.Motores (numero_serie, marca, modelo_motor, cilindrada, combustible, estado, es_nuevo, costo, repuesto_id, proveedor_id) VALUES
('1KD-7845123', 'Toyota', '1KD-FTV',  '3.0L', 'DIESEL', 'DISPONIBLE', 1, 95000.00, 12, 3),   -- compatible con la Hilux 2015
('2KD-6632145', 'Toyota', '2KD-FTV',  '2.5L', 'DIESEL', 'RESERVADO',  1, 82000.00, 13, 3),   -- compatible, pero no disponible
('YD25-998877', 'Nissan', 'YD25DDTi', '2.5L', 'DIESEL', 'DISPONIBLE', 1, 78000.00, 14, 3);   -- incompatible con la Hilux

INSERT INTO dbo.CompatibilidadMotor (motor_id, marca, modelo, anio_desde, anio_hasta) VALUES
(1, 'Toyota', 'Hilux',    2005, 2015),
(1, 'Toyota', 'Fortuner', 2005, 2015),
(2, 'Toyota', 'Hilux',    2005, 2015),
(3, 'Nissan', 'Frontier', 2008, 2020);

-- Orden 1: Nissan Frontier, frenos, EN REPARACIÓN (sirve para consumir repuestos en la demostración)
INSERT INTO dbo.OrdenesTrabajo (cliente_id, vehiculo_id, kilometraje_ingreso, sintomas, diagnostico_preliminar, estado)
VALUES (2, 3, 98000, 'Ruido metálico al frenar', 'Pastillas gastadas', 'EN_REPARACION');
INSERT INTO dbo.DetalleOrden (orden_id, servicio_id, cantidad, precio_unitario) VALUES (1, 6, 1, 900.00);
INSERT INTO dbo.AsignacionMecanicos (orden_id, mecanico_id, es_responsable) VALUES (1, 2, 1), (1, 3, 0);
INSERT INTO dbo.Presupuestos (orden_id, subtotal, descuento, impuesto_id, monto_impuesto, total, estado, fecha_respuesta)
VALUES (1, 2250.00, 0, 1, 337.50, 2587.50, 'APROBADO', SYSDATETIME());
INSERT INTO dbo.DetallePresupuesto (presupuesto_id, tipo_linea, servicio_id, repuesto_id, descripcion, horas, cantidad, precio_unitario) VALUES
(1, 'MANO_OBRA', 6,    NULL, 'Servicio de frenos',          2.00, 1,  900.00),
(1, 'REPUESTO',  NULL, 7,    'Juego de pastillas de freno', 0.00, 1, 1350.00);
INSERT INTO dbo.ActividadesTrabajo (orden_id, mecanico_id, descripcion, horas, estado, inicio)
VALUES (1, 2, 'Cambio de pastillas delanteras', 0, 'EN_PROCESO', SYSDATETIME());

-- Orden 2: Honda Civic, cambio de aceite, histórica: entregada, pagada y facturada
-- (se crea abierta y al final se cierra, porque una orden cerrada no acepta servicios ni repuestos)
INSERT INTO dbo.OrdenesTrabajo (cliente_id, vehiculo_id, fecha_ingreso, kilometraje_ingreso, sintomas, diagnostico_preliminar, estado)
VALUES (1, 2, DATEADD(DAY, -30, SYSDATETIME()), 60000, 'Mantenimiento programado de 60,000 km', 'Cambio de aceite y filtro', 'EN_CONTROL_CALIDAD');
INSERT INTO dbo.DetalleOrden (orden_id, servicio_id, cantidad, precio_unitario) VALUES (2, 1, 1, 350.00);
INSERT INTO dbo.AsignacionMecanicos (orden_id, mecanico_id, es_responsable) VALUES (2, 2, 1);
INSERT INTO dbo.ActividadesTrabajo (orden_id, mecanico_id, descripcion, horas, estado, inicio, fin)
VALUES (2, 2, 'Cambio de aceite y filtro', 1.00, 'COMPLETADA', DATEADD(DAY, -30, SYSDATETIME()), DATEADD(DAY, -30, DATEADD(HOUR, 1, SYSDATETIME())));
INSERT INTO dbo.RepuestosOrden (orden_id, repuesto_id, nombre_historico, cantidad, costo_unitario_historico, precio_unitario_historico, consumido, fecha) VALUES
(2, 2, 'Aceite sintético 5W-30 (cuarto)', 5, 210.00, 290.00, 1, DATEADD(DAY, -30, SYSDATETIME())),
(2, 3, 'Filtro de aceite',                1, 180.00, 260.00, 1, DATEADD(DAY, -30, SYSDATETIME()));
INSERT INTO dbo.MovimientosInventario (repuesto_id, orden_id, tipo_movimiento, cantidad, stock_anterior, stock_resultante,
                                       costo_unitario, motivo, usuario_registro, fecha_movimiento) VALUES
(2, 2, 'SALIDA_ORDEN', 5, 53, 48, 210.00, 'Consumo en orden OT-000002', 'SISTEMA', DATEADD(DAY, -30, SYSDATETIME())),
(3, 2, 'SALIDA_ORDEN', 1, 26, 25, 180.00, 'Consumo en orden OT-000002', 'SISTEMA', DATEADD(DAY, -30, SYSDATETIME()));
INSERT INTO dbo.ControlesCalidad (orden_id, inspector_id, fecha, aprobado, checklist)
VALUES (2, 3, DATEADD(DAY, -30, DATEADD(HOUR, 1, SYSDATETIME())), 1, 'Nivel de aceite OK; sin fugas; testigo apagado');
-- Total = (350 + 1450 + 260) * 1.15 = 2,369.00
INSERT INTO dbo.Pagos (orden_id, monto, metodo, fecha, referencia) VALUES (2, 2369.00, 'TARJETA', DATEADD(DAY, -30, DATEADD(HOUR, 2, SYSDATETIME())), 'VISA-4521');
INSERT INTO dbo.Facturas (orden_id, fecha_emision, subtotal, descuento, impuesto, total)
VALUES (2, DATEADD(DAY, -30, DATEADD(HOUR, 2, SYSDATETIME())), 2060.00, 0, 309.00, 2369.00);
UPDATE dbo.OrdenesTrabajo SET estado = 'ENTREGADA', fecha_entrega = DATEADD(DAY, -30, DATEADD(HOUR, 2, SYSDATETIME()))
WHERE orden_id = 2;

-- Orden 3: Toyota Hilux 2015, caso obligatorio de sustitución de motor (recibida)
INSERT INTO dbo.OrdenesTrabajo (cliente_id, vehiculo_id, kilometraje_ingreso, sintomas, diagnostico_preliminar, estado)
VALUES (1, 1, 185000, 'Sobrecalentamiento, pérdida de potencia y humo blanco', 'Posible daño interno del motor', 'RECIBIDA');
GO


/* =====================================================================
   17. CONSULTAS DE EJEMPLO
   (sirven para la defensa: INNER JOIN, LEFT JOIN, GROUP BY,
    agregaciones y subconsultas)
   ===================================================================== */

-- INNER JOIN: vehículos con su propietario
-- SELECT v.placa, v.marca, v.modelo, v.anio, c.nombre AS propietario
-- FROM dbo.Vehiculos v INNER JOIN dbo.Clientes c ON c.cliente_id = v.cliente_id;

-- LEFT JOIN: todos los clientes y cuántos vehículos tienen (incluye los que no tienen)
-- SELECT c.nombre, COUNT(v.vehiculo_id) AS vehiculos
-- FROM dbo.Clientes c LEFT JOIN dbo.Vehiculos v ON v.cliente_id = c.cliente_id
-- GROUP BY c.nombre;

-- GROUP BY + agregaciones: órdenes e ingresos por estado
-- SELECT estado, COUNT(*) AS ordenes, SUM(total) AS monto, SUM(saldo) AS saldo_pendiente
-- FROM dbo.vw_OrdenesResumen GROUP BY estado;

-- Subconsulta: repuestos más caros que el precio promedio
-- SELECT codigo, nombre, precio_venta FROM dbo.Repuestos
-- WHERE precio_venta > (SELECT AVG(precio_venta) FROM dbo.Repuestos);

-- Subconsulta: mecánicos que nunca han sido asignados
-- SELECT codigo_empleado, nombre FROM dbo.Mecanicos
-- WHERE mecanico_id NOT IN (SELECT mecanico_id FROM dbo.AsignacionMecanicos);

-- Motores compatibles y disponibles para la Hilux (vehiculo_id = 1)
-- SELECT m.motor_id, m.numero_serie, m.modelo_motor, m.estado,
--        dbo.fn_MotorCompatible(m.motor_id, 1) AS compatible
-- FROM dbo.Motores m;

-- Vistas
-- SELECT * FROM dbo.vw_OrdenesResumen;
-- SELECT * FROM dbo.vw_StockBajo;
-- SELECT * FROM dbo.vw_SaldosFacturas;
-- SELECT * FROM dbo.vw_ProductividadMecanicos;
-- SELECT * FROM dbo.vw_ServiciosMasSolicitados ORDER BY veces_solicitado DESC;
-- EXEC dbo.sp_HistorialVehiculo @placa = 'HCD 5678';

-- Procedimientos
-- DECLARE @saldo DECIMAL(12,2);
-- EXEC dbo.sp_ConsumirRepuesto @orden_id = 1, @repuesto_id = 7, @cantidad = 1, @clave_operacion = 'A1B2C3D4-0000-0000-0000-000000000001';
-- EXEC dbo.sp_RegistrarPago @orden_id = 1, @monto = 1000, @metodo = 'EFECTIVO', @saldo_nuevo = @saldo OUTPUT; SELECT @saldo;
-- SELECT * FROM dbo.Auditoria ORDER BY fecha DESC;


/* =====================================================================
   18. USUARIOS DEL SISTEMA (inicio de sesión)
   ---------------------------------------------------------------------
   Las contraseñas NO se guardan: se guarda su huella PBKDF2-HMAC-SHA256
   (sal aleatoria de 16 bytes, 120000 iteraciones). La aplicación calcula la
   huella de lo que escribe el usuario y la compara con la guardada.
   Tras 5 intentos fallidos seguidos el usuario queda bloqueado.
   ===================================================================== */
DROP TABLE IF EXISTS dbo.Usuarios;
GO

CREATE TABLE dbo.Usuarios (
    usuario_id         INT IDENTITY(1,1) CONSTRAINT PK_Usuarios PRIMARY KEY,
    nombre_usuario     VARCHAR(30)   NOT NULL CONSTRAINT UQ_Usuarios_Nombre UNIQUE,
    nombre_completo    VARCHAR(100)  NOT NULL,
    rol                VARCHAR(20)   NOT NULL CONSTRAINT DF_Usuarios_Rol DEFAULT 'OPERADOR',
    sal                CHAR(32)      NOT NULL,
    hash_contrasena    CHAR(64)      NOT NULL,
    iteraciones        INT           NOT NULL,
    intentos_fallidos  INT           NOT NULL CONSTRAINT DF_Usuarios_Intentos DEFAULT 0,
    ultimo_acceso      DATETIME2(0)  NULL,
    activo             BIT           NOT NULL CONSTRAINT DF_Usuarios_Activo DEFAULT 1,
    fecha_registro     DATETIME2(0)  NOT NULL CONSTRAINT DF_Usuarios_Fecha DEFAULT SYSDATETIME(),
    CONSTRAINT CK_Usuarios_Rol      CHECK (rol IN ('ADMIN','OPERADOR','CAJA')),
    CONSTRAINT CK_Usuarios_Intentos CHECK (intentos_fallidos >= 0),
    CONSTRAINT CK_Usuarios_Iter     CHECK (iteraciones >= 10000)
);
GO

INSERT INTO dbo.Usuarios (nombre_usuario, nombre_completo, rol, sal, hash_contrasena, iteraciones) VALUES
    ('mairon', 'Mairon', 'ADMIN', 'CB7F56B26F204AB62CDD15513F6B63E5', 'F78513C84CCD9CCA5E135BF01A5FD3FD973DCB2C40C5A77CEB9B4C8BD3CF4C27', 120000),
    ('cristhian', 'Cristhian', 'ADMIN', '4F219CD00485915D8488EBC47E614C0C', '34E7B4E8532EA66A895B14185579AF63AA254DEB7539E00230364F5661581491', 120000),
    ('cristopher', 'Cristopher', 'ADMIN', 'FB5C65184048B9A7F2C2108845A51395', '1ABA41D9C55FCE2AAB59DA0E0F1E625BE9E5460B295BC4845D4C23D8EECBA83C', 120000);
GO

-- Consulta de ejemplo: usuarios y su último acceso
-- SELECT nombre_usuario, nombre_completo, rol, intentos_fallidos, ultimo_acceso, activo FROM dbo.Usuarios;
-- Desbloquear un usuario:
-- UPDATE dbo.Usuarios SET intentos_fallidos = 0 WHERE nombre_usuario = 'mairon';
